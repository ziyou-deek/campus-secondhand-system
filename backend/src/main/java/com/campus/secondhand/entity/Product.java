package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private String images;
    private String conditionLevel;
    private Integer status;
    private Integer viewCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String sellerNickname;
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private Boolean favorited;

    /** 兼容前端字段名 condition */
    public String getCondition() {
        return conditionLevel;
    }

    public String getSellerName() {
        return sellerNickname;
    }

    /** 兼容前端字段名 sellerId（别名 userId） */
    public Long getSellerId() {
        return userId;
    }

    public String getCover() {
        if (images == null || images.isEmpty()) return null;
        return images.split(",")[0];
    }
}
