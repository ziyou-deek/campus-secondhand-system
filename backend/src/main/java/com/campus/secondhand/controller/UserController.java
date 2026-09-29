package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.UserUpdateRequest;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/user/me")
    public Result<User> me() {
        return Result.ok(userService.getCurrentUser());
    }

    @PutMapping("/api/user/me")
    public Result<User> updateMe(@RequestBody UserUpdateRequest request) {
        return Result.ok(userService.updateCurrentUser(request));
    }

    @GetMapping("/api/user/profile")
    public Result<User> profile() {
        return Result.ok(userService.getCurrentUser());
    }

    @PutMapping("/api/user/profile")
    public Result<User> updateProfile(@RequestBody UserUpdateRequest request) {
        return Result.ok(userService.updateCurrentUser(request));
    }
}
