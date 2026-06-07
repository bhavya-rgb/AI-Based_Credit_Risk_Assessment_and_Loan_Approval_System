package com.creditrisk.decision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDecisionRepository extends JpaRepository<LoanDecisionEntity, Long> {
    List<LoanDecisionEntity> findByLoanApplicationIdOrderByDecidedAtDesc(Long loanApplicationId);
}
