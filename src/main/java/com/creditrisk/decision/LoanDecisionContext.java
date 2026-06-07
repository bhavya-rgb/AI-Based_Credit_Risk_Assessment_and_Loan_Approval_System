package com.creditrisk.decision;

import com.creditrisk.loan.CreditProfileSnapshotEntity;
import com.creditrisk.loan.LoanApplicationEntity;

public record LoanDecisionContext(LoanApplicationEntity application, CreditProfileSnapshotEntity creditProfile) {}
