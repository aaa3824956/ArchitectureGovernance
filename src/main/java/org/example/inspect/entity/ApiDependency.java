package org.example.inspect.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiDependency {
    private String apiDependencyId;
    private String fromReleaseUnitId;
    private String fromApiId;
    private String toApiId;
    private String toReleaseUnitId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
