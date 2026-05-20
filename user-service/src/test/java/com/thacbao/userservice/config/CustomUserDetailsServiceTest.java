package com.thacbao.userservice.config;

import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    private User buildUser(Integer id, String email) {
        User user = User.builder().email(email).passwordHash("enc")
                .isActive(true).emailVerified(true).roles(new HashSet<>()).build();
        user.setId(id);
        return user;
    }

    @Test
    void loadUserByUsername_found() {
        User user = buildUser(1, "test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("no@test.com"));
    }

    @Test
    void loadUserEntityByEmail_found() {
        User user = buildUser(1, "test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = service.loadUserEntityByEmail("test@test.com");

        assertEquals(1, result.getId());
    }

    @Test
    void loadUserEntityByEmail_notFound_throws() {
        when(userRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserEntityByEmail("no@test.com"));
    }

    @Test
    void loadUserEntityById_found() {
        User user = buildUser(1, "test@test.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = service.loadUserEntityById(1);

        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void loadUserEntityById_notFound_throws() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserEntityById(999));
    }
}
