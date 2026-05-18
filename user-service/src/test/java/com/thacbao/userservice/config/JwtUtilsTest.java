package com.thacbao.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final String SECRET = "nekiSecretKeyForTestingJwtToken1234";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpirationHours", 24);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    @Test
    void generateToken_returnsValidToken() {
        String token = jwtUtils.generateToken("test@test.com",
                Map.of("userId", 1, "roles", List.of("USER")));

        assertNotNull(token);
        assertEquals("test@test.com", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromToken_returnsSubject() {
        String token = Jwts.builder()
                .subject("admin@test.com")
                .signWith(getKey())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .compact();

        assertEquals("admin@test.com", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void getClaimsFromToken_returnsClaims() {
        String token = jwtUtils.generateToken("test@test.com", Map.of("userId", 42));

        Claims claims = jwtUtils.getClaimsFromToken(token);

        assertEquals("test@test.com", claims.getSubject());
        assertEquals(42, claims.get("userId", Integer.class));
    }

    @Test
    void getClaimsFromTokenExpired_returnsClaimsForExpiredToken() {
        String token = Jwts.builder()
                .subject("test@test.com")
                .claim("userId", 1)
                .signWith(getKey())
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .compact();

        Claims claims = jwtUtils.getClaimsFromTokenExpired(token);

        assertEquals("test@test.com", claims.getSubject());
    }

    @Test
    void getClaimFromToken_resolvesFunction() {
        String token = jwtUtils.generateToken("test@test.com", Map.of());

        String subject = jwtUtils.getClaimFromToken(token, Claims::getSubject);

        assertEquals("test@test.com", subject);
    }

    @Test
    void getUserNameFromTokenExpired_returnsSubject() {
        String token = Jwts.builder()
                .subject("expired@test.com")
                .signWith(getKey())
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .compact();

        assertEquals("expired@test.com", jwtUtils.getUserNameFromTokenExpired(token));
    }

    @Test
    void getExpirationDateFromToken_returnsDate() {
        String token = jwtUtils.generateToken("test@test.com", Map.of());

        Date expiration = jwtUtils.getExpirationDateFromToken(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_validTokenAndMatchingUser_returnsTrue() {
        String token = jwtUtils.generateToken("test@test.com", Map.of());
        UserDetails userDetails = new User("test@test.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertTrue(jwtUtils.validateToken(token, userDetails));
    }

    @Test
    void validateToken_differentUser_returnsFalse() {
        String token = jwtUtils.generateToken("test@test.com", Map.of());
        UserDetails userDetails = new User("other@test.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertFalse(jwtUtils.validateToken(token, userDetails));
    }

    @Test
    void validateToken_expiredToken_throwsOrReturnsFalse() {
        String token = Jwts.builder()
                .subject("test@test.com")
                .signWith(getKey())
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .compact();
        UserDetails userDetails = new User("test@test.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThrows(Exception.class, () -> jwtUtils.validateToken(token, userDetails));
    }

    @Test
    void createToken_includesClaims() {
        Map<String, Object> claims = Map.of("userId", 5, "email", "test@test.com");
        String token = jwtUtils.createToken("test@test.com", claims);

        Claims result = jwtUtils.getClaimsFromToken(token);
        assertEquals(5, result.get("userId", Integer.class));
    }
}
