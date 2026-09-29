package com.campus.secondhand.service;

import com.campus.secondhand.dto.ConversationVO;
import com.campus.secondhand.dto.MessageRequest;
import com.campus.secondhand.entity.Message;

import java.util.List;

public interface MessageService {
    Message send(MessageRequest request);
    List<ConversationVO> conversations();
    List<Message> withUser(Long userId, Long productId);
    void markRead(Long userId);
}
