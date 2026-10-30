package com.community.repair.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 签发与校验。密钥来自 app.jwt.secret，生产环境必须用 JWT_SECRET 覆盖。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-minutes:720}")
    private long expireMinutes;

    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT 密钥长度不足 32 字节，拒绝启动");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generate(LoginUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("realName", user.getRealName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /**
     * 校验并解析令牌。无效或过期时返回 null，由调用方决定如何响应。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            log.debug("JWT 校验失败: {}", ex.getMessage());
            return null;
        }
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }
}
