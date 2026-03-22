package org.example.inspect.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InspectionIssue {

    private Long issueId;

    private Long inspectionId;

    private Long jobId;

    private String objectType;

    private Long objectId;

    private String severity;

    private String description;

}
