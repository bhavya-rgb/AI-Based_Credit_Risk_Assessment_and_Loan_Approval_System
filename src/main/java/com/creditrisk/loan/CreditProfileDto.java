package com.creditrisk.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record CreditProfileDto(
        @Min(0) Integer ficoLow,
        @Min(0) Integer ficoHigh,
        @Min(0) Integer inqLast6Months,
        @Min(0) Integer delinq2Yrs,
        @Min(0) Integer openAccounts,
        @Min(0) Integer publicRecords,
        @DecimalMin("0.0") BigDecimal revolvingBalance,
        @DecimalMin("0.0") BigDecimal revolvingUtilization,
        @Min(0) Integer totalAccounts,
        @Min(0) Integer mortgageAccounts,
        @Min(0) Integer bankruptcies
) {}
