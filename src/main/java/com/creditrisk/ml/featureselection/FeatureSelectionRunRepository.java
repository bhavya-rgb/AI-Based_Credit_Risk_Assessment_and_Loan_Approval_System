package com.creditrisk.ml.featureselection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureSelectionRunRepository extends JpaRepository<FeatureSelectionRunEntity, Long> {
    List<FeatureSelectionRunEntity> findByTrainingRunIdOrderByCreatedAtDesc(Long trainingRunId);
}
