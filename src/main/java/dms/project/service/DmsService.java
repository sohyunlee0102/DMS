package dms.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClient;
import software.amazon.awssdk.services.databasemigration.model.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DmsService {

    private static final String TERRAFORM_FILE_PATH = "C:\\project\\project\\src\\main\\resources\\terraform\\dms"; // 실제 경로로 변경

    private final DatabaseMigrationClient databaseMigrationClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void startTask(String taskArn, SseEmitter emitter) {
        Boolean restart = false;

        List<Map<String, String>> existingTasks = getTasks(taskArn.split(":")[3]);
        System.out.println(existingTasks);

        if (existingTasks.stream().anyMatch(task -> !task.get("status").equals("ready") && task.get("arn").equals(taskArn))) {
            restart = true;
        }

        System.out.println(restart);

        if (!restart) {
            StartReplicationTaskResponse startResponse = databaseMigrationClient.startReplicationTask(
                    StartReplicationTaskRequest.builder()
                            .replicationTaskArn(taskArn)
                            .startReplicationTaskType(StartReplicationTaskTypeValue.START_REPLICATION)
                            .build()
            );
        } else {
            StartReplicationTaskResponse startResponse = databaseMigrationClient.startReplicationTask(
                    StartReplicationTaskRequest.builder()
                    .replicationTaskArn(taskArn)
                    .startReplicationTaskType(StartReplicationTaskTypeValue.RELOAD_TARGET)
                    .build());

        }

        System.out.println("Migration has started.");

        // 2. 상태 확인을 위한 쓰레드 시작
        new Thread(() -> {
            try {
                emitter.send("Start Data Migration...");

                while (true) {
                    try {
                        System.out.println("Fetching task status for ARN: " + taskArn);

                        // 3. 태스크 상태 조회
                        DescribeReplicationTasksResponse describeResponse = databaseMigrationClient.describeReplicationTasks(
                                DescribeReplicationTasksRequest.builder()
                                        .filters(Filter.builder()
                                                .name("replication-task-arn")
                                                .values(taskArn)
                                                .build())
                                        .build()
                        );

                        System.out.println("DescribeReplicationTasksResponse: " + describeResponse);

                        if (!describeResponse.replicationTasks().isEmpty()) {
                            ReplicationTask task = describeResponse.replicationTasks().get(0);
                            String status = task.status();
                            String progress = task.replicationTaskStats() != null
                                    ? task.replicationTaskStats().fullLoadProgressPercent() + "%"
                                    : "N/A";

                            System.out.println("Task Status: " + status);
                            System.out.println("Task Progress: " + progress);

                            try {
                                emitter.send(Map.of(
                                        "statusOfTask", status,
                                        "progressOfTask", progress
                                ));
                            } catch (IOException e) {
                                System.err.println("Error sending event: " + e.getMessage());
                                break;
                            }

                            if ("completed".equalsIgnoreCase(status) || "stopped".equalsIgnoreCase(status)) {
                                System.out.println("Task completed successfully.");
                                emitter.send("Data Migration completed.");
                                emitter.complete();
                                break;
                            } else if ("failed".equalsIgnoreCase(status)) {
                                String errorMessage = objectMapper.writeValueAsString(Map.of(
                                        "type", "error",
                                        "message", "Data Migration has failed."
                                ));
                                System.err.println("Task failed: " + errorMessage);
                                emitter.send(errorMessage);
                                emitter.complete();
                                break;
                            }
                        } else {
                            System.out.println("No replication tasks found for ARN: " + taskArn);
                        }

                        // 6. 일정 시간 대기 후 재시도
                        System.out.println("Waiting 5 seconds before the next status check...");
                        Thread.sleep(5000);

                    } catch (Exception e) {
                        System.err.println("Error during task status check: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (Exception e) {
                try {
                    emitter.send(new ObjectMapper().writeValueAsString(Map.of(
                            "type", "error",
                            "message", "Migration task execution has failed."
                    )));
                } catch (IOException ioException) {
                    System.err.println("Failed to send error message: " + ioException.getMessage());
                }
            } finally {
                emitter.complete();
            }
        }).start();

    }

    public List<Map<String, String>> getTasks(String region) {
        // 1. 복제 작업을 필터 없이 모두 조회
        DescribeReplicationTasksRequest request = DescribeReplicationTasksRequest.builder().build();
        DescribeReplicationTasksResponse response = databaseMigrationClient.describeReplicationTasks(request);

        if (response.replicationTasks().isEmpty()) {
            // 복제 작업이 없다면 빈 배열을 반환하고 콘솔에 경고 메시지 출력
            System.out.println("No replication tasks found for region: " + region);
            return new ArrayList<>(); // 빈 배열 반환
        }

        // 2. region에 해당하는 복제 작업만 필터링
        List<Map<String, String>> tasks = response.replicationTasks().stream()
                .filter(task -> task.replicationTaskArn().contains(region))  // ARN에 region이 포함된 작업만 필터링
                .map(task -> Map.of(
                        "taskIdentifier", task.replicationTaskIdentifier(),
                        "status", task.status(),
                        "arn", task.replicationTaskArn()))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            // region에 해당하는 복제 작업이 없다면 빈 배열을 반환하고 경고 메시지 출력
            System.out.println("No replication tasks found for region: " + region);
            return new ArrayList<>(); // 빈 배열 반환
        }

        return tasks;
    }

    // Terraform 명령을 실행하고, 진행 상황을 실시간으로 클라이언트에 전달
    public String executeTerraformWithProgress(Map<String, String> dmsTaskParams, SseEmitter emitter) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 필수 환경 변수 확인
        String tableMappings = dmsTaskParams.get("tableMappings");
        if (tableMappings == null || tableMappings.trim().isEmpty()) {
            throw new IllegalArgumentException("tableMappings is required and cannot be empty.");
        }

        // Terraform 환경 변수 설정
        processBuilder.environment().put("TF_VAR_task_name", dmsTaskParams.get("taskName"));
        processBuilder.environment().put("TF_VAR_migration_type", dmsTaskParams.get("migrationType"));
        processBuilder.environment().put("TF_VAR_target_table_preparation_mode", dmsTaskParams.get("targetTablePreparationMode"));
        processBuilder.environment().put("TF_VAR_lob_column_settings", dmsTaskParams.get("lobColumnSettings"));
        processBuilder.environment().put("TF_VAR_max_lob_size", dmsTaskParams.get("maxLobSize"));
        processBuilder.environment().put("TF_VAR_data_validation", dmsTaskParams.get("dataValidation"));
        processBuilder.environment().put("TF_VAR_task_logs", dmsTaskParams.get("taskLogs"));
        processBuilder.environment().put("TF_VAR_source_endpoint_arn", dmsTaskParams.get("sourceEndpointArn"));
        processBuilder.environment().put("TF_VAR_target_endpoint_arn", dmsTaskParams.get("targetEndpointArn"));
        processBuilder.environment().put("TF_VAR_replication_instance_arn", dmsTaskParams.get("replicationInstanceArn"));
        processBuilder.environment().put("TF_VAR_table_mappings", tableMappings);

        // Handle tags safely
        String tags = dmsTaskParams.getOrDefault("tags", "");
        Map<String, String> tagMap = new HashMap<>();
        if (tags != null && !tags.trim().isEmpty()) {
            String[] tagPairs = tags.split(",");
            for (String pair : tagPairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    tagMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
        processBuilder.environment().put("TF_VAR_task_tags", tagMap.toString());

        // Terraform 명령어 실행
        processBuilder.command("cmd.exe", "/c", "terraform init && terraform apply -auto-approve");
        processBuilder.directory(new File(TERRAFORM_FILE_PATH));

        System.out.println("Starting Terraform with parameters: " + dmsTaskParams);

        Process process = processBuilder.start();

        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    try {
                        emitter.send(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread errorThread =  new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                    try {
                        emitter.send("ERROR: " + line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();
        errorThread.start();

        int exitCode = process.waitFor();

        outputThread.join();
        errorThread.join();

        System.out.println(exitCode);

        if (exitCode == 0) {
            if ("false".equals(dmsTaskParams.get("startTaskOnCreation"))) {
                System.out.println("false");
                emitter.send("Migration task creation completed.");
                emitter.complete();
                return "Success";
            } else {
                System.out.println("true");
                // terraform output을 실행하여 taskArn을 추출
                ProcessBuilder outputProcessBuilder = new ProcessBuilder();
                outputProcessBuilder.command("cmd.exe", "/c", "terraform output -raw dms_task_arn");  // 'dms_task_arn' output 값을 가져옴
                outputProcessBuilder.directory(new File(TERRAFORM_FILE_PATH));

                Process outputProcess = outputProcessBuilder.start();
                int outputExitCode = outputProcess.waitFor();

                if (outputExitCode == 0) {
                    // Output 값 읽기
                    BufferedReader reader = new BufferedReader(new InputStreamReader(outputProcess.getInputStream()));
                    String taskArn = reader.readLine();  // output값을 추출
                    System.out.println("Before start");
                    startTask(taskArn, emitter);  // DMS 태스크 수동 시작
                    return "Success";
                } else {
                    String errorMessage = objectMapper.writeValueAsString(Map.of(
                            "type", "error",
                            "message", "Terraform execution failed with exit code: " + exitCode
                    ));
                    emitter.send(errorMessage);
                    throw new RuntimeException("Terraform execution failed.");
                }
            }
        } else {
            String errorMessage = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Terraform execution failed with exit code: " + exitCode
            ));
            emitter.send(errorMessage);
            throw new RuntimeException("Terraform execution failed.");
        }
    }

    // DMS 작업을 생성하는 메서드
    public String createDmsTask(Map<String, String> dmsTaskParams, SseEmitter emitter) throws IOException, InterruptedException {
        if (emitter == null) {
            throw new IllegalArgumentException("Emitter is null. Cannot proceed.");
        }

        return executeTerraformWithProgress(dmsTaskParams, emitter);
    }

}