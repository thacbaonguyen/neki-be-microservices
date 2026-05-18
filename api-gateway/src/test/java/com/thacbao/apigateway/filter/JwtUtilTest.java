package com.thacbao.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "nekiTestSecretKeyMustBeAtLeast32Bytes!";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET);
    }

    private String generateToken(String subject, Map<String, Object> claims, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key);
        claims.forEach(builder::claim);
        return builder.compact();
    }

    @Test
    void extractAllClaims_validToken() {
        String token = generateToken("test@test.com", Map.of("userId", 1), 3600000);

        Claims claims = jwtUtil.extractAllClaims(token);

        assertEquals("test@test.com", claims.getSubject());
        assertEquals(1, claims.get("userId", Integer.class));
    }

    @Test
    void extractEmail_returnsSubject() {
        String token = generateToken("user@test.com", Map.of(), 3600000);

        assertEquals("user@test.com", jwtUtil.extractEmail(token));
    }

    @Test
    void extractUserId_returnsId() {
        String token = generateToken("user@test.com", Map.of("userId", 99), 3600000);

        assertEquals(99, jwtUtil.extractUserId(token));
    }

    @Test
    void extractRoles_returnsList() {
        String token = generateToken("user@test.com",
                Map.of("roles", List.of("ADMIN", "USER")), 3600000);

        List<String> roles = jwtUtil.extractRoles(token);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = generateToken("user@test.com", Map.of(), 3600000);

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String token = generateToken("user@test.com", Map.of(), -1000);

        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_malformed_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }

    @Test
    void isTokenValid_wrongKey_returnsFalse() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("differentKeyThatIsLongEnough12345".getBytes());
        String token = Jwts.builder().subject("test").signWith(wrongKey).compact();

        assertFalse(jwtUtil.isTokenValid(token));
    }
}
