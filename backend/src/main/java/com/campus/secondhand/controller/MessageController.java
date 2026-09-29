package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ConversationVO;
import com.campus.secondhand.dto.MessageRequest;
import com.campus.secondhand.entity.Message;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserMapper userMapper;

    @PostMapping
    public Result<Message> send(@Validated @RequestBody MessageRequest request) {
        return Result.ok(messageService.send(request));
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        return Result.ok(messageService.conversations());
    }

    /** 前端：创建/打开与某用户的会话 */
    @PostMapping("/conversations")
    public Result<ConversationVO> start(@RequestBody Map<String, Object> body) {
        Long peerId = Long.valueOf(body.get("peerId").toString());
        for (ConversationVO vo : messageService.conversations()) {
            if (Objects.equals(peerId, vo.getUserId())) {
                return Result.ok(vo);
            }
        }
        ConversationVO vo = new ConversationVO();
        vo.setUserId(peerId);
        vo.setUnreadCount(0);
        User u = userMapper.selectById(peerId);
        if (u != null) {
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
        }
        return Result.ok(vo);
    }

    /** 前端：按会话 peerId 拉取历史并标记已读 */
    @GetMapping("/conversations/{peerId}")
    public Result<List<Message>> historyByConversation(@PathVariable Long peerId,
                                                       @RequestParam(required = false) Long productId) {
        List<Message> list = messageService.withUser(peerId, productId);
        messageService.markRead(peerId);
        return Result.ok(list);
    }

    @GetMapping("/history/{peerId}")
    public Result<List<Message>> historyAlias(@PathVariable Long peerId,
                                              @RequestParam(required = false) Long productId) {
        List<Message> list = messageService.withUser(peerId, productId);
        messageService.markRead(peerId);
        return Result.ok(list);
    }

    @GetMapping("/with/{userId}")
    public Result<List<Message>> withUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Long productId) {
        return Result.ok(messageService.withUser(userId, productId));
    }

    @PutMapping("/read/{userId}")
    public Result<?> markRead(@PathVariable Long userId) {
        messageService.markRead(userId);
        return Result.ok("已标记已读", null);
    }
}
