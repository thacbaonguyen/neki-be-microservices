package com.thacbao.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String secretKey;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        secretKey = Base64.getEncoder().encodeToString(
                "nekiTestSecretKeyMustBeAtLeast32BytesLong!!".getBytes());
        ReflectionTestUtils.setField(jwtUtils, "secretKey", secretKey);
    }

    private String generateTestToken(String subject, Map<String, Object> claims, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key);
        claims.forEach(builder::claim);
        return builder.compact();
    }

    @Test
    void extractAllClaims_validToken_returnsClaims() {
        String token = generateTestToken("test@test.com",
                Map.of("userId", 1, "roles", List.of("USER")), 3600000);

        Claims claims = jwtUtils.extractAllClaims(token);

        assertEquals("test@test.com", claims.getSubject());
        assertEquals(1, claims.get("userId", Integer.class));
    }

    @Test
    void extractEmail_returnsSubject() {
        String token = generateTestToken("user@example.com", Map.of(), 3600000);

        assertEquals("user@example.com", jwtUtils.extractEmail(token));
    }

    @Test
    void extractUserId_returnsUserId() {
        String token = generateTestToken("test@test.com", Map.of("userId", 42), 3600000);

        assertEquals(42, jwtUtils.extractUserId(token));
    }

    @Test
    void extractRoles_returnsList() {
        String token = generateTestToken("test@test.com",
                Map.of("roles", List.of("ADMIN", "USER")), 3600000);

        List<String> roles = jwtUtils.extractRoles(token);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = generateTestToken("test@test.com", Map.of(), 3600000);

        assertTrue(jwtUtils.isTokenValid(token));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String token = generateTestToken("test@test.com", Map.of(), -1000);

        assertFalse(jwtUtils.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidSignature_returnsFalse() {
        SecretKey differentKey = Keys.hmacShaKeyFor(
                "differentSecretKeyThatIsLongEnough!!!!".getBytes());
        String token = Jwts.builder()
                .subject("test@test.com")
                .signWith(differentKey)
                .compact();

        assertFalse(jwtUtils.isTokenValid(token));
    }

    @Test
    void isTokenValid_malformedToken_returnsFalse() {
        assertFalse(jwtUtils.isTokenValid("not.a.valid.token"));
    }
}
