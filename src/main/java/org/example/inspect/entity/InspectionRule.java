package org.example.inspect.entity;

import lombok.Data;

@Data
public class InspectionRule {
    private Long id;

    private Long inspectionId;

    private Long ruleId;

    private String ruleName;

    private String description;
}
