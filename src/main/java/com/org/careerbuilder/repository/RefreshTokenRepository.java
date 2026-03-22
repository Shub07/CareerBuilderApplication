package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Load active tokens for a user (used when validating refresh token hash).
     */
    List<RefreshToken> findByUser_IdAndRevokedFalseAndExpiresAtAfter(Long userId, LocalDateTime now);

    /**
     * Revoke all tokens for logout-all-devices.
     */
    long deleteByUser_Id(Long userId);
}
