package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.dto.UserUpdateRequest;
import com.campus.secondhand.dto.PasswordChangeRequest;
import com.campus.secondhand.dto.PasswordResetResponse;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.SecurityUtils;
import com.campus.secondhand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String TEMP_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getCurrentUser() {
        return userMapper.selectById(SecurityUtils.getUserId());
    }

    @Override
    public User updateCurrentUser(UserUpdateRequest request) {
        User user = userMapper.selectById(SecurityUtils.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
        User user = userMapper.selectById(SecurityUtils.getUserId());
        if (user == null || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public Page<User> pageUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        qw.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(new Page<>(page, size), qw);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能禁用管理员");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public PasswordResetResponse resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if (!"USER".equals(user.getRole())) throw new BusinessException("只能重置普通用户的密码");
        StringBuilder temporary = new StringBuilder(18);
        for (int i = 0; i < 18; i++) {
            temporary.append(TEMP_PASSWORD_ALPHABET.charAt(RANDOM.nextInt(TEMP_PASSWORD_ALPHABET.length())));
        }
        User update = new User();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(temporary.toString()));
        userMapper.updateById(update);
        return new PasswordResetResponse(user.getUsername(), temporary.toString());
    }
}
