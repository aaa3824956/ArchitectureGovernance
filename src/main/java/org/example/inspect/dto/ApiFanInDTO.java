package org.example.inspect.dto;

import lombok.Data;

@Data
public class ApiFanInDTO {

    private Long apiId;

    private String apiName;

    private Long depCount;

    /** 调用方发布单元 ID 列表（逗号分隔） */
    private String callerUnitIds;

}
