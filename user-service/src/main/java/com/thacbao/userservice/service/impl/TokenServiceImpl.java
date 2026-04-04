package com.thacbao.userservice.service.impl;

import com.thacbao.common.exception.InvalidException;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.userservice.model.RefreshToken;
import com.thacbao.userservice.model.User;
import com.thacbao.userservice.repository.RefreshTokenRepository;
import com.thacbao.userservice.repository.UserRepository;
import com.thacbao.userservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refreshToken.expirationDays:7}")
    private int refreshTokenExpirationDays;

    @Value("${jwt.refreshToken.maxDevices:5}")
    private int maxDevicesPerUser;

    @Override
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        long validTokenCount = refreshTokenRepository.countValidTokensByUser(user, LocalDateTime.now());
        if (validTokenCount >= maxDevicesPerUser) {
            List<RefreshToken> userTokens = refreshTokenRepository.findByUser(user);
            userTokens.stream()
                    .filter(RefreshToken::isValid)
                    .min((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .ifPresent(oldestToken -> {
                        oldestToken.setIsRevoked(true);
                        oldestToken.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(oldestToken);
                        log.info("Revoked oldest token for user {} due to device limit", user.getEmail());
                    });
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateRefreshToken())
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .isRevoked(false)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", user.getEmail());
        return refreshToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidException("Refresh token không hợp lệ"));

        if (!refreshToken.isValid()) {
            if (refreshToken.getIsRevoked()) {
                throw new InvalidException("Refresh token đã bị thu hồi");
            } else if (refreshToken.isExpired()) {
                throw new InvalidException("Refresh token đã hết hạn");
            }
        }
        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh token không tồn tại"));

        refreshToken.setIsRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    }

    @Override
    public void revokeAllUserTokens(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

        List<RefreshToken> userTokens = refreshTokenRepository.findByUser(user);
        userTokens.forEach(token -> {
            token.setIsRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
        });
        refreshTokenRepository.saveAll(userTokens);
        log.info("Revoked all tokens for user: {}", user.getEmail());
    }

    @Override
    public RefreshToken rotateRefreshToken(String oldToken, String deviceInfo, String ipAddress) {
        RefreshToken oldRefreshToken = verifyRefreshToken(oldToken);
        User user = oldRefreshToken.getUser();

        oldRefreshToken.setIsRevoked(true);
        oldRefreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(oldRefreshToken);

        RefreshToken newRefreshToken = createRefreshToken(user, deviceInfo, ipAddress);
        log.info("Rotated refresh token for user: {}", user.getEmail());
        return newRefreshToken;
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired and revoked tokens");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Cleanup completed");
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
}
