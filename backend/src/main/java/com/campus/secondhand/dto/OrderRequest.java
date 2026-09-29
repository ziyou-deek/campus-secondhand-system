package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    private String remark;
}
