package com.creditrisk.auth;

import java.util.List;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        List<String> roles
) {}
