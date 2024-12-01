package dms.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DmsTaskRequest {
    private String taskName;
    private String migrationType;
    private String targetTablePreparationMode;
    private String lobColumnSettings;
    private String maxLobSize;
    private boolean taskLogs;
    private String dataValidation;
    private String startTaskOnCreation;
    private List<Map<String, Object>> tags;
    private String tableMappings;
    private String source_endpoint_arn;
    private String target_endpoint_arn;
    private String replication_instance_arn;
}
