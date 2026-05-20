package com.thacbao.userservice.service.impl;

import com.thacbao.common.exception.InvalidException;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.userservice.model.RefreshToken;
import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.RefreshTokenRepository;
import com.thacbao.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User testUser;

    private static User buildUser(Integer id, String email) {
        User user = User.builder().email(email).build();
        user.setId(id);
        return user;
    }

    private static RefreshToken buildRefreshToken(String token, User user, boolean revoked,
                                                   LocalDateTime expiresAt, LocalDateTime createdAt) {
        RefreshToken rt = RefreshToken.builder()
                .token(token).user(user).isRevoked(revoked).expiresAt(expiresAt).build();
        rt.setCreatedAt(createdAt);
        return rt;
    }

    @BeforeEach
    void setUp() {
        testUser = buildUser(1, "test@test.com");
        ReflectionTestUtils.setField(tokenService, "refreshTokenExpirationDays", 7);
        ReflectionTestUtils.setField(tokenService, "maxDevicesPerUser", 5);
    }

    @Test
    void createRefreshToken_success() {
        when(refreshTokenRepository.countValidTokensByUser(eq(testUser), any())).thenReturn(0L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = tokenService.createRefreshToken(testUser, "Chrome", "127.0.0.1");

        assertNotNull(result.getToken());
        assertEquals(testUser, result.getUser());
        assertFalse(result.getIsRevoked());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_exceedsMaxDevices_revokesOldest() {
        when(refreshTokenRepository.countValidTokensByUser(eq(testUser), any())).thenReturn(5L);

        RefreshToken oldest = buildRefreshToken("old", testUser, false,
                LocalDateTime.now().plusDays(7), LocalDateTime.now().minusDays(6));
        RefreshToken newer = buildRefreshToken("new", testUser, false,
                LocalDateTime.now().plusDays(7), LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByUser(testUser)).thenReturn(new ArrayList<>(List.of(oldest, newer)));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenService.createRefreshToken(testUser, "Chrome", "127.0.0.1");

        assertTrue(oldest.getIsRevoked());
    }

    @Test
    void verifyRefreshToken_valid() {
        RefreshToken token = buildRefreshToken("valid", testUser, false,
                LocalDateTime.now().plusDays(1), LocalDateTime.now());
        when(refreshTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));

        RefreshToken result = tokenService.verifyRefreshToken("valid");

        assertEquals("valid", result.getToken());
    }

    @Test
    void verifyRefreshToken_revoked_throws() {
        RefreshToken token = buildRefreshToken("revoked", testUser, true,
                LocalDateTime.now().plusDays(1), LocalDateTime.now());
        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(token));

        assertThrows(InvalidException.class, () -> tokenService.verifyRefreshToken("revoked"));
    }

    @Test
    void verifyRefreshToken_expired_throws() {
        RefreshToken token = buildRefreshToken("expired", testUser, false,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(8));
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThrows(InvalidException.class, () -> tokenService.verifyRefreshToken("expired"));
    }

    @Test
    void verifyRefreshToken_notFound_throws() {
        when(refreshTokenRepository.findByToken("nonexist")).thenReturn(Optional.empty());

        assertThrows(InvalidException.class, () -> tokenService.verifyRefreshToken("nonexist"));
    }

    @Test
    void revokeRefreshToken_success() {
        RefreshToken token = buildRefreshToken("token", testUser, false,
                LocalDateTime.now().plusDays(1), LocalDateTime.now());
        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        tokenService.revokeRefreshToken("token");

        assertTrue(token.getIsRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revokeRefreshToken_notFound_throws() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tokenService.revokeRefreshToken("unknown"));
    }

    @Test
    void revokeAllUserTokens_success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        RefreshToken t1 = buildRefreshToken("t1", testUser, false,
                LocalDateTime.now().plusDays(1), LocalDateTime.now());
        RefreshToken t2 = buildRefreshToken("t2", testUser, false,
                LocalDateTime.now().plusDays(1), LocalDateTime.now());
        when(refreshTokenRepository.findByUser(testUser)).thenReturn(List.of(t1, t2));

        tokenService.revokeAllUserTokens(1);

        assertTrue(t1.getIsRevoked());
        assertTrue(t2.getIsRevoked());
        verify(refreshTokenRepository).saveAll(anyList());
    }

    @Test
    void revokeAllUserTokens_userNotFound_throws() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tokenService.revokeAllUserTokens(999));
    }

    @Test
    void rotateRefreshToken_success() {
        RefreshToken oldToken = buildRefreshToken("old", testUser, false,
                LocalDateTime.now().plusDays(1), LocalDateTime.now());
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.countValidTokensByUser(eq(testUser), any())).thenReturn(0L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = tokenService.rotateRefreshToken("old", "Chrome", "127.0.0.1");

        assertTrue(oldToken.getIsRevoked());
        assertNotNull(result);
        assertNotEquals("old", result.getToken());
    }

    @Test
    void cleanupExpiredTokens() {
        tokenService.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteExpiredAndRevokedTokens(any(LocalDateTime.class));
    }
}
