package com.thacbao.userservice.config;

import com.thacbao.userservice.model.Role;
import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.RoleRepository;
import com.thacbao.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class InitialDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.email:admin@neki.com}")
    private String adminEmail;

    @Value("${app.init.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.init.admin.fullName:Admin NEKI}")
    private String adminFullName;

    @Value("${app.init.admin.phoneNumber:0123456789}")
    private String adminPhoneNumber;

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("USER", "Người dùng");
        createRoleIfNotExists("ADMIN", "Quản trị viên");

        createAdminUserIfNotExists();
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Da tao role: {}", name);
        } else {
            log.debug("Role {} da ton tai", name);
        }
    }

    private void createAdminUserIfNotExists() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("User admin: {} da ton tai", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Chua ton tai role ADMIN"));

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .phone(adminPhoneNumber)
                .isActive(true)
                .emailVerified(true)
                .provider("LOCAL")
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
        log.warn("Email    : {}", adminEmail);
        log.warn("Password : {}", adminPassword);
        log.warn("Doi password sau khi dang nhap");
    }
}
