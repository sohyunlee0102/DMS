package dms.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClient;
import software.amazon.awssdk.services.databasemigration.model.*;
import software.amazon.awssdk.services.ssmincidents.model.ListReplicationSetsRequest;
import software.amazon.awssdk.services.ssmincidents.model.ListReplicationSetsResponse;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DmsService {

    private static final String TERRAFORM_FILE_PATH = "C:\\project\\project\\src\\main\\resources\\terraform\\dms"; // 실제 경로로 변경

    private final DatabaseMigrationClient databaseMigrationClient;

    public void startReplicationTaskWithConnectionCheck(String taskArn, String replicationInstanceArn, String sourceEndpointArn, String targetEndpointArn) {
        // 1. Test connections
        try {
            testReplicationInstanceStatus(replicationInstanceArn); // Check the status of the replication instance
            testConnections(replicationInstanceArn, sourceEndpointArn, targetEndpointArn); // Test source and target endpoint connections
        } catch (RuntimeException e) {
            // If connection test fails, handle the exception
            throw new RuntimeException("Connection test failed: " + e.getMessage());
        }

        // 2. If connections are successful, start the replication task
        try {
            StartReplicationTaskRequest request = StartReplicationTaskRequest.builder()
                    .replicationTaskArn(taskArn) // Use the ARN of the already created task
                    .startReplicationTaskType(StartReplicationTaskTypeValue.START_REPLICATION)
                    .build();

            StartReplicationTaskResponse response = databaseMigrationClient.startReplicationTask(request);
            System.out.println("Task started successfully: " + response.replicationTask().replicationTaskArn());
        } catch (DatabaseMigrationException e) {
            throw new RuntimeException("AWS DMS error: " + e.getMessage(), e);
        }
    }

    private void testConnections(String replicationInstanceArn, String sourceEndpointArn, String targetEndpointArn) {
        try {
            // 1. 소스 엔드포인트에 대한 연결 테스트
            TestConnectionRequest testConnectionRequest = TestConnectionRequest.builder()
                    .replicationInstanceArn(replicationInstanceArn)
                    .endpointArn(sourceEndpointArn)
                    .build();
            TestConnectionResponse testSourceResponse = databaseMigrationClient.testConnection(testConnectionRequest);
            System.out.println("Source Endpoint Connection Test Success.");

            // 2. 타겟 엔드포인트에 대한 연결 테스트
            testConnectionRequest = TestConnectionRequest.builder()
                    .replicationInstanceArn(replicationInstanceArn)
                    .endpointArn(targetEndpointArn)
                    .build();
            TestConnectionResponse testTargetResponse = databaseMigrationClient.testConnection(testConnectionRequest);
            System.out.println("Target Endpoint Connection Test Success.");

            // 3. 복제 인스턴스에 대한 연결 테스트
            DescribeReplicationInstancesRequest describeReplicationInstancesRequest = DescribeReplicationInstancesRequest.builder().build();
            DescribeReplicationInstancesResponse describeReplicationInstancesResponse = databaseMigrationClient.describeReplicationInstances(describeReplicationInstancesRequest);

            describeReplicationInstancesResponse.replicationInstances().forEach(instance -> {
                System.out.println("Replication Instance ARN: " + instance.replicationInstanceArn() + " - Status: " + instance.replicationInstanceStatus());
            });

        } catch (DatabaseMigrationException e) {
            System.err.println("Error while testing connection: " + e.getMessage());
            e.printStackTrace(); // 스택 트레이스를 통해 자세한 오류 확인
            throw new RuntimeException("Error while testing connection: " + e.getMessage(), e);
        }
    }

    private void testReplicationInstanceStatus(String replicationInstanceArn) {
        try {
            // Check the status of the replication instance
            DescribeReplicationInstancesRequest describeRequest = DescribeReplicationInstancesRequest.builder()
                    .filters(Filter.builder()
                            .name("replication-instance-arn")
                            .values(replicationInstanceArn)
                            .build())
                    .build();

            DescribeReplicationInstancesResponse describeResponse = databaseMigrationClient.describeReplicationInstances(describeRequest);

            // Check if the replication instance is found
            if (describeResponse.replicationInstances().isEmpty()) {
                throw new RuntimeException("Replication instance not found.");
            }

            ReplicationInstance replicationInstance = describeResponse.replicationInstances().get(0);
            String replicationInstanceStatus = replicationInstance.replicationInstanceStatus();

            // Check if the replication instance status is 'available'
            if (!"available".equals(replicationInstanceStatus)) {
                throw new RuntimeException("Replication instance status is not 'available'. Current status: " + replicationInstanceStatus);
            }

            System.out.println("Replication instance status: " + replicationInstanceStatus);
        } catch (DatabaseMigrationException e) {
            throw new RuntimeException("Error while checking replication instance status: " + e.getMessage(), e);
        }
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

    private String getReplicationInstanceArnByRegion(String region) {
        // 이 메서드는 특정 region에 있는 복제 인스턴스를 조회하여 ARN을 반환하는 로직입니다.
        DescribeReplicationInstancesRequest request = DescribeReplicationInstancesRequest.builder()
                .build();

        DescribeReplicationInstancesResponse response = databaseMigrationClient.describeReplicationInstances(request);

        // 복제 인스턴스를 순회하면서 region에 맞는 ARN을 찾습니다.
        return response.replicationInstances().stream()
                .filter(instance -> instance.replicationInstanceArn().contains(region))  // region을 포함한 ARN 필터링
                .map(ReplicationInstance::replicationInstanceArn)
                .findFirst()
                .orElse(null);  // 해당 지역에 맞는 복제 인스턴스가 없으면 null 반환
    }

    // Terraform 명령을 실행하고, 진행 상황을 실시간으로 클라이언트에 전달
    public String executeTerraformWithProgress(Map<String, String> dmsTaskParams) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 환경 변수 설정
        processBuilder.environment().put("TF_VAR_task_name", dmsTaskParams.get("taskName"));
        processBuilder.environment().put("TF_VAR_migration_type", dmsTaskParams.get("migrationType"));
        processBuilder.environment().put("TF_VAR_target_table_preparation_mode", dmsTaskParams.get("targetTablePreparationMode"));
        processBuilder.environment().put("TF_VAR_lob_column_settings", dmsTaskParams.get("lobColumnSettings"));
        processBuilder.environment().put("TF_VAR_max_lob_size", dmsTaskParams.get("maxLobSize"));
        processBuilder.environment().put("TF_VAR_data_validation", dmsTaskParams.get("dataValidation"));
        processBuilder.environment().put("TF_VAR_task_logs", dmsTaskParams.get("taskLogs"));
        processBuilder.environment().put("TF_VAR_start_task_on_creation", dmsTaskParams.get("startTaskOnCreation"));
        processBuilder.environment().put("TF_VAR_source_endpoint_arn", dmsTaskParams.get("sourceEndpointArn"));
        processBuilder.environment().put("TF_VAR_target_endpoint_arn", dmsTaskParams.get("targetEndpointArn"));
        processBuilder.environment().put("TF_VAR_replication_instance_arn", dmsTaskParams.get("replicationInstanceArn"));

        String tableMappings = dmsTaskParams.get("tableMappings");
        if (tableMappings == null || tableMappings.trim().isEmpty()) {
            throw new IllegalArgumentException("tableMappings is required and cannot be empty.");
        }

        String tags = dmsTaskParams.get("tags");
        String[] tagPairs = tags.split(",");  // ","로 구분된 태그 쌍을 배열로 분리

        // tagMap 생성
        Map<String, String> tagMap = new HashMap<>();
        for (String pair : tagPairs) {
            String[] keyValue = pair.split("=");  // "key=value" 형식으로 분리
            if (keyValue.length == 2) {
                tagMap.put(keyValue[0], keyValue[1]);
            }
        }

        // Terraform에 map 형태로 전달 (tags를 환경 변수로 전달)
        processBuilder.environment().put("TF_VAR_task_tags", tagMap.toString());  // map 형태로 전달

        // Terraform 명령어 설정
        processBuilder.command("cmd.exe", "/c", "terraform init && terraform apply -auto-approve");
        processBuilder.directory(new File(TERRAFORM_FILE_PATH));

        // 출력 처리
        StringBuilder output = new StringBuilder();
        Process process = processBuilder.start();

        // 표준 출력 및 표준 오류 스트림을 비동기적으로 처리
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println(line);  // 터미널에 실시간으로 출력
                }
            } catch (IOException e) {
                e.printStackTrace();  // 로깅을 사용해도 좋음
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append("ERROR: ").append(line).append("\n");
                    System.err.println(line);  // 오류는 에러 스트림으로 출력
                }
            } catch (IOException e) {
                e.printStackTrace();  // 로깅을 사용해도 좋음
            }
        });

        // 쓰레드 시작
        outputThread.start();
        errorThread.start();

        // Terraform 명령어 완료 대기
        int exitCode = process.waitFor();

        // 프로세스 종료 대기
        outputThread.join();
        errorThread.join();

        // 종료 코드 반환
        if (exitCode == 0) {
            return "Terraform applied successfully!";
        } else {
            return "Error executing Terraform, exit code: " + exitCode;
        }
    }

    // DMS 작업을 생성하는 메서드
    public String createDmsTask(Map<String, String> dmsTaskParams) throws IOException, InterruptedException {
        // Terraform 명령 실행 후 DMS 작업 시작
        String terraformOutput = executeTerraformWithProgress(dmsTaskParams);
   //     startDmsTask();  // Terraform 후 DMS 작업을 시작
        return terraformOutput;
    }

}