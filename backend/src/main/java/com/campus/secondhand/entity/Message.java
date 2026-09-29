package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Long productId;
    private String content;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String fromUserNickname;
    @TableField(exist = false)
    private String toUserNickname;
    @TableField(exist = false)
    private String productTitle;

    public Long getSenderId() { return fromUserId; }
    public Long getReceiverId() { return toUserId; }
    public Boolean getRead() { return isRead != null && isRead == 1; }
}
