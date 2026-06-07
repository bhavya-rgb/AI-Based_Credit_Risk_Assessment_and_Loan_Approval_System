package com.creditrisk.ml.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetIngestionJobRepository extends JpaRepository<DatasetIngestionJobEntity, Long> {
    Optional<DatasetIngestionJobEntity> findTopByOrderByStartedAtDesc();

    Optional<DatasetIngestionJobEntity> findFirstBySourcePathAndSourceFingerprintAndStatusOrderByStartedAtDesc(
            String sourcePath, String sourceFingerprint, DatasetIngestionStatus status);
}
