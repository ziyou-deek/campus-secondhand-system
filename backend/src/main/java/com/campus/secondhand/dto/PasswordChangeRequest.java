package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PasswordChangeRequest {
    @NotBlank(message = "请输入当前密码")
    private String oldPassword;
    @NotBlank(message = "请输入新密码")
    @Size(min = 8, max = 50, message = "新密码至少8位")
    private String newPassword;
}
