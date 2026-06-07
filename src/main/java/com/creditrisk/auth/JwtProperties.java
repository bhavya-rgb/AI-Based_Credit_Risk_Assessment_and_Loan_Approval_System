package com.creditrisk.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        long accessTokenMinutes,
        long refreshTokenDays,
        String secret
) {}
