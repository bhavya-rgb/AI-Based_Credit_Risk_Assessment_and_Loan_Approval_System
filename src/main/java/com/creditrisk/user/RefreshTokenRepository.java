package com.creditrisk.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHashAndRevokedAtIsNull(String tokenHash);
    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(Long userId);
    long deleteByExpiresAtBefore(Instant cutoff);
}
