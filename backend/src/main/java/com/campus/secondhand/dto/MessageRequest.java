package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MessageRequest {
    @JsonAlias({"receiverId", "peerId"})
    private Long toUserId;
    private Long productId;
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
