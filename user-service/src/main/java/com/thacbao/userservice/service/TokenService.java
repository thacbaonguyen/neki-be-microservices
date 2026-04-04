package com.thacbao.userservice.service;

import com.thacbao.userservice.model.RefreshToken;
import com.thacbao.userservice.model.User;

public interface TokenService {

    RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(Integer userId);

    void cleanupExpiredTokens();

    RefreshToken rotateRefreshToken(String oldToken, String deviceInfo, String ipAddress);
}
