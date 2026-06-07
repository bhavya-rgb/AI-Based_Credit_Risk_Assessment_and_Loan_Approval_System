package com.creditrisk.loan;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanApplicationResponse(
        Long id,
        Long applicantId,
        BigDecimal loanAmount,
        Integer termMonths,
        String purpose,
        BigDecimal annualIncome,
        Integer employmentLengthYears,
        String homeOwnership,
        String verificationStatus,
        BigDecimal dti,
        BigDecimal existingDebt,
        LoanApplicationStatus applicationStatus,
        Instant submittedAt,
        CreditProfileDto creditProfile,
        Instant createdAt,
        Instant updatedAt
) {}
