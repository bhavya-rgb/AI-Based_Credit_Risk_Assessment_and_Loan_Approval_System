package com.creditrisk.decision;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanDecisionResponse(
        Long id,
        Long loanApplicationId,
        Long riskAssessmentId,
        LoanDecisionStatus decisionStatus,
        BigDecimal approvedAmount,
        Integer approvedTermMonths,
        BigDecimal interestRateOffer,
        boolean override,
        String overrideReason,
        Long decidedBy,
        Instant decidedAt
) {
    public static LoanDecisionResponse from(LoanDecisionEntity entity) {
        return new LoanDecisionResponse(
                entity.getId(),
                entity.getLoanApplication().getId(),
                entity.getRiskAssessment() == null ? null : entity.getRiskAssessment().getId(),
                entity.getDecisionStatus(),
                entity.getApprovedAmount(),
                entity.getApprovedTermMonths(),
                entity.getInterestRateOffer(),
                Boolean.TRUE.equals(entity.getOverrideFlag()),
                entity.getOverrideReason(),
                entity.getDecidedBy() == null ? null : entity.getDecidedBy().getId(),
                entity.getDecidedAt()
        );
    }
}
