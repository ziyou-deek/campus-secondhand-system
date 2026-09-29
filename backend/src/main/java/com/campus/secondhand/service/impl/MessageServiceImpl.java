package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.dto.ConversationVO;
import com.campus.secondhand.dto.MessageRequest;
import com.campus.secondhand.entity.Message;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.MessageMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.SecurityUtils;
import com.campus.secondhand.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    public Message send(MessageRequest request) {
        Long fromId = SecurityUtils.getUserId();
        if (Objects.equals(fromId, request.getToUserId())) throw new BusinessException("不能给自己发消息");
        if (userMapper.selectById(request.getToUserId()) == null) throw new BusinessException("接收用户不存在");
        Message msg = new Message();
        msg.setFromUserId(fromId);
        msg.setToUserId(request.getToUserId());
        msg.setProductId(request.getProductId());
        msg.setContent(request.getContent());
        msg.setIsRead(0);
        messageMapper.insert(msg);
        fillExtra(List.of(msg));
        return msg;
    }

    @Override
    public List<ConversationVO> conversations() {
        Long uid = SecurityUtils.getUserId();
        List<Message> all = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, uid).or().eq(Message::getToUserId, uid))
                .orderByDesc(Message::getCreateTime));
        Map<Long, ConversationVO> map = new LinkedHashMap<>();
        for (Message m : all) {
            Long peerId = Objects.equals(m.getFromUserId(), uid) ? m.getToUserId() : m.getFromUserId();
            ConversationVO vo = map.get(peerId);
            if (vo == null) {
                vo = new ConversationVO();
                vo.setUserId(peerId);
                vo.setLastContent(m.getContent());
                vo.setLastTime(m.getCreateTime());
                vo.setProductId(m.getProductId());
                vo.setUnreadCount(0);
                map.put(peerId, vo);
            }
            if (Objects.equals(m.getToUserId(), uid) && (m.getIsRead() == null || m.getIsRead() == 0)) {
                vo.setUnreadCount(vo.getUnreadCount() + 1);
            }
        }
        if (!map.isEmpty()) {
            Map<Long, User> umap = userMapper.selectBatchIds(map.keySet()).stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
            for (ConversationVO vo : map.values()) {
                User u = umap.get(vo.getUserId());
                if (u != null) {
                    vo.setNickname(u.getNickname());
                    vo.setAvatar(u.getAvatar());
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public List<Message> withUser(Long userId, Long productId) {
        Long uid = SecurityUtils.getUserId();
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<Message>()
                .and(w -> w.and(a -> a.eq(Message::getFromUserId, uid).eq(Message::getToUserId, userId))
                        .or(b -> b.eq(Message::getFromUserId, userId).eq(Message::getToUserId, uid)));
        if (productId != null) qw.eq(Message::getProductId, productId);
        qw.orderByAsc(Message::getCreateTime);
        List<Message> list = messageMapper.selectList(qw);
        fillExtra(list);
        return list;
    }

    @Override
    public void markRead(Long userId) {
        Long uid = SecurityUtils.getUserId();
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getFromUserId, userId)
                .eq(Message::getToUserId, uid)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
    }

    private void fillExtra(List<Message> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> uids = list.stream().flatMap(m -> Stream.of(m.getFromUserId(), m.getToUserId())).collect(Collectors.toSet());
        Set<Long> pids = list.stream().map(Message::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> umap = userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Product> pmap = pids.isEmpty() ? Map.of() :
                productMapper.selectBatchIds(pids).stream().collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        for (Message m : list) {
            User f = umap.get(m.getFromUserId());
            if (f != null) m.setFromUserNickname(f.getNickname());
            User t = umap.get(m.getToUserId());
            if (t != null) m.setToUserNickname(t.getNickname());
            if (m.getProductId() != null) {
                Product p = pmap.get(m.getProductId());
                if (p != null) m.setProductTitle(p.getTitle());
            }
        }
    }
}
