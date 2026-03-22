package org.example.inspect.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Inspection {

    private Long inspectionId;

    private String inspectionName;

    private String functionId;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
