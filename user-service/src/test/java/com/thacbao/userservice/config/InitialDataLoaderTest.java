package com.thacbao.userservice.config;

import com.thacbao.userservice.model.Role;
import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.RoleRepository;
import com.thacbao.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialDataLoaderTest {

    @Mock private RoleRepository roleRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InitialDataLoader loader;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loader, "adminEmail", "admin@neki.com");
        ReflectionTestUtils.setField(loader, "adminPassword", "admin123");
        ReflectionTestUtils.setField(loader, "adminFullName", "Admin NEKI");
        ReflectionTestUtils.setField(loader, "adminPhoneNumber", "0123456789");
    }

    @Test
    void run_createsRoles_andAdmin() throws Exception {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(Role.builder().name("ADMIN").build()));
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.existsByEmail("admin@neki.com")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        loader.run();

        verify(roleRepository, atLeast(2)).save(any(Role.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_rolesExist_skipsCreation() throws Exception {
        Role userRole = Role.builder().name("USER").build();
        Role adminRole = Role.builder().name("ADMIN").build();
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.existsByEmail("admin@neki.com")).thenReturn(true);

        loader.run();

        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_adminExists_skips() throws Exception {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(Role.builder().build()));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(Role.builder().build()));
        when(userRepository.existsByEmail("admin@neki.com")).thenReturn(true);

        loader.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
