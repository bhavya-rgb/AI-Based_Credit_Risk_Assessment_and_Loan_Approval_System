package com.creditrisk.ml.training;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRunRepository extends JpaRepository<TrainingRunEntity, Long> {
}
