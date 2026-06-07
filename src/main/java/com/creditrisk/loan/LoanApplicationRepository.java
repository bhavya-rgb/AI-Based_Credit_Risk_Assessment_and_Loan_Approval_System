package com.creditrisk.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {
    Page<LoanApplicationEntity> findAllByApplicationStatus(LoanApplicationStatus status, Pageable pageable);
}
