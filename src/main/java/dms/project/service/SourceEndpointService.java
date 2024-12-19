package dms.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClient;
import software.amazon.awssdk.services.databasemigration.model.*;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceEndpointService {

    private static final String TERRAFORM_FILE_PATH = "C:\\project\\project\\src\\main\\resources\\terraform\\source"; // 실제 경로로 변경

    @Autowired
    private final DatabaseMigrationClient databaseMigrationClient;

    // 클라이언트별 SSEEmitter 저장소
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError((e) -> emitters.remove(clientId));

        return emitter;
    }

    public SseEmitter getEmitter(String clientId) {
        return emitters.get(clientId);
    }

    public DatabaseMigrationClient createDatabaseMigrationClient(String region) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKey, secretKey);

        return DatabaseMigrationClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public List<Map<String, String>> getSourceEndpoints(String region) {
        DatabaseMigrationClient client = createDatabaseMigrationClient(region);
        DescribeEndpointsRequest request = DescribeEndpointsRequest.builder().build();
        DescribeEndpointsResponse response = client.describeEndpoints(request);

        return response.endpoints().stream()
                .filter(endpoint -> "SOURCE".equals(endpoint.endpointTypeAsString()))
                .map(endpoint -> {
                    Map<String, String> endpointData = new HashMap<>();
                    endpointData.put("id", endpoint.endpointIdentifier());
                    endpointData.put("arn", endpoint.endpointArn());
                    return endpointData;
                })
                .collect(Collectors.toList());
    }

    public String createSourceEndpoint(Map<String, String> sourceEndpointParams, SseEmitter emitter) throws IOException, InterruptedException {
        if (emitter == null) {
            throw new IllegalArgumentException("Emitter is null. Cannot proceed.");
        }

        String result = executeTerraformWithProgress(sourceEndpointParams, emitter);
        System.out.println("Before return");

        return result;
    }

    public String executeTerraformWithProgress(Map<String, String> sourceEndpointParams, SseEmitter emitter) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 환경 변수 설정
        processBuilder.environment().put("TF_VAR_source_endpoint_id", sourceEndpointParams.get("endpointId"));
        processBuilder.environment().put("TF_VAR_source_username", sourceEndpointParams.get("username"));
        processBuilder.environment().put("TF_VAR_source_password", sourceEndpointParams.get("password"));
        processBuilder.environment().put("TF_VAR_source_server_name", sourceEndpointParams.get("serverName"));
        processBuilder.environment().put("TF_VAR_source_port", sourceEndpointParams.get("port"));
        processBuilder.environment().put("TF_VAR_source_engine", sourceEndpointParams.get("engine"));

        String replicationInstance = sourceEndpointParams.get("RI");

        // tags 값을 쉼표로 구분된 문자열로 가져옴
        String tags = sourceEndpointParams.get("tags");
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
        processBuilder.environment().put("TF_VAR_source_tags", tagMap.toString());  // map 형태로 전달

        processBuilder.environment().put("AWS_ACCESS_KEY_ID", accessKey);  // AWS Access Key
        processBuilder.environment().put("AWS_SECRET_ACCESS_KEY", secretKey);

        // Terraform 명령어 설정
        processBuilder.command("cmd.exe", "/c",
                "terraform init && terraform apply -auto-approve");

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
                    try {
                        emitter.send(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                    try {
                        emitter.send("ERROR: " + line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        processBuilder.command("cmd.exe", "/c",
                "terraform output -raw source_endpoint_arn");  // ARN 출력 명령어 실행
        Process outputProcess = processBuilder.start();  // ARN 가져오기 프로세스 시작
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(outputProcess.getInputStream()));

        StringBuilder arnOutput = new StringBuilder();
        String line;
        while ((line = outputReader.readLine()) != null) {
            arnOutput.append(line).append("\n");
        }

        int outputExitCode = outputProcess.waitFor();
        if (outputExitCode == 0) {
            String arn = arnOutput.toString().trim();  // ARN 가져오기 성공
            emitter.send("Terraform execution successful.");
            emitter.send("Testing connection of created endpoint...");
            System.out.println("Arn: " + arn);
            System.out.println("RI : " + replicationInstance);
            processBuilder.command("cmd.exe", "/c",
                    "aws dms test-connection --region ap-northeast-2 " +
                            "--endpoint-arn " + arn +
                            " --replication-instance-arn " + replicationInstance
            );

            Process connectionProcess = processBuilder.start();  // <-- Test-Connection 명령어 실행
            BufferedReader connectionReader = new BufferedReader(new InputStreamReader(connectionProcess.getInputStream()));

            StringBuilder connectionOutput = new StringBuilder();
            String connectionLine;
            while ((connectionLine = connectionReader.readLine()) != null) {
                connectionOutput.append(connectionLine).append("\n");
                System.out.println(connectionLine);  // 터미널에 실시간으로 출력
            }

            int connectionExitCode = connectionProcess.waitFor();
            if (connectionExitCode == 0) {
                while (true) {
                    processBuilder.command("cmd.exe", "/c",
                            "aws dms describe-connections --region ap-northeast-2 --filters Name=endpoint-arn,Values=" + arn
                    );

                    Process describeProcess = processBuilder.start();
                    BufferedReader describeReader = new BufferedReader(new InputStreamReader(describeProcess.getInputStream()));

                    StringBuilder describeOutput = new StringBuilder();
                    String describeLine;
                    while ((describeLine = describeReader.readLine()) != null) {
                        describeOutput.append(describeLine).append("\n");
                        System.out.println(describeLine);
                    }

                    int describeExitCode = describeProcess.waitFor();
                    System.out.println("Describe: " + describeExitCode);
                    if (describeOutput.toString().contains("testing")) {
                        System.out.println("Contain");
                    }
                    if (describeExitCode == 0 && describeOutput.toString().contains("successful")) {
                        emitter.send("Connection test successful on status check.");
                        /*
                        ProcessBuilder unlockProcessBuilder = new ProcessBuilder("terraform", "force-unlock", "<lock_id>");
                        unlockProcessBuilder.start();
                         */
                        return arn;  // ARN 반환
                    } else if (describeOutput.toString().contains("testing"))  {
                        emitter.send("Still testing...");
                        Thread.sleep(5000);  // 5초 대기 후 재시도
                    } else {
                        // describeOutput JSON 파싱
                        JsonNode rootNode = objectMapper.readTree(describeOutput.toString());
                        JsonNode connections = rootNode.get("Connections");

                        if (connections != null && connections.isArray() && connections.size() > 0) {
                            JsonNode connection = connections.get(0); // 첫 번째 연결 정보 사용
                            String lastFailureMessage = connection.get("LastFailureMessage").asText();

                            // 에러 형식으로 프론트에 전달
                            String errorMessage = objectMapper.writeValueAsString(Map.of(
                                    "type", "error",
                                    "message", lastFailureMessage
                            ));
                            emitter.send(errorMessage);
                            throw new RuntimeException("Test connection failed.");
                        } else {
                            // 실패 이유를 추출할 수 없는 경우 기본 메시지 전달
                            String errorMessage = objectMapper.writeValueAsString(Map.of(
                                    "type", "error",
                                    "message", "Unable to parse failure reason from response."
                            ));
                            emitter.send(errorMessage);
                            throw new RuntimeException("Test connection failed.");
                        }
                    }
                    /*
                    ProcessBuilder unlockProcessBuilder = new ProcessBuilder("terraform", "force-unlock", "<lock_id>");
                    unlockProcessBuilder.start();
                     */
                }
            } else {
                String errorMessage = objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", "Connection test failed with exit code: " + connectionExitCode
                ));
                emitter.send(errorMessage);emitter.send("Connection test failed with exit code: " + connectionExitCode);
                throw new RuntimeException("Connection test failed.");

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

    private String extractArn(String terraformOutput) {
        String arnPrefix = "arn:aws:dms:";
        int startIndex = terraformOutput.indexOf(arnPrefix);

        if (startIndex != -1) {
            int endIndex = terraformOutput.indexOf("\"", startIndex);
            return endIndex != -1 ? terraformOutput.substring(startIndex, endIndex) : terraformOutput.substring(startIndex);
        }
        return null;
    }

    private void completeEmitterWithError(SseEmitter emitter) {
        emitter.completeWithError(new RuntimeException("Connection test failed.")); // 에러로 Emitter 완료 처리
    }

}