package com.creditrisk.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] raw;
        String secret = properties.secret();
        try {
            raw = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            raw = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(padKey(raw));
    }

    public String generateAccessToken(SecurityPrincipal principal, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(principal.username())
                .claim("uid", principal.userId())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static byte[] padKey(byte[] raw) {
        if (raw.length >= 32) {
            return raw;
        }
        byte[] out = new byte[32];
        for (int i = 0; i < out.length; i++) {
            out[i] = raw[i % raw.length];
        }
        return out;
    }
}
