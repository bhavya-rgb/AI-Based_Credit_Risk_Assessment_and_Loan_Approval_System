package com.creditrisk.ml.scoring;

import com.creditrisk.loan.CreditProfileSnapshotEntity;
import com.creditrisk.loan.LoanApplicationEntity;
import com.creditrisk.ml.model.MlModelEntity;

public interface RiskScorer {
    ScoringComputationResult score(MlModelEntity model, LoanApplicationEntity application, CreditProfileSnapshotEntity creditProfile);
}
