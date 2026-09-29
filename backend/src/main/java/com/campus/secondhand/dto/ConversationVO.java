package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String lastContent;
    private LocalDateTime lastTime;
    private Integer unreadCount;
    private Long productId;

    @JsonProperty("id")
    public Long getId() { return userId; }

    @JsonProperty("peerId")
    public Long getPeerId() { return userId; }

    @JsonProperty("peerName")
    public String getPeerName() { return nickname; }

    @JsonProperty("peerAvatar")
    public String getPeerAvatar() { return avatar; }

    @JsonProperty("lastMessage")
    public String getLastMessage() { return lastContent; }

    @JsonProperty("unread")
    public Integer getUnread() { return unreadCount; }
}
