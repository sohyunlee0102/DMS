package dms.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplicationInstanceRequest {
    private String instanceName;
    private String instanceClass;
    private String engineVersion;
    private boolean highAvailability;
    private String storage;
    private String vpc;
    private String subnetGroup;
    private boolean publicAccessible;
    private List<Map<String, Object>> tags;
    private String description;

}
