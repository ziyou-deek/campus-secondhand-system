package com.campus.secondhand.security;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.entity.User;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void disabledAccountCannotReuseExistingToken() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        UserDetailsServiceImpl users = mock(UserDetailsServiceImpl.class);
        FilterChain chain = mock(FilterChain.class);
        User disabled = new User();
        disabled.setId(5L);
        disabled.setUsername("disabled");
        disabled.setRole(Constants.ROLE_USER);
        disabled.setStatus(Constants.USER_STATUS_DISABLED);
        when(jwt.validate("token")).thenReturn(true);
        when(jwt.parse("token")).thenReturn(Jwts.claims().setSubject("disabled"));
        when(users.loadUserByUsername("disabled")).thenReturn(new LoginUser(disabled));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/mine");
        request.addHeader("Authorization", "Bearer token");

        new JwtAuthenticationFilter(jwt, users).doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void oldPasswordTokenCannotAuthenticate() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        UserDetailsServiceImpl users = mock(UserDetailsServiceImpl.class);
        FilterChain chain = mock(FilterChain.class);
        User user = new User();
        user.setId(6L);
        user.setUsername("reset-user");
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setPassword("new-hash");
        var claims = Jwts.claims().setSubject("reset-user");
        when(jwt.validate("old-token")).thenReturn(true);
        when(jwt.parse("old-token")).thenReturn(claims);
        when(users.loadUserByUsername("reset-user")).thenReturn(new LoginUser(user));
        when(jwt.matchesPasswordVersion(claims, "new-hash")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/mine");
        request.addHeader("Authorization", "Bearer old-token");

        new JwtAuthenticationFilter(jwt, users).doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
