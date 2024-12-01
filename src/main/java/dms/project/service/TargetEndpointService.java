package dms.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    private String testStatus = "idle";

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

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
    public String executeTerraformWithProgress(Map<String, String> targetEndpointParams) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 환경 변수 설정
        processBuilder.environment().put("TF_VAR_target_endpoint_id", targetEndpointParams.get("endpointId"));
        processBuilder.environment().put("TF_VAR_target_username", targetEndpointParams.get("username"));
        processBuilder.environment().put("TF_VAR_target_password", targetEndpointParams.get("password"));
        processBuilder.environment().put("TF_VAR_target_server_name", targetEndpointParams.get("serverName"));
        processBuilder.environment().put("TF_VAR_target_port", targetEndpointParams.get("port"));
        processBuilder.environment().put("TF_VAR_target_engine", targetEndpointParams.get("engine"));

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
            // 출력에서 ARN만 추출하여 반환 (출력에서 ARN을 찾는 로직 추가)
            String arn = extractArn(output.toString());

            if (arn != null) {
                // URL 디코딩하여 특수 문자 제거
                try {
                    arn = URLDecoder.decode(arn, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Error decoding ARN: " + e.getMessage());
                    arn = null;
                }

                // 쌍따옴표 제거
                if (arn != null) {
                    arn = arn.replace("\"", "");  // 쌍따옴표 제거
                }

                if (arn != null && !arn.isEmpty()) {
                    return arn;  // ARN만 반환
                } else {
                    throw new RuntimeException("ARN could not be extracted or decoded properly.");
                }
            } else {
                throw new RuntimeException("ARN could not be extracted from Terraform output.");
            }
        } else {
            return "Error executing Terraform, exit code: " + exitCode;
        }
    }

    // target endpoint를 생성하는 메서드
    public String createtargetEndpoint(Map<String, String> targetEndpointParams) throws IOException, InterruptedException {
        String targetEndpointArn = executeTerraformWithProgress(targetEndpointParams);

        // 2. 소스 엔드포인트 연결 테스트
        if (targetEndpointArn != null && !targetEndpointArn.isEmpty()) {
            testTargetConnection(targetEndpointArn, targetEndpointParams.get("RI"));
        }

        return "Terraform applied and connection test completed.";
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

    public String testTargetConnection(String targetEndpointArn, String replicationInstanceArn) {
        // "testing" 상태일 때, 연결 테스트 진행 중임을 표시
        if ("testing".equals(testStatus)) {
            return "Test is in progress. Please wait...";
        }

        try {
            // 상태를 "testing"으로 변경하여 연결 테스트가 진행 중임을 나타냄
            testStatus = "testing";

            // AWS DMS 연결 테스트 요청 생성
            TestConnectionRequest testConnectionRequest = TestConnectionRequest.builder()
                    .endpointArn(targetEndpointArn)  // 소스 엔드포인트 ARN
                    .replicationInstanceArn(replicationInstanceArn)  // 복제 인스턴스 ARN
                    .build();

            // DMS 클라이언트 생성
            DatabaseMigrationClient databaseMigrationClient = createDatabaseMigrationClient("ap-northeast-2");

            // 연결 테스트 실행
            TestConnectionResponse testSourceResponse = databaseMigrationClient.testConnection(testConnectionRequest);

            // 응답이 null일 경우를 처리하는 로직 추가
            if (testSourceResponse == null || testSourceResponse.sdkHttpResponse() == null) {
                System.err.println("Connection response is null.");
                testStatus = "failed";  // 실패 상태로 변경
                return "Connection response is null.";
            }

            // 연결 테스트 결과 확인
            if (testSourceResponse.sdkHttpResponse().isSuccessful()) {
                System.out.println("Source Endpoint Connection Test Success.");
                testStatus = "success";  // 테스트 성공 시 상태를 "success"로 설정
                return "Source Endpoint Connection Test Success.";  // 성공 메시지 반환
            } else {
                System.err.println("Source Endpoint Connection Test Failed: " + testSourceResponse.sdkHttpResponse().statusText());
                testStatus = "failed";  // 테스트 실패 시 상태를 "failed"로 설정
                return "Source Endpoint Connection Test Failed: " + testSourceResponse.sdkHttpResponse().statusText();  // 실패 메시지 반환
            }
        } catch (DatabaseMigrationException e) {
            System.err.println("Error while testing connection: " + e.getMessage());
            e.printStackTrace(); // 오류 상세 확인
            testStatus = "failed";  // 예외 발생 시 실패 상태로 변경
            return "Error while testing connection: " + e.getMessage();  // 오류 메시지 반환
        } catch (Exception e) {
            // 다른 예외 처리
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            testStatus = "failed";  // 예기치 못한 오류 발생 시 "failed"로 상태 변경
            return "Unexpected error: " + e.getMessage();  // 예기치 못한 오류 처리
        }
    }

    public String getTestStatus() {
        return testStatus;  // 현재 상태 반환
    }

}
