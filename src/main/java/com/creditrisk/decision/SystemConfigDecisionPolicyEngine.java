package com.creditrisk.decision;

import com.creditrisk.config.SystemConfigRepository;
import com.creditrisk.loan.CreditProfileSnapshotEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SystemConfigDecisionPolicyEngine implements DecisionPolicyEngine {
    private final SystemConfigRepository systemConfigRepository;

    public SystemConfigDecisionPolicyEngine(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDecision evaluate(BigDecimal defaultProbability, LoanDecisionContext context) {
        BigDecimal autoApprove = getThreshold("AUTO_APPROVE_PD_MAX", new BigDecimal("0.12"));
        BigDecimal manualReview = getThreshold("MANUAL_REVIEW_PD_MAX", new BigDecimal("0.25"));
        CreditProfileSnapshotEntity profile = context.creditProfile();

        if (context.application().getAnnualIncome() == null || context.application().getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0) {
            return new PolicyDecision("MANUAL_REVIEW", band(defaultProbability));
        }
        if (context.application().getLoanAmount() == null || context.application().getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new PolicyDecision("AUTO_DECLINE", band(defaultProbability));
        }
        if (context.application().getDti() != null && context.application().getDti().compareTo(new BigDecimal("0.65")) > 0) {
            return new PolicyDecision("AUTO_DECLINE", band(defaultProbability));
        }
        if (profile == null || profile.getFicoLow() == null || profile.getFicoHigh() == null) {
            return new PolicyDecision("MANUAL_REVIEW", band(defaultProbability));
        }

        if (defaultProbability.compareTo(autoApprove) < 0) {
            return new PolicyDecision("AUTO_APPROVE", band(defaultProbability));
        }
        if (defaultProbability.compareTo(manualReview) < 0) {
            return new PolicyDecision("MANUAL_REVIEW", band(defaultProbability));
        }
        return new PolicyDecision("AUTO_DECLINE", band(defaultProbability));
    }

    private BigDecimal getThreshold(String key, BigDecimal fallback) {
        return systemConfigRepository.findById(key)
                .map(cfg -> new BigDecimal(cfg.getConfigValue()))
                .orElse(fallback);
    }

    private String band(BigDecimal pd) {
        if (pd.compareTo(new BigDecimal("0.12")) < 0) return "LOW";
        if (pd.compareTo(new BigDecimal("0.25")) < 0) return "MEDIUM";
        return "HIGH";
    }
}
