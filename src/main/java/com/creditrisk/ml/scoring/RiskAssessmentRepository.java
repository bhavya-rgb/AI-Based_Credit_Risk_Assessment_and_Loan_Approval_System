package com.creditrisk.ml.scoring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, Long> {
    List<RiskAssessmentEntity> findByLoanApplicationIdOrderByScoredAtDesc(Long loanApplicationId);
}
