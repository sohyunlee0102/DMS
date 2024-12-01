package dms.project.dto;

import lombok.Data;

@Data
public class TaskStartRequest {

    private String arn;
    private String replicationInstanceArn;
    private String sourceEndpointArn;
    private String targetEndpointArn;

}