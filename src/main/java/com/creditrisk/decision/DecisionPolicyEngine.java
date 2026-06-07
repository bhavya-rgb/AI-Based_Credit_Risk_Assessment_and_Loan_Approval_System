package com.creditrisk.decision;

import java.math.BigDecimal;

public interface DecisionPolicyEngine {
    PolicyDecision evaluate(BigDecimal defaultProbability, LoanDecisionContext context);
}
