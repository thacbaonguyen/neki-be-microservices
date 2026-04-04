package com.thacbao.userservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.common.dto.UserDTO;
import com.thacbao.userservice.model.Role;
import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal API for inter-service communication.
 * These endpoints are NOT exposed through API Gateway.
 * Called directly by other services via Feign clients.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<UserDTO>builder().code(404).status("error")
                            .message("User not found").build());
        }

        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhone())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                .code(200).status("success").data(dto).build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<UserDTO>builder().code(404).status("error")
                            .message("User not found").build());
        }

        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhone())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                .code(200).status("success").data(dto).build());
    }
}
