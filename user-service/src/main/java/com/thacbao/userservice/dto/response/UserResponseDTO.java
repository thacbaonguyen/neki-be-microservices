package com.thacbao.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.userservice.model.Role;
import com.thacbao.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {

    private Integer id;
    private String email;
    private String fullName;
    private String phone;
    private Boolean isActive;
    private Boolean emailVerified;
    private String provider;
    private Set<String> roles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponseDTO from(User user) {
        if (user == null) return null;

        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet())
                : Set.of();

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .provider(user.getProvider())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
