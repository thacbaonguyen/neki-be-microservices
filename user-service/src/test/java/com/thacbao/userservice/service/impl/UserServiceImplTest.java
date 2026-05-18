package com.thacbao.userservice.service.impl;

import com.thacbao.common.event.PasswordResetEvent;
import com.thacbao.common.event.UserRegisteredEvent;
import com.thacbao.common.exception.*;
import com.thacbao.userservice.config.JwtUtils;
import com.thacbao.userservice.dto.request.ChangePasswordRequest;
import com.thacbao.userservice.dto.request.UserLoginRequest;
import com.thacbao.userservice.dto.request.UserRegisterRequest;
import com.thacbao.userservice.dto.request.UserUpdateRequest;
import com.thacbao.userservice.dto.response.TokenResponse;
import com.thacbao.userservice.dto.response.UserResponseDTO;
import com.thacbao.userservice.model.RefreshToken;
import com.thacbao.userservice.model.Role;
import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.RoleRepository;
import com.thacbao.userservice.repository.UserRepository;
import com.thacbao.userservice.security.UserPrincipal;
import com.thacbao.userservice.service.TokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenService tokenService;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;

    private static User buildUser(Integer id, String email, String passwordHash, String fullName,
                                   String phone, boolean isActive, boolean emailVerified,
                                   String provider, Set<Role> roles) {
        User user = User.builder()
                .email(email).passwordHash(passwordHash).fullName(fullName)
                .phone(phone).isActive(isActive).emailVerified(emailVerified)
                .provider(provider).roles(roles).build();
        user.setId(id);
        return user;
    }

    private static Role buildRole(Integer id, String name) {
        Role role = Role.builder().name(name).build();
        role.setId(id);
        return role;
    }

    private static RefreshToken buildRefreshToken(String token, User user) {
        RefreshToken rt = RefreshToken.builder().token(token).user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)).isRevoked(false).build();
        return rt;
    }

    @BeforeEach
    void setUp() {
        userRole = buildRole(1, "USER");
        testUser = buildUser(1, "test@test.com", "encoded", "Test User",
                "0123456789", true, true, "LOCAL", new HashSet<>(Set.of(userRole)));
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(Integer userId, String... roles) {
        Set<Role> roleSet = new HashSet<>();
        for (String r : roles) {
            roleSet.add(buildRole(null, r));
        }
        User user = buildUser(userId, "test@test.com", "enc", "Test", null,
                true, true, "LOCAL", roleSet);
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void register_success() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("pass123");
        request.setConfirmPassword("pass123");
        request.setFullName("New User");
        request.setProvider("LOCAL");

        when(userRepository.findByEmailSimple("new@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.register(request);

        verify(userRepository).save(any(User.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(UserRegisteredEvent.class));
    }

    @Test
    void register_emailExists_verified_throws() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass123");
        request.setConfirmPassword("pass123");

        when(userRepository.findByEmailSimple("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(AlreadyException.class, () -> userService.register(request));
    }

    @Test
    void register_emailExists_notVerified_deletesAndCreates() {
        User unverified = buildUser(2, "test@test.com", "enc", "User", null,
                true, false, "LOCAL", new HashSet<>());
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass123");
        request.setConfirmPassword("pass123");
        request.setFullName("User");
        request.setProvider("LOCAL");

        when(userRepository.findByEmailSimple("test@test.com")).thenReturn(Optional.of(unverified));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.register(request);

        verify(userRepository).deleteByIdDirect(2);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_phoneExists_throws() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@test.com");
        request.setPhone("0123456789");
        request.setPassword("pass123");
        request.setConfirmPassword("pass123");

        when(userRepository.findByEmailSimple("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByPhone("0123456789")).thenReturn(true);

        assertThrows(AlreadyException.class, () -> userService.register(request));
    }

    @Test
    void register_passwordMismatch_throws() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("pass123");
        request.setConfirmPassword("different");

        when(userRepository.findByEmailSimple("new@test.com")).thenReturn(Optional.empty());

        assertThrows(InvalidException.class, () -> userService.register(request));
    }

    @Test
    void login_success() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(eq("test@test.com"), anyMap())).thenReturn("access-token");
        RefreshToken refreshToken = buildRefreshToken("refresh-token", testUser);
        when(tokenService.createRefreshToken(any(), anyString(), anyString())).thenReturn(refreshToken);

        TokenResponse response = userService.login(request, "Chrome", "127.0.0.1");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void login_badCredentials_throws() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(InvalidException.class, () -> userService.login(request, "Chrome", "127.0.0.1"));
    }

    @Test
    void login_accountBlocked_throws() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass123");
        testUser.setIsActive(false);

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.login(request, "Chrome", "127.0.0.1"));
    }

    @Test
    void login_emailNotVerified_throws() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass123");
        testUser.setEmailVerified(false);

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.login(request, "Chrome", "127.0.0.1"));
    }

    @Test
    void getProfile_success() {
        setSecurityContext(1, "USER");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        UserResponseDTO result = userService.getProfile();

        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void getProfile_notLoggedIn_throws() {
        assertThrows(InvalidException.class, () -> userService.getProfile());
    }

    @Test
    void updateProfile_success() {
        setSecurityContext(1, "USER");
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("Updated Name");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        userService.updateProfile(request);

        assertEquals("Updated Name", testUser.getFullName());
    }

    @Test
    void updateProfile_phoneExists_otherUser_throws() {
        setSecurityContext(1, "USER");
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPhone("0987654321");

        User otherUser = buildUser(2, "other@test.com", "enc", "Other", "0987654321",
                true, true, "LOCAL", new HashSet<>());
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.findByPhone("0987654321")).thenReturn(Optional.of(otherUser));

        assertThrows(AlreadyException.class, () -> userService.updateProfile(request));
    }

    @Test
    void changePassword_success() {
        setSecurityContext(1, "USER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old");
        request.setNewPassword("new123");
        request.setConfirmPassword("new123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("old", "encoded")).thenReturn(true);
        when(passwordEncoder.matches("new123", "encoded")).thenReturn(false);
        when(passwordEncoder.encode("new123")).thenReturn("newEncoded");

        userService.changePassword(request);

        verify(userRepository).save(testUser);
        verify(tokenService).revokeAllUserTokens(1);
    }

    @Test
    void changePassword_wrongOld_throws() {
        setSecurityContext(1, "USER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidException.class, () -> userService.changePassword(request));
    }

    @Test
    void changePassword_mismatch_throws() {
        setSecurityContext(1, "USER");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old");
        request.setNewPassword("new1");
        request.setConfirmPassword("new2");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("old", "encoded")).thenReturn(true);

        assertThrows(InvalidException.class, () -> userService.changePassword(request));
    }

    @Test
    void getActiveUsers_returnsPage() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAllActiveUsers(any(Pageable.class))).thenReturn(page);

        Page<UserResponseDTO> result = userService.getActiveUsers(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBlockedUsers_returnsPage() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAllBlockedUsers(any(Pageable.class))).thenReturn(page);

        Page<UserResponseDTO> result = userService.getBlockedUsers(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUsersByRole_success() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findUsersByRole(eq("USER"), any())).thenReturn(page);

        Page<UserResponseDTO> result = userService.getUsersByRole("user", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUsersByRole_roleNotFound_throws() {
        when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUsersByRole("invalid", Pageable.unpaged()));
    }

    @Test
    void getUserById_admin_success() {
        setSecurityContext(2, "ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        UserResponseDTO result = userService.getUserById(1);

        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void getUserById_self_success() {
        setSecurityContext(1, "USER");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        UserResponseDTO result = userService.getUserById(1);

        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void getUserById_otherUser_throws() {
        setSecurityContext(2, "USER");

        assertThrows(PermissionException.class, () -> userService.getUserById(1));
    }

    @Test
    void toggleUserBlock_block() {
        setSecurityContext(2, "ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        userService.toggleUserBlock(1, true);

        assertFalse(testUser.getIsActive());
        verify(tokenService).revokeAllUserTokens(1);
    }

    @Test
    void toggleUserBlock_unblock() {
        setSecurityContext(2, "ADMIN");
        testUser.setIsActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        userService.toggleUserBlock(1, false);

        assertTrue(testUser.getIsActive());
        verify(tokenService, never()).revokeAllUserTokens(anyInt());
    }

    @Test
    void toggleUserBlock_self_throws() {
        setSecurityContext(1, "ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.toggleUserBlock(1, true));
    }

    @Test
    void deleteUser_success() {
        setSecurityContext(2, "ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1);

        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_self_throws() {
        setSecurityContext(1, "ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.deleteUser(1));
    }

    @Test
    void verifyAccount_success() {
        testUser.setEmailVerified(false);
        testUser.setOtp("123456");
        testUser.setOtpGenerateTime(LocalDateTime.now());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        userService.verifyAccount("test@test.com", "123456");

        assertTrue(testUser.getEmailVerified());
        assertNull(testUser.getOtp());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(UserRegisteredEvent.class));
    }

    @Test
    void verifyAccount_alreadyVerified_throws() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.verifyAccount("test@test.com", "123456"));
    }

    @Test
    void verifyAccount_wrongOtp_throws() {
        testUser.setEmailVerified(false);
        testUser.setOtp("123456");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.verifyAccount("test@test.com", "654321"));
    }

    @Test
    void verifyAccount_expiredOtp_throws() {
        testUser.setEmailVerified(false);
        testUser.setOtp("123456");
        testUser.setOtpGenerateTime(LocalDateTime.now().minusMinutes(15));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(OtpExpiredException.class, () -> userService.verifyAccount("test@test.com", "123456"));
    }

    @Test
    void regenerateOtp_success() {
        testUser.setEmailVerified(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        userService.regenerateOtp("test@test.com");

        assertNotNull(testUser.getOtp());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(UserRegisteredEvent.class));
    }

    @Test
    void regenerateOtp_alreadyVerified_throws() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.regenerateOtp("test@test.com"));
    }

    @Test
    void forgotPassword_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        userService.forgotPassword("test@test.com");

        assertNotNull(testUser.getOtp());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PasswordResetEvent.class));
    }

    @Test
    void forgotPassword_notFound_throws() {
        when(userRepository.findByEmail("nonexist@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.forgotPassword("nonexist@test.com"));
    }

    @Test
    void verifyForgotPassword_success() {
        testUser.setOtp("123456");
        testUser.setOtpGenerateTime(LocalDateTime.now());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(eq("test@test.com"), anyMap())).thenReturn("reset-token");

        String token = userService.verifyForgotPassword("test@test.com", "123456");

        assertEquals("reset-token", token);
        assertNull(testUser.getOtp());
    }

    @Test
    void verifyForgotPassword_wrongOtp_throws() {
        testUser.setOtp("123456");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidException.class, () -> userService.verifyForgotPassword("test@test.com", "000000"));
    }

    @Test
    void setPassword_success() {
        when(jwtUtils.getUsernameFromToken("token")).thenReturn("test@test.com");
        Claims claims = mock(Claims.class);
        when(claims.get("type")).thenReturn("password_reset");
        when(jwtUtils.getClaimsFromToken("token")).thenReturn(claims);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpass")).thenReturn("newEncoded");

        userService.setPassword("token", "newpass", "newpass");

        verify(userRepository).save(testUser);
        verify(tokenService).revokeAllUserTokens(1);
    }

    @Test
    void setPassword_mismatch_throws() {
        assertThrows(InvalidException.class, () -> userService.setPassword("token", "pass1", "pass2"));
    }

    @Test
    void setPassword_invalidTokenType_throws() {
        when(jwtUtils.getUsernameFromToken("token")).thenReturn("test@test.com");
        Claims claims = mock(Claims.class);
        when(claims.get("type")).thenReturn("access");
        when(jwtUtils.getClaimsFromToken("token")).thenReturn(claims);

        assertThrows(InvalidException.class, () -> userService.setPassword("token", "pass", "pass"));
    }

    @Test
    void refreshToken_success() {
        RefreshToken newRefreshToken = buildRefreshToken("new-refresh", testUser);
        when(tokenService.rotateRefreshToken("old-refresh", "Chrome", "127.0.0.1")).thenReturn(newRefreshToken);
        when(jwtUtils.generateToken(eq("test@test.com"), anyMap())).thenReturn("new-access");

        TokenResponse response = userService.refreshToken("old-refresh", "Chrome", "127.0.0.1");

        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
    }

    @Test
    void logout_success() {
        userService.logout("refresh-token");

        verify(tokenService).revokeRefreshToken("refresh-token");
    }

    @Test
    void countActiveVerifiedUsers() {
        when(userRepository.countActiveVerifiedUsers()).thenReturn(42L);

        assertEquals(42L, userService.countActiveVerifiedUsers());
    }

    @Test
    void filterUsers() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.filterUsers(anyString(), anyString(), anyBoolean(), any())).thenReturn(page);

        Page<UserResponseDTO> result = userService.filterUsers("test", "LOCAL", true, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }
}
