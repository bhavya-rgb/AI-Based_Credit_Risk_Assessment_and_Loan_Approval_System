package com.creditrisk.ml.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MlModelRepository extends JpaRepository<MlModelEntity, Long> {
    Optional<MlModelEntity> findFirstByStatusOrderByPromotedAtDescCreatedAtDesc(ModelStatus status);
    List<MlModelEntity> findAllByOrderByCreatedAtDesc();
    Optional<MlModelEntity> findByModelVersion(String modelVersion);
}
