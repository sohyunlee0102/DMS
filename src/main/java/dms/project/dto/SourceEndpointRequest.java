package dms.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceEndpointRequest {
    private String endpointId;
    private String username;
    private String password;
    private String serverName;
    private String port;
    private String engine;
    private List<Map<String, Object>> tags;

    @JsonProperty("ReplicationInstance")  // JSON 필드 이름과 정확히 일치하도록
    private String ReplicationInstance;

    // getters and setters
}
