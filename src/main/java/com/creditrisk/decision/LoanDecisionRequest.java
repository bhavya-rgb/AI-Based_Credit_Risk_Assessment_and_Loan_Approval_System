package com.creditrisk.decision;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoanDecisionRequest(
        @NotNull LoanDecisionStatus decisionStatus,
        @DecimalMin("0.0") BigDecimal approvedAmount,
        Integer approvedTermMonths,
        @DecimalMin("0.0") BigDecimal interestRateOffer,
        @Size(max = 1000) String overrideReason
) {}
