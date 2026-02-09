package com.embrace.cup.zoo;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class UtilJwt {

    // 生成 token
    public static String generateToken(Map<String, Object> claims, long expireMillis, String secret) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expireMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 token
    public static Claims parseToken(String token, String secret) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
