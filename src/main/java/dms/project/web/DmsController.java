package dms.project.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dms.project.dto.*;
import dms.project.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/dms")
@RequiredArgsConstructor
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

    @Autowired
    private final SseEmitter emitter;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @PostMapping("/create-source-endpoint")
    public ResponseEntity<String> createSourceEndpoint(@RequestBody SourceEndpointRequest request) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(clientId);

        emitters.put(clientId, emitter); // 클라이언트 ID와 Emitter 매핑
        new Thread(() -> {
            try {
                emitter.send("Starting source endpoint creation...");

                // 태그 배열을 쉼표로 구분된 문자열로 변환
                StringBuilder tagsStringBuilder = new StringBuilder();
                for (Map<String, Object> tag : request.getTags()) {
                    String key = (String) tag.get("key");
                    String value = (String) tag.get("value");

                    if (tagsStringBuilder.length() > 0) {
                        tagsStringBuilder.append(", ");
                    }
                    tagsStringBuilder.append(key).append("=").append(value);
                }
                String tags = tagsStringBuilder.toString();

                // 작업 수행 로직 (예: Terraform 명령 실행)
                Map<String, String> sourceEndpointParams = Map.of(
                        "endpointId", request.getEndpointId(),
                        "username", request.getUsername(),
                        "password", request.getPassword(),
                        "serverName", request.getServerName(),
                        "port", request.getPort(),
                        "engine", request.getEngine(),
                        "tags", tags,
                        "RI", request.getReplicationInstance()
                );

                String result = sourceEndpointService.createSourceEndpoint(sourceEndpointParams, emitter);
                emitter.send(SseEmitter.event()
                        .name("source") // 이벤트 이름 지정
                        .data(result));
                System.out.println(result);

                emitter.send("Source endpoint creation completed.");
                System.out.println("Before complete");
                emitter.complete();
                System.out.println("After complete");
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                completeEmitterWithError(clientId, e);
            }
        }).start();

        System.out.println("Before return2");
        return ResponseEntity.ok(clientId); // 클라이언트에 ID 반환
    }

    private SseEmitter createSseEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(3600000L); // 1시간 지속

        emitter.onCompletion(() -> emitters.remove(clientId)); // 연결 종료 시 Emitter 제거
        emitter.onTimeout(() -> emitters.remove(clientId)); // 타임아웃 시 Emitter 제거

        return emitter;
    }

    private void completeEmitterWithError(String clientId, Throwable ex) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            emitter.completeWithError(ex); // 에러로 완료
            emitters.remove(clientId); // 기존 Emitter 삭제
        }
        SseEmitter newEmitter = createSseEmitter(clientId); // 새로운 Emitter 생성
        emitters.put(clientId, newEmitter); // 새로운 Emitter 저장
    }


    // GET 요청: SSE로 작업 상태 스트리밍
    @GetMapping("/stream-endpoint")
    public SseEmitter streamSourceEndpoint(@RequestParam String clientId) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Emitter not found for client ID: " + clientId);
        }
        return emitter; // 클라이언트와 연결 유지
    }
    // 타겟 엔드포인트 생성
    @PostMapping("/create-target-endpoint")
    public ResponseEntity<String> createTargetEndpoint(@RequestBody TargetEndpointRequest request) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(clientId);

        emitters.put(clientId, emitter); // 클라이언트 ID와 Emitter 매핑
        new Thread(() -> {
            try {
                emitter.send("Starting target endpoint creation...");

                // 태그 배열을 쉼표로 구분된 문자열로 변환
                StringBuilder tagsStringBuilder = new StringBuilder();
                for (Map<String, Object> tag : request.getTags()) {
                    String key = (String) tag.get("key");
                    String value = (String) tag.get("value");

                    if (tagsStringBuilder.length() > 0) {
                        tagsStringBuilder.append(", ");
                    }
                    tagsStringBuilder.append(key).append("=").append(value);
                }
                String tags = tagsStringBuilder.toString();

                // 작업 수행 로직 (예: Terraform 명령 실행)
                Map<String, String> targetEndpointParams = Map.of(
                        "endpointId", request.getEndpointId(),
                        "username", request.getUsername(),
                        "password", request.getPassword(),
                        "serverName", request.getServerName(),
                        "port", request.getPort(),
                        "engine", request.getEngine(),
                        "tags", tags,
                        "RI", request.getReplicationInstance()
                );

                String result = targetEndpointService.createTargetEndpoint(targetEndpointParams, emitter);
                emitter.send(SseEmitter.event()
                        .name("target") // 이벤트 이름 지정
                        .data(result));
                System.out.println(result);

                emitter.send("Target endpoint creation completed.");
                System.out.println("Before complete");
                emitter.complete();
                System.out.println("After complete");
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                completeEmitterWithError(clientId, e);
            }
        }).start();

        return ResponseEntity.ok(clientId); // 클라이언트에 ID 반환
    }

    @PostMapping("/create-replication-instance")
    public ResponseEntity<String> createReplicationInstance(@RequestBody ReplicationInstanceRequest request) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(clientId);

        emitters.put(clientId, emitter); // 클라이언트 ID와 Emitter 매핑
        new Thread(() -> {
            try {
                emitter.send("Starting Replication Instance creation...");
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
                String result = replicationInstanceService.createReplicationInstance(replicationInstanceParams, emitter);
                emitter.send(SseEmitter.event()
                        .name("RI") // 이벤트 이름 지정
                        .data(result));
                System.out.println(result);

                emitter.send("Replication Instance creation completed.");
                System.out.println("Before complete");
                emitter.complete();
                System.out.println("After complete");
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                completeEmitterWithError(clientId, e);
            }
        }).start();

        return ResponseEntity.ok(clientId); // 클라이언트에 ID 반환
    }

    // DMS 태스크 생성
    @PostMapping("/create-dms-task")
    public ResponseEntity<String> createDmsTask(@RequestBody DmsTaskRequest request) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(clientId);

        emitters.put(clientId, emitter); // 클라이언트 ID와 Emitter 매핑
        new Thread(() -> {
            try {
                emitter.send("Starting Replication Task creation...");
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
                // getMaxLobSize()가 String일 경우 int로 변환
                int maxLobSize = Integer.parseInt(request.getMaxLobSize());

                ObjectMapper objectMapper = new ObjectMapper();

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
                dmsTaskParams.put("sourceEndpointArn", request.getSource_endpoint_arn());
                dmsTaskParams.put("targetEndpointArn", request.getTarget_endpoint_arn());
                dmsTaskParams.put("replicationInstanceArn", request.getReplication_instance_arn());

                try {
                    String tableMappingsJson = objectMapper.writeValueAsString(request.getTableMappings());
                    dmsTaskParams.put("tableMappings", tableMappingsJson);
                } catch (Exception e) {
                    e.printStackTrace();
                    dmsTaskParams.put("tableMappings", "{}"); // 예외 처리로 빈 JSON 반환
                }

                System.out.println(request.getTableMappings());

                System.out.println("HERE IN CONTROLLER");

                // Terraform 명령 실행 및 결과를 실시간으로 클라이언트에 전달
                dmsService.createDmsTask(dmsTaskParams, emitter);

                // Terraform 실행 후 결과 전달 (성공 메시지)
           //     emitter.send("Replication task completed.");
                System.out.println("Before complete");
            //    emitter.complete();
                System.out.println("After complete");
            } catch (IOException | InterruptedException e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                completeEmitterWithError(clientId, e);
            }
        }).start();

        return ResponseEntity.ok(clientId);
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
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(clientId);

        emitters.put(clientId, emitter);
        try {
            System.out.println(request);
            dmsService.startTask(request.getArn(), emitter);
      //      emitter.complete();
            return ResponseEntity.ok(clientId);
        } catch (RuntimeException e) {
            try {
                emitter.send("Error: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            completeEmitterWithError(clientId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("오류: " + e.getMessage());
        }
    }

}
