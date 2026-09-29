package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Long categoryId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String description;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    @JsonAlias({"cover"})
    private String images;
    @JsonAlias({"condition"})
    private String conditionLevel;
}
