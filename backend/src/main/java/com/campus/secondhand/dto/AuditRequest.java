package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AuditRequest {
    @NotNull(message = "审核状态不能为空")
    private Integer status;
    private String reason;
}
