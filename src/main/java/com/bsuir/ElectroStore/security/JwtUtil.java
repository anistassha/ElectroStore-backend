package com.bsuir.ElectroStore.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "secret";
    private static final long EXPIRATION = 3600_000; // 1 час

//    public String generateToken(String username, String role) {
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("role", role)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }

    // В JwtUtil.java
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // Добавляем ID пользователя
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Long getUserId(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}

