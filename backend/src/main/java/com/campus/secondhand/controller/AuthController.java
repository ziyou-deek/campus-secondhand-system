package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.LoginRequest;
import com.campus.secondhand.dto.LoginResponse;
import com.campus.secondhand.dto.RegisterRequest;
import com.campus.secondhand.dto.UserUpdateRequest;
import com.campus.secondhand.dto.PasswordChangeRequest;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.service.AuthService;
import com.campus.secondhand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok("注册成功", null);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /** 兼容别名：等同 GET /api/user/me */
    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(userService.getCurrentUser());
    }

    /** 兼容别名：等同 PUT /api/user/me */
    @PutMapping("/profile")
    public Result<User> profile(@RequestBody UserUpdateRequest request) {
        return Result.ok(userService.updateCurrentUser(request));
    }

    @PutMapping("/password")
    public Result<?> password(@Validated @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return Result.ok("密码已修改", null);
    }
}
