package com.creditrisk.loan;

import jakarta.validation.constraints.NotNull;

public record LoanApplicationStatusUpdateRequest(@NotNull LoanApplicationStatus status) {}
