package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderStatusRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
