package com.campus.secondhand.service;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.UserUpdateRequest;
import com.campus.secondhand.dto.PasswordChangeRequest;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.LoginUser;
import com.campus.secondhand.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void profileUpdateCannotChangePassword() {
        User user = new User();
        user.setId(4L);
        user.setUsername("student");
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setPassword("old-hash");
        LoginUser principal = new LoginUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        UserMapper mapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(mapper.selectById(4L)).thenReturn(user);
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("malicious-new-password");

        new UserServiceImpl(mapper, encoder).updateCurrentUser(request);

        assertEquals("old-hash", user.getPassword());
        verifyNoInteractions(encoder);
    }

    @Test
    void passwordChangeRequiresCurrentPassword() {
        User user = new User();
        user.setId(4L);
        user.setUsername("student");
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setPassword("old-hash");
        LoginUser principal = new LoginUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        UserMapper mapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(mapper.selectById(4L)).thenReturn(user);
        when(encoder.matches("wrong", "old-hash")).thenReturn(false);
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("wrong");
        request.setNewPassword("new-password-123");

        assertThrows(BusinessException.class,
                () -> new UserServiceImpl(mapper, encoder).changePassword(request));
        verify(mapper, never()).updateById(any());
    }

    @Test
    void adminResetCreatesTemporaryPasswordAndStoresOnlyHash() {
        User user = new User();
        user.setId(8L);
        user.setUsername("student8");
        user.setRole(Constants.ROLE_USER);
        user.setPassword("old-hash");
        UserMapper mapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(mapper.selectById(8L)).thenReturn(user);
        when(encoder.encode(anyString())).thenReturn("new-hash");

        var result = new UserServiceImpl(mapper, encoder).resetPassword(8L);

        assertEquals("student8", result.getUsername());
        assertEquals(18, result.getTemporaryPassword().length());
        assertTrue(result.getTemporaryPassword().matches("[A-Za-z2-9]+"));
        verify(encoder).encode(result.getTemporaryPassword());
        ArgumentCaptor<User> update = ArgumentCaptor.forClass(User.class);
        verify(mapper).updateById(update.capture());
        assertEquals(8L, update.getValue().getId());
        assertEquals("new-hash", update.getValue().getPassword());
    }

    @Test
    void adminAccountCannotBeResetThroughUserEndpoint() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Constants.ROLE_ADMIN);
        UserMapper mapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(mapper.selectById(1L)).thenReturn(admin);
        assertThrows(BusinessException.class, () -> new UserServiceImpl(mapper, encoder).resetPassword(1L));
        verify(mapper, never()).updateById(any(User.class));
    }
}
