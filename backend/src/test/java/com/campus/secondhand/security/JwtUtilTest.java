package com.campus.secondhand.security;

import com.campus.secondhand.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    @Test
    void passwordChangeInvalidatesPreviouslyIssuedToken() {
        JwtUtil jwt = new JwtUtil();
        ReflectionTestUtils.setField(jwt, "secret", "course-project-test-secret-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwt, "expiration", 86400000L);
        jwt.init();
        User user = new User();
        user.setId(7L);
        user.setUsername("student7");
        user.setRole("USER");
        user.setPassword("bcrypt-old-hash");

        String oldToken = jwt.generateToken(user);
        assertTrue(jwt.matchesPasswordVersion(jwt.parse(oldToken), user.getPassword()));
        user.setPassword("bcrypt-new-hash");
        assertFalse(jwt.matchesPasswordVersion(jwt.parse(oldToken), user.getPassword()));
        String newToken = jwt.generateToken(user);
        assertTrue(jwt.matchesPasswordVersion(jwt.parse(newToken), user.getPassword()));
    }
}
