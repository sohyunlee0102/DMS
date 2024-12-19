package dms.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.Collectors;

@Service
public class TargetEndpointService {

    private static final String TERRAFORM_FILE_PATH = "C:\\project\\project\\src\\main\\resources\\terraform\\target"; // 실제 경로로 변경

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatabaseMigrationClient createDatabaseMigrationClient(String region) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKey, secretKey);

        return DatabaseMigrationClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public List<Map<String, String>> getTargetEndpoints(String region) {
        // 리전별로 DatabaseMigrationClient 생성
        DatabaseMigrationClient client = createDatabaseMigrationClient(region);

        // DescribeEndpointsRequest 생성
        DescribeEndpointsRequest request = DescribeEndpointsRequest.builder().build();

        // 엔드포인트 조회
        DescribeEndpointsResponse response = client.describeEndpoints(request);

        // 엔드포인트 응답 출력 (디버깅용)
        System.out.println("DescribeEndpointsResponse: " + response);

        // 각 엔드포인트의 타입을 출력하여 확인
        response.endpoints().forEach(endpoint -> {
            System.out.println("Endpoint Identifier: " + endpoint.endpointIdentifier() + ", Type: " + endpoint.endpointTypeAsString());
        });

        // 소스 엔드포인트만 필터링하여 ARN 목록 추출
        List<Map<String, String>> targetEndpoints = response.endpoints().stream()
                .filter(endpoint -> "TARGET".equals(endpoint.endpointTypeAsString()))  // 엔드포인트 타입이 'SOURCE'인 경우만 필터링
                .map(endpoint -> {
                    Map<String, String> endpointData = new HashMap<>();
                    endpointData.put("id", endpoint.endpointIdentifier());  // ID
                    endpointData.put("arn", endpoint.endpointArn());  // ARN
                    return endpointData;
                })
                .collect(Collectors.toList());

        // 필터링된 소스 엔드포인트 로그 출력 (디버깅용)
        System.out.println("Filtered Source Endpoints: " + targetEndpoints);

        return targetEndpoints;
    }

    // Terraform 명령을 실행하고, 진행 상황을 실시간으로 클라이언트에 전달
    public String executeTerraformWithProgress(Map<String, String> targetEndpointParams, SseEmitter emitter) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 환경 변수 설정
        processBuilder.environment().put("TF_VAR_target_endpoint_id", targetEndpointParams.get("endpointId"));
        processBuilder.environment().put("TF_VAR_target_username", targetEndpointParams.get("username"));
        processBuilder.environment().put("TF_VAR_target_password", targetEndpointParams.get("password"));
        processBuilder.environment().put("TF_VAR_target_server_name", targetEndpointParams.get("serverName"));
        processBuilder.environment().put("TF_VAR_target_port", targetEndpointParams.get("port"));
        processBuilder.environment().put("TF_VAR_target_engine", targetEndpointParams.get("engine"));

        String replicationInstance = targetEndpointParams.get("RI");

        // tags 값을 쉼표로 구분된 문자열로 가져옴
        String tags = targetEndpointParams.get("tags");
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
        processBuilder.environment().put("TF_VAR_target_tags", tagMap.toString());  // map 형태로 전달

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
                "terraform output -raw target_endpoint_arn");  // ARN 출력 명령어 실행
        Process outputProcess = processBuilder.start();  // ARN 가져오기 프로세스 시작
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(outputProcess.getInputStream()));

        StringBuilder arnOutput = new StringBuilder();
        String line;
        while ((line = outputReader.readLine()) != null) {
            arnOutput.append(line).append("\n");
        }

        int outputExitCode = outputProcess.waitFor();

        // 종료 코드 반환
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
                }
            } else {
                String errorMessage = objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", "Connection test failed with exit code: " + connectionExitCode
                ));
                emitter.send(errorMessage);
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

    // target endpoint를 생성하는 메서드
    public String createTargetEndpoint(Map<String, String> targetEndpointParams, SseEmitter emitter) throws IOException, InterruptedException {
        if (emitter == null) {
            throw new IllegalArgumentException("Emitter is null. Cannot proceed.");
        }

        String result = executeTerraformWithProgress(targetEndpointParams, emitter);
        System.out.println("Before return");

        return result;
    }

    private String extractArn(String terraformOutput) {
        // "arn:aws:dms:"로 시작하는 ARN을 추출
        String arnPrefix = "arn:aws:dms:";
        int startIndex = terraformOutput.indexOf(arnPrefix);

        if (startIndex != -1) {
            int endIndex = terraformOutput.indexOf("\"", startIndex);  // 종료 인덱스를 찾기 위해 "을 찾음
            if (endIndex == -1) {
                endIndex = terraformOutput.length();  // 종료 인덱스를 못 찾으면 출력의 끝까지
            }

            // ARN 추출
            String arn = terraformOutput.substring(startIndex, endIndex);

            // ARN이 올바른 형식인지 확인
            if (arn.matches("arn:aws:dms:[a-zA-Z0-9-]+:[0-9]+:endpoint:[a-zA-Z0-9]+")) {
                return arn;  // 유효한 ARN 반환
            } else {
                return null;  // 유효하지 않은 ARN
            }
        }
        return null;  // ARN을 찾을 수 없으면 null 반환
    }

}
