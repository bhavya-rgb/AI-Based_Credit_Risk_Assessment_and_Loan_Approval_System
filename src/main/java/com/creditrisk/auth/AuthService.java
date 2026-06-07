package com.creditrisk.auth;

import com.creditrisk.common.ApiException;
import com.creditrisk.user.RefreshTokenEntity;
import com.creditrisk.user.RefreshTokenRepository;
import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import com.creditrisk.user.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       JwtProperties jwtProperties,
                       TokenHashService tokenHashService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenHashService = tokenHashService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User inactive");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        String hash = tokenHashService.sha256(request.refreshToken());
        RefreshTokenEntity token = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (token.isExpired() || token.isRevoked()) {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
        return issueTokens(token.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String hash = tokenHashService.sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    public MeResponse me(SecurityPrincipal principal) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        List<String> roles = user.getRoles().stream().map(r -> r.getName().name()).toList();
        return new MeResponse(user.getId(), user.getEmail(), user.getFullName(), roles);
    }

    public UserEntity ensureSeedAdmin(String email, String fullName, String rawPassword) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            UserEntity user = new UserEntity();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });
    }

    private AuthTokenResponse issueTokens(UserEntity user) {
        List<String> roles = user.getRoles().stream().map(r -> r.getName().name()).toList();
        SecurityPrincipal principal = new SecurityPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name())).toList()
        );
        String access = jwtService.generateAccessToken(principal, roles);
        String refresh = generateOpaqueToken();
        RefreshTokenEntity refreshEntity = new RefreshTokenEntity();
        refreshEntity.setUser(user);
        refreshEntity.setTokenHash(tokenHashService.sha256(refresh));
        refreshEntity.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshEntity);
        return new AuthTokenResponse(access, refresh, jwtProperties.accessTokenMinutes() * 60, roles);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
