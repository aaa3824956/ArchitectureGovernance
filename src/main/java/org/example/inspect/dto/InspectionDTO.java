package org.example.inspect.dto;

import lombok.Data;

@Data
public class InspectionDTO {
    private Long inspectionId;

    private String inspectionName;

    private String functionId;

    private String description;
}
