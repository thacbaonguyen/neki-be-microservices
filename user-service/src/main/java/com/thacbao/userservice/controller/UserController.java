package com.thacbao.userservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.userservice.dto.request.*;
import com.thacbao.userservice.dto.response.TokenResponse;
import com.thacbao.userservice.dto.response.UserResponseDTO;
import com.thacbao.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Void>builder()
                .code(HttpStatus.CREATED.value()).status("success")
                .message("Vui lòng kiểm tra email để xác thực tài khoản").build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);
        TokenResponse tokens = userService.login(request, deviceInfo, ipAddress);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true).secure(false).path("/api/v1/auth")
                .maxAge(Duration.ofDays(7)).sameSite("Strict").build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        TokenResponse safeResponse = TokenResponse.builder()
                .accessToken(tokens.getAccessToken()).tokenType(tokens.getTokenType())
                .expiresIn(tokens.getExpiresIn()).user(tokens.getUser()).build();

        return ResponseEntity.ok(ApiResponse.<TokenResponse>builder()
                .code(200).status("success").message("Đăng nhập thành công").data(safeResponse).build());
    }

    @PostMapping("/verify-account")
    public ResponseEntity<ApiResponse<Void>> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        userService.verifyAccount(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Xác thực email thành công").build());
    }

    @PostMapping("/regenerate-otp")
    public ResponseEntity<ApiResponse<Void>> regenerateOtp(
            @RequestParam @Email(message = "Email không hợp lệ") String email) {
        userService.regenerateOtp(email);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("OTP mới đã được gửi đến email của bạn").build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn").build());
    }

    @PostMapping("/verify-forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyForgotPassword(
            @Valid @RequestBody VerifyAccountRequest request) {
        String token = userService.verifyForgotPassword(request.getEmail(), request.getOtp());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder().code(200).status("success").message("Xác thực OTP thành công").data(tokenMap).build());
    }

    @PostMapping("/set-password/{token}")
    public ResponseEntity<ApiResponse<Void>> setPassword(
            @PathVariable String token, @Valid @RequestBody SetPasswordRequest request) {
        userService.setPassword(token, request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Đặt lại mật khẩu thành công").build());
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile() {
        UserResponseDTO userDto = userService.getProfile();
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder().code(200).status("success").message("Lấy thông tin người dùng thành công").data(userDto).build());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Cập nhật thông tin thành công").build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Đổi mật khẩu thành công").build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.<TokenResponse>builder()
                    .code(401).status("error").message("Refresh token không tồn tại").build());
        }

        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);
        TokenResponse tokens = userService.refreshToken(refreshToken, deviceInfo, ipAddress);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true).secure(false).path("/api/v1/auth")
                .maxAge(Duration.ofDays(7)).sameSite("Strict").build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        TokenResponse safeResponse = TokenResponse.builder()
                .accessToken(tokens.getAccessToken()).tokenType(tokens.getTokenType())
                .expiresIn(tokens.getExpiresIn()).build();

        return ResponseEntity.ok(ApiResponse.<TokenResponse>builder().code(200).status("success")
                .message("Token đã được làm mới").data(safeResponse).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestParam @NotBlank(message = "Refresh token không được để trống") String refreshToken) {
        userService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Đăng xuất thành công").build());
    }

    // Admin endpoints
    @GetMapping("/users/filter")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> filterUsers(
            @ModelAttribute UserFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<UserResponseDTO>>builder().code(200).status("success")
                .data(userService.filterUsers(filter.getKeyword(), filter.getProvider(), filter.getIsActive(), pageable)).build());
    }

    @GetMapping("/users/active")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getActiveUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<UserResponseDTO>>builder().code(200).status("success")
                .data(userService.getActiveUsers(pageable)).build());
    }

    @GetMapping("/users/blocked")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getBlockedUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<UserResponseDTO>>builder().code(200).status("success")
                .data(userService.getBlockedUsers(pageable)).build());
    }

    @GetMapping("/users/role/{roleName}")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getUsersByRole(
            @PathVariable String roleName,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<UserResponseDTO>>builder().code(200).status("success")
                .data(userService.getUsersByRole(roleName, pageable)).build());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Integer userId) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder().code(200).status("success")
                .data(userService.getUserById(userId)).build());
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable Integer userId) {
        userService.toggleUserBlock(userId, true);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Khóa người dùng thành công").build());
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Integer userId) {
        userService.toggleUserBlock(userId, false);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Mở khóa người dùng thành công").build());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).status("success").message("Xóa người dùng thành công").build());
    }

    @GetMapping("/users/stats/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUserCount() {
        long count = userService.countActiveVerifiedUsers();
        Map<String, Integer> map = new HashMap<>();
        map.put("count", (int) count);
        return ResponseEntity.ok(ApiResponse.<Map<String, Integer>>builder().code(200).status("success").data(map).build());
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.<String>builder().code(200).status("success")
                .message("API is working!").data("User Service v1.0 - Microservice").build());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) return xForwardedFor.split(",")[0].trim();
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) return xRealIp;
        return request.getRemoteAddr();
    }
}
