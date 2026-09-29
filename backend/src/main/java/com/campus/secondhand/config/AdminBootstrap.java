package com.campus.secondhand.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:}") private String username;
    @Value("${app.bootstrap-admin.password:}") private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(username)) return;
        if (!StringUtils.hasText(password) || password.length() < 12) {
            throw new IllegalStateException("初始管理员密码至少需要 12 个字符");
        }
        Long existing = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing != null && existing > 0) return;
        User admin = new User();
        admin.setUsername(username);
        admin.setNickname("系统管理员");
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Constants.ROLE_ADMIN);
        admin.setStatus(Constants.USER_STATUS_NORMAL);
        userMapper.insert(admin);
    }
}
