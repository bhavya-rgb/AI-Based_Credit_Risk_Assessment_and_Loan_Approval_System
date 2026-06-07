package com.creditrisk.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanApplicationCreateRequest(
        @NotNull Long applicantId,
        @NotNull @DecimalMin("1.0") BigDecimal loanAmount,
        @NotNull @Min(1) Integer termMonths,
        @NotBlank String purpose,
        @NotNull @DecimalMin("0.0") BigDecimal annualIncome,
        @NotNull @Min(0) Integer employmentLengthYears,
        @NotBlank String homeOwnership,
        @NotBlank String verificationStatus,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal dti,
        @NotNull @DecimalMin("0.0") BigDecimal existingDebt,
        @Valid @NotNull CreditProfileDto creditProfile
) {}
