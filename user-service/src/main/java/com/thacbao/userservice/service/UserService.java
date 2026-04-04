package com.thacbao.userservice.service;

import com.thacbao.userservice.dto.request.ChangePasswordRequest;
import com.thacbao.userservice.dto.request.UserLoginRequest;
import com.thacbao.userservice.dto.request.UserRegisterRequest;
import com.thacbao.userservice.dto.request.UserUpdateRequest;
import com.thacbao.userservice.dto.response.TokenResponse;
import com.thacbao.userservice.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    void register(UserRegisterRequest request);

    TokenResponse login(UserLoginRequest request, String deviceInfo, String ipAddress);

    UserResponseDTO getProfile();

    void updateProfile(UserUpdateRequest request);

    void changePassword(ChangePasswordRequest request);

    Page<UserResponseDTO> getActiveUsers(Pageable pageable);

    Page<UserResponseDTO> getBlockedUsers(Pageable pageable);

    Page<UserResponseDTO> getUsersByRole(String roleName, Pageable pageable);

    UserResponseDTO getUserById(Integer userId);

    void toggleUserBlock(Integer userId, boolean block);

    void deleteUser(Integer userId);

    void verifyAccount(String email, String otp);

    void regenerateOtp(String email);

    void forgotPassword(String email);

    String verifyForgotPassword(String email, String otp);

    void setPassword(String token, String newPassword, String confirmPassword);

    TokenResponse refreshToken(String refreshToken, String deviceInfo, String ipAddress);

    void logout(String refreshToken);

    long countActiveVerifiedUsers();

    Page<UserResponseDTO> filterUsers(String keyword, String provider, Boolean isActive, Pageable pageable);
}
