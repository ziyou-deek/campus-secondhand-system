package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.UserUpdateRequest;
import com.campus.secondhand.dto.PasswordChangeRequest;
import com.campus.secondhand.dto.PasswordResetResponse;
import com.campus.secondhand.entity.User;

public interface UserService {
    User getCurrentUser();
    User updateCurrentUser(UserUpdateRequest request);
    void changePassword(PasswordChangeRequest request);
    Page<User> pageUsers(int page, int size, String keyword);
    void updateStatus(Long id, Integer status);
    PasswordResetResponse resetPassword(Long id);
}
