package com.thacbao.userservice.security;

import com.thacbao.userservice.model.Role;
import com.thacbao.userservice.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(Integer userId, String email, String... roles) {
        Set<Role> roleSet = new HashSet<>();
        for (String r : roles) {
            roleSet.add(Role.builder().name(r).build());
        }
        User user = User.builder().email(email).roles(roleSet)
                .isActive(true).emailVerified(true).passwordHash("enc").build();
        user.setId(userId);
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void getCurrentUser_authenticated() {
        setAuth(1, "test@test.com", "USER");

        UserPrincipal user = SecurityUtils.getCurrentUser();

        assertNotNull(user);
        assertEquals(1, user.getId());
    }

    @Test
    void getCurrentUser_notAuthenticated_returnsNull() {
        assertNull(SecurityUtils.getCurrentUser());
    }

    @Test
    void getCurrentUserId_returnsId() {
        setAuth(42, "test@test.com", "USER");

        assertEquals(42, SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_notAuthenticated_returnsNull() {
        assertNull(SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentEmail_returnsEmail() {
        setAuth(1, "admin@test.com", "ADMIN");

        assertEquals("admin@test.com", SecurityUtils.getCurrentEmail());
    }

    @Test
    void getCurrentEmail_notAuthenticated_returnsNull() {
        assertNull(SecurityUtils.getCurrentEmail());
    }

    @Test
    void hasRole_admin_true() {
        setAuth(1, "admin@test.com", "ADMIN");

        assertTrue(SecurityUtils.hasRole("ADMIN"));
    }

    @Test
    void hasRole_user_doesNotHaveAdmin_false() {
        setAuth(1, "user@test.com", "USER");

        assertFalse(SecurityUtils.hasRole("ADMIN"));
    }

    @Test
    void hasRole_notAuthenticated_false() {
        assertFalse(SecurityUtils.hasRole("USER"));
    }
}
