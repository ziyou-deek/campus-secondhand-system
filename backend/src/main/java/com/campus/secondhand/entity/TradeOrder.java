package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal price;
    private Integer status;
    private Boolean buyerConfirmed;
    private Boolean sellerConfirmed;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productTitle;
    @TableField(exist = false)
    private String buyerNickname;
    @TableField(exist = false)
    private String sellerNickname;
    @TableField(exist = false)
    private String productImages;

    public String getBuyerName() { return buyerNickname; }
    public String getSellerName() { return sellerNickname; }
    public String getProductCover() {
        if (productImages == null || productImages.isEmpty()) return null;
        return productImages.split(",")[0];
    }
}
