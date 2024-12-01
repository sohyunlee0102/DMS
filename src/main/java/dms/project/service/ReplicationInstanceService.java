package dms.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClient;
import software.amazon.awssdk.services.databasemigration.model.DescribeReplicationInstancesRequest;
import software.amazon.awssdk.services.databasemigration.model.DescribeReplicationInstancesResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplicationInstanceService {

    private static final String TERRAFORM_FILE_PATH = "C:\\project\\project\\src\\main\\resources\\terraform\\replication"; // 실제 경로로 변경

    private final Ec2Client ec2Client;

    private final DatabaseMigrationClient databaseMigrationClient;

    private static final Logger logger = LoggerFactory.getLogger(ReplicationInstanceService.class);

    public List<Map<String, String>> getRIs(String region) {
        // 주어진 리전을 기반으로 새 클라이언트 생성
        DatabaseMigrationClient regionalClient = databaseMigrationClient;

        // 복제 인스턴스 요청 생성
        DescribeReplicationInstancesRequest request = DescribeReplicationInstancesRequest.builder().build();

        // 복제 인스턴스 이름과 ARN 반환
        DescribeReplicationInstancesResponse response = regionalClient.describeReplicationInstances(request);

       // System.out.println(response);

        return response.replicationInstances().stream()
                .map(instance -> {
                    Map<String, String> instanceData = new HashMap<>();
                    instanceData.put("name", instance.replicationInstanceIdentifier());
                    instanceData.put("arn", instance.replicationInstanceArn());
                    return instanceData;
                })
                .collect(Collectors.toList());
    }

    public List<String> getVpcList() {
        // DescribeVpcsRequest로 모든 VPC 정보를 가져오는 요청을 보냄
        DescribeVpcsRequest describeVpcsRequest = DescribeVpcsRequest.builder().build();
        DescribeVpcsResponse vpcsResponse = ec2Client.describeVpcs(describeVpcsRequest);

        // VPC ID만 추출하여 반환
        return vpcsResponse.vpcs().stream()
                .map(Vpc::vpcId)
                .collect(Collectors.toList());
    }

    // 특정 VPC에 속한 보안 그룹 목록을 가져오는 메소드
    public List<String> getSecurityGroupsForVpc(String vpcId) {
        // DescribeSecurityGroupsRequest로 VPC ID에 해당하는 보안 그룹을 가져옴
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = DescribeSecurityGroupsRequest.builder()
                .filters(Filter.builder().name("vpc-id").values(vpcId).build())
                .build();

        DescribeSecurityGroupsResponse securityGroupsResponse = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest);

        // 보안 그룹 ID만 추출하여 반환
        return securityGroupsResponse.securityGroups().stream()
                .map(SecurityGroup::groupId)
                .collect(Collectors.toList());
    }

    // VPC와 해당 보안 그룹 목록을 함께 반환하는 메소드
    public List<Map<String, String>> getVpcListWithSecurityGroups() {
        // VPC 목록을 가져옴
        List<String> vpcIds = getVpcList();

        // 각 VPC에 대한 보안 그룹 정보를 추가하여 결과 리스트에 저장
        List<Map<String, String>> vpcsWithSecurityGroups = new ArrayList<>();

        // 각 VPC에 대해 보안 그룹 정보를 가져옴
        for (String vpcId : vpcIds) {
            // 해당 VPC의 보안 그룹 목록을 가져옴
            List<String> securityGroups = getSecurityGroupsForVpc(vpcId);

            // 보안 그룹이 하나 이상 있는 경우 첫 번째 보안 그룹만 선택
            if (!securityGroups.isEmpty()) {
                String representativeSecurityGroup = securityGroups.get(0);  // 첫 번째 보안 그룹을 대표로 선택

                // 각 VPC와 그에 해당하는 대표 보안 그룹을 맵으로 묶어서 리스트에 추가
                Map<String, String> vpcAndSecurityGroup = new HashMap<>();
                vpcAndSecurityGroup.put("vpcId", vpcId);
                vpcAndSecurityGroup.put("securityGroup", representativeSecurityGroup);

                vpcsWithSecurityGroups.add(vpcAndSecurityGroup);

                // 디버깅용 출력 (필요시)
                System.out.println("VPC ID: " + vpcId + ", Representative Security Group: " + representativeSecurityGroup);
            }
        }

        return vpcsWithSecurityGroups;
    }

    public List<String> getSubnet() {
        // 서브넷 정보를 조회하는 요청 생성
        DescribeSubnetsRequest request = DescribeSubnetsRequest.builder().build();

        try {
            // EC2 클라이언트를 통해 서브넷 목록을 조회
            DescribeSubnetsResponse response = ec2Client.describeSubnets(request);

            // 서브넷 ID만 추출하여 리스트로 반환
            return response.subnets().stream()
                    .map(Subnet::subnetId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching subnets: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 에러 발생 시 빈 리스트 반환
        }
    }

    // Terraform 명령을 실행하고, 진행 상황을 실시간으로 클라이언트에 전달
    public String executeTerraformWithProgress(Map<String, String> replicationInstanceParams) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 환경 변수 설정
        processBuilder.environment().put("TF_VAR_instance_name", replicationInstanceParams.get("instanceName"));
        processBuilder.environment().put("TF_VAR_instance_class", replicationInstanceParams.get("instanceClass"));
        processBuilder.environment().put("TF_VAR_engine_version", replicationInstanceParams.get("engineVersion"));
        processBuilder.environment().put("TF_VAR_high_availability", replicationInstanceParams.get("highAvailability"));
        processBuilder.environment().put("TF_VAR_storage", replicationInstanceParams.get("storage"));
        processBuilder.environment().put("TF_VAR_vpc", replicationInstanceParams.get("vpc"));
        processBuilder.environment().put("TF_VAR_subnet_group", replicationInstanceParams.get("subnetGroup"));
        processBuilder.environment().put("TF_VAR_public_accessible", replicationInstanceParams.get("publicAccessible"));
        processBuilder.environment().put("TF_VAR_description", replicationInstanceParams.get("description"));

        String tags = replicationInstanceParams.get("tags");
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
        processBuilder.environment().put("TF_VAR_RI_tags", tagMap.toString());  // map 형태로 전달

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
            return "Replication instance created successfully!";
        } else {
            String errorMessage = "Error executing Terraform, exit code: " + exitCode;
            errorMessage += "\n" + output.toString();  // 실제 출력된 내용 추가
            return errorMessage;
        }
    }

    public String createReplicationInstance(Map<String, String> replicationInstanceParams) throws IOException, InterruptedException {
        // Terraform 명령어 실행
        try {
            return executeTerraformWithProgress(replicationInstanceParams);  // Terraform 명령 실행
        } catch (IOException e) {
            // IOException 처리
            return "IO Error occurred while executing Terraform: " + e.getMessage();
        } catch (InterruptedException e) {
            // InterruptedException 처리
            return "Execution interrupted: " + e.getMessage();
        }
    }

}
