package dms.project.web;

import dms.project.dto.*;
import dms.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/dms")
public class DmsController {

    private static final Logger logger = LoggerFactory.getLogger(DmsController.class);

    private static final String ERROR_MESSAGE = "Error processing the request: ";

    @Autowired
    private DmsService dmsService;

    @Autowired
    private SourceEndpointService sourceEndpointService;

    @Autowired
    private TargetEndpointService targetEndpointService;

    @Autowired
    private ReplicationInstanceService replicationInstanceService;

    @PostMapping("/create-source-endpoint")
    public SseEmitter createSourceEndpoint(@RequestBody SourceEndpointRequest request) {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                System.out.println("I'm HERE");

                // 태그 배열을 쉼표로 구분된 문자열로 변환 (Map<String, Object> 형태의 태그 처리)
                StringBuilder tagsStringBuilder = new StringBuilder();
                for (Map<String, Object> tag : request.getTags()) {
                    String key = (String) tag.get("key");
                    String value = (String) tag.get("value");

                    if (tagsStringBuilder.length() > 0) {
                        tagsStringBuilder.append(", ");
                    }
                    tagsStringBuilder.append(key).append("=").append(value); // "key=value" 형태로 만듦
                }
                String tags = tagsStringBuilder.toString(); // 변환된 tags 문자열

                // 진행 상태를 클라이언트에 전송 (시작)
                emitter.send("Starting source endpoint creation...");

                System.out.println(request.getReplicationInstance());
                // Source endpoint 생성에 필요한 파라미터들
                Map<String, String> sourceEndpointParams = Map.of(
                        "endpointId", request.getEndpointId(),
                        "username", request.getUsername(),
                        "password", request.getPassword(),
                        "serverName", request.getServerName(),
                        "port", request.getPort(),
                        "engine", request.getEngine(),
                        "tags", tags,
                        "RI", request.getReplicationInstance()  // 실제 복제 인스턴스 ARN 추가
                );

                // Terraform 명령 실행 및 결과를 실시간으로 클라이언트에 전달
                String result = sourceEndpointService.createSourceEndpoint(sourceEndpointParams);

                // Terraform 실행 후 결과 전달 (성공 메시지)
                emitter.send("Source endpoint creation completed.");
                emitter.send(result); // 명령 실행 결과를 전달

                emitter.complete(); // 작업 완료
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e); // 에러 발생 시 에러로 종료
            }
        }).start();
        return emitter;
    }

    // 타겟 엔드포인트 생성
    @PostMapping("/create-target-endpoint")
    public SseEmitter createTargetEndpoint(@RequestBody TargetEndpointRequest request) {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                System.out.println("I'm HERE");

                // 태그 배열을 쉼표로 구분된 문자열로 변환 (Map<String, Object> 형태의 태그 처리)
                StringBuilder tagsStringBuilder = new StringBuilder();
                for (Map<String, Object> tag : request.getTags()) {
                    String key = (String) tag.get("key");
                    String value = (String) tag.get("value");

                    if (tagsStringBuilder.length() > 0) {
                        tagsStringBuilder.append(", ");
                    }
                    tagsStringBuilder.append(key).append("=").append(value); // "key=value" 형태로 만듦
                }
                String tags = tagsStringBuilder.toString(); // 변환된 tags 문자열

                // 진행 상태를 클라이언트에 전송 (시작)
                emitter.send("Starting target endpoint creation...");

                // Source endpoint 생성에 필요한 파라미터들
                Map<String, String> targetEndpointParams = Map.of(
                        "endpointId", request.getEndpointId(),
                        "username", request.getUsername(),
                        "password", request.getPassword(),
                        "serverName", request.getServerName(),
                        "port", request.getPort(),
                        "engine", request.getEngine(),
                        "tags", tags,  // 변환된 tags 문자열 추가
                        "RI", request.getReplicationInstance()
                );

                // Terraform 명령 실행 및 결과를 실시간으로 클라이언트에 전달
                String result = targetEndpointService.createtargetEndpoint(targetEndpointParams);

                // Terraform 실행 후 결과 전달 (성공 메시지)
                emitter.send("Target endpoint creation completed.");
                emitter.send(result); // 명령 실행 결과를 전달

                emitter.complete(); // 작업 완료
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e); // 에러 발생 시 에러로 종료
            }
        }).start();
        return emitter;
    }

    @PostMapping("/create-replication-instance")
    public SseEmitter createReplicationInstance(@RequestBody ReplicationInstanceRequest request) {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                // 태그 배열을 쉼표로 구분된 문자열로 변환
                StringBuilder tagsStringBuilder = new StringBuilder();
                for (Map<String, Object> tag : request.getTags()) {
                    String key = (String) tag.get("key");
                    String value = (String) tag.get("value");

                    if (tagsStringBuilder.length() > 0) {
                        tagsStringBuilder.append(", ");
                    }
                    tagsStringBuilder.append(key).append("=").append(value); // "key=value" 형태로 만듦
                }
                String tags = tagsStringBuilder.toString(); // 변환된 tags 문자열

                // 진행 상태를 클라이언트에 전송 (시작)
                emitter.send("Starting replication instance creation...");

                // 스토리지 값이 String으로 되어 있으므로, int로 변환
                int storage = Integer.parseInt(request.getStorage());

                // Replication instance 생성에 필요한 파라미터들
                Map<String, String> replicationInstanceParams = Map.of(
                        "instanceName", request.getInstanceName(),
                        "instanceClass", request.getInstanceClass(),
                        "engineVersion", request.getEngineVersion(),
                        "highAvailability", String.valueOf(request.isHighAvailability()),
                        "storage", String.valueOf(storage),
                        "vpc", request.getVpc(),
                        "subnetGroup", request.getSubnetGroup(),
                        "publicAccessible", String.valueOf(request.isPublicAccessible()),
                        "tags", tags,
                        "description", request.getDescription()
                );

                // Terraform 명령 실행 및 결과를 실시간으로 클라이언트에 전달
                String result = replicationInstanceService.createReplicationInstance(replicationInstanceParams);

                // Terraform 실행 후 결과 전달 (성공 메시지)
                emitter.send("Replication instance creation completed.");
                emitter.send(result); // 명령 실행 결과를 전달

                emitter.complete(); // 작업 완료
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e); // 에러 발생 시 에러로 종료
            } catch (NumberFormatException e) {
                try {
                    emitter.send("Invalid storage value: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e); // 에러 발생 시 에러로 종료
            }
        }).start();
        return emitter;
    }

    // DMS 태스크 생성
    @PostMapping("/create-dms-task")
    public SseEmitter createDmsTask(@RequestBody DmsTaskRequest request) {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                StringBuilder tagsStringBuilder = new StringBuilder();
                for (Map<String, Object> tag : request.getTags()) {
                    String key = (String) tag.get("key");
                    String value = (String) tag.get("value");

                    if (tagsStringBuilder.length() > 0) {
                        tagsStringBuilder.append(", ");
                    }
                    tagsStringBuilder.append(key).append("=").append(value); // "key=value" 형태로 만듦
                }
                String tags = tagsStringBuilder.toString(); // 변환된 tags 문자열

                // 진행 상태를 클라이언트에 전송 (시작)
                emitter.send("Starting DMS task creation...");

                // getMaxLobSize()가 String일 경우 int로 변환
                int maxLobSize = Integer.parseInt(request.getMaxLobSize());

                // DMS 작업에 필요한 파라미터들
                Map<String, String> dmsTaskParams = new HashMap<>();
                dmsTaskParams.put("taskName", request.getTaskName());
                dmsTaskParams.put("migrationType", request.getMigrationType());
                dmsTaskParams.put("targetTablePreparationMode", request.getTargetTablePreparationMode());
                dmsTaskParams.put("lobColumnSettings", request.getLobColumnSettings());
                dmsTaskParams.put("maxLobSize", String.valueOf(maxLobSize));  // maxLobSize는 int로 변환
                dmsTaskParams.put("taskLogs", String.valueOf(request.isTaskLogs())); // boolean을 String으로 변환
                dmsTaskParams.put("dataValidation", String.valueOf(request.getDataValidation()));  // boolean을 String으로 변환
                dmsTaskParams.put("startTaskOnCreation", request.getStartTaskOnCreation());
                dmsTaskParams.put("tags", tags);
                dmsTaskParams.put("tableMappings", request.getTableMappings());
                dmsTaskParams.put("sourceEndpointArn", request.getSource_endpoint_arn());
                dmsTaskParams.put("targetEndpointArn", request.getTarget_endpoint_arn());
                dmsTaskParams.put("replicationInstanceArn", request.getReplication_instance_arn());

                // Terraform 명령 실행 및 결과를 실시간으로 클라이언트에 전달
                String result = dmsService.createDmsTask(dmsTaskParams);

                // Terraform 실행 후 결과 전달 (성공 메시지)
                emitter.send("DMS task creation completed.");
                emitter.send(result); // 명령 실행 결과를 전달

                emitter.complete(); // 작업 완료
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e); // 에러 발생 시 에러로 종료
            } catch (NumberFormatException e) {
                try {
                    emitter.send("Invalid maxLobSize value: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                emitter.completeWithError(e); // 에러 발생 시 에러로 종료
            }
        }).start();
        return emitter;
    }

    @GetMapping("/sourceEndpoints")
    public List<Map<String, String>> getSourceEndpoints(@RequestParam String region) {
        // 주어진 리전에서 소스 엔드포인트 조회
        return sourceEndpointService.getSourceEndpoints(region);
    }

    @GetMapping("/targetEndpoints")
    public List<Map<String, String>> getTargetEndpoints(@RequestParam String region) {
        // 주어진 리전에서 소스 엔드포인트 조회
        return targetEndpointService.getTargetEndpoints(region);
    }

    @GetMapping("/RIs")
    public List<Map<String, String>> getRIs(@RequestParam String region) {
        // 주어진 리전에서 소스 엔드포인트 조회
        return replicationInstanceService.getRIs(region);
    }

    // 서버에서 VPC와 Security Group을 Map 형식으로 묶어서 반환
    @GetMapping("/vpc")
    public ResponseEntity<List<Map<String, String>>> getVpcListWithSecurityGroups() {
        System.out.println("HERE");
        List<Map<String, String>> vpcsWithSecurityGroups = replicationInstanceService.getVpcListWithSecurityGroups();

        if (vpcsWithSecurityGroups.isEmpty()) {
            return ResponseEntity.noContent().build(); // 데이터가 없으면 204 No Content 응답
        }

        return ResponseEntity.ok(vpcsWithSecurityGroups); // VPC와 보안 그룹을 함께 반환
    }

    @GetMapping("/subnet")
    public List<String> getSubnet() {
        return replicationInstanceService.getSubnet();
    }

    @GetMapping("/tasks")
    public List<Map<String, String>> getTasks(@RequestParam String region) {
        return dmsService.getTasks(region);
    }

    @PostMapping("/start-task")
    public ResponseEntity<String> startTask(@RequestBody TaskStartRequest request) {
        try {
            System.out.println(request);
            dmsService.startReplicationTaskWithConnectionCheck(
                    request.getArn(),
                    request.getReplicationInstanceArn(),
                    request.getSourceEndpointArn(),
                    request.getTargetEndpointArn()
            );
            return ResponseEntity.ok("작업이 성공적으로 시작되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("오류: " + e.getMessage());
        }
    }

}
