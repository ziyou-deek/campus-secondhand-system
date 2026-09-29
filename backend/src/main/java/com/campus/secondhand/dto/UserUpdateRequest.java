package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String password;
}
