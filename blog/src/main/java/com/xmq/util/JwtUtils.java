package com.xmq.util;

import com.xmq.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Slf4j
public class JwtUtils {
    
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRE = 24 * 60 * 60 * 1000; // 24小时

    /**
     * 生成token
     */
    public static String generateToken(LoginUser loginUser) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE);
        
        return Jwts.builder()
                .setSubject(loginUser.getId().toString())
                .claim("username", loginUser.getUsername())
                .claim("role", loginUser.getRole())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 解析token
     */
    public static LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            LoginUser loginUser = new LoginUser();
            loginUser.setId(Long.valueOf(claims.getSubject()));
            loginUser.setUsername(claims.get("username", String.class));
            loginUser.setRole(claims.get("role", String.class));
            loginUser.setToken(token);
            
            return loginUser;
        } catch (Exception e) {
            log.error("JWT解析失败：", e);
            return null;
        }
    }

    /**
     * 验证token
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT验证失败：", e);
            return false;
        }
    }
} 