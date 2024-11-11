package com.example.kscommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    public static String createJWT(Integer userId, Long ttlSend) {
        long expMillis = System.currentTimeMillis() + ttlSend * 1000L;
        return Jwts.builder()
                .setId(UUID.randomUUID().toString().replaceAll("-", ""))
                .setSubject(userId.toString())
                .setIssuer("ZJB")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "rukoujihua233rukoujihua233rukoujihua233rukoujihua233")
                .setNotBefore(new Date())
                .setExpiration(new Date(expMillis)).compact();
    }

    public static String createJWTNoExpiration(Integer userId) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString().replaceAll("-", ""))
                .setSubject(userId.toString())
                .setIssuer("ZJB")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "rukoujihua233rukoujihua233rukoujihua233rukoujihua233")
                .compact();
    }

    public static Claims parseJWT(String token) {
        Claims claims;
        try {
            claims = (Claims) Jwts.parserBuilder()
                    .setSigningKey("rukoujihua233rukoujihua233rukoujihua233rukoujihua233")
                    .build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }
        return claims;
    }
}

