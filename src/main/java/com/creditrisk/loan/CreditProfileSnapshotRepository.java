package com.creditrisk.loan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditProfileSnapshotRepository extends JpaRepository<CreditProfileSnapshotEntity, Long> {
    Optional<CreditProfileSnapshotEntity> findByLoanApplicationId(Long loanApplicationId);
}
