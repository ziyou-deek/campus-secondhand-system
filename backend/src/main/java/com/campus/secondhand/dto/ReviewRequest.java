package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ReviewRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    @JsonAlias({"rating"})
    private Integer score;
    private String content;
}
