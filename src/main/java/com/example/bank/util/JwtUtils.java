package com.example.bank.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    // 预警：在生产环境，这个密钥应该放在环境变量或配置文件中，绝对不能硬编码
    private static final String SECRET_KEY = "YourSecretKeyForJimiBankProjectWhichShouldBeVeryLong";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 设置过期时间：24小时
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * 根据用户信息生成 Token
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中解析出用户名
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}