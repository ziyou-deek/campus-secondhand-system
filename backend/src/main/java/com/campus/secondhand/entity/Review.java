package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String fromUserNickname;
    @TableField(exist = false)
    private String toUserNickname;
    @TableField(exist = false)
    private String productTitle;

    public Integer getRating() { return score; }
    public String getUserName() { return fromUserNickname; }
    public Long getUserId() { return fromUserId; }
}
