package com.creditrisk.decision;

import com.creditrisk.config.SystemConfigEntity;
import com.creditrisk.config.SystemConfigRepository;
import com.creditrisk.loan.CreditProfileSnapshotEntity;
import com.creditrisk.loan.LoanApplicationEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

class SystemConfigDecisionPolicyEngineTest {

    @Test
    void returnsAutoApproveForLowRiskAndValidInputs() {
        SystemConfigRepository repo = Mockito.mock(SystemConfigRepository.class);
        Mockito.when(repo.findById(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            SystemConfigEntity cfg = new SystemConfigEntity();
            cfg.setConfigKey(key);
            cfg.setConfigValue(switch (key) {
                case "AUTO_APPROVE_PD_MAX" -> "0.12";
                case "MANUAL_REVIEW_PD_MAX" -> "0.25";
                default -> "0.25";
            });
            return Optional.of(cfg);
        });

        SystemConfigDecisionPolicyEngine engine = new SystemConfigDecisionPolicyEngine(repo);
        LoanApplicationEntity app = new LoanApplicationEntity();
        app.setAnnualIncome(new BigDecimal("80000"));
        app.setLoanAmount(new BigDecimal("10000"));
        app.setDti(new BigDecimal("0.22"));
        CreditProfileSnapshotEntity profile = new CreditProfileSnapshotEntity();
        profile.setFicoLow(680);
        profile.setFicoHigh(700);

        PolicyDecision decision = engine.evaluate(new BigDecimal("0.08"), new LoanDecisionContext(app, profile));
        assertEquals("AUTO_APPROVE", decision.recommendation());
        assertEquals("LOW", decision.riskBand());
    }

    @Test
    void forcesDeclineWhenDtiTooHigh() {
        SystemConfigRepository repo = Mockito.mock(SystemConfigRepository.class);
        Mockito.when(repo.findById(anyString())).thenReturn(Optional.empty());
        SystemConfigDecisionPolicyEngine engine = new SystemConfigDecisionPolicyEngine(repo);

        LoanApplicationEntity app = new LoanApplicationEntity();
        app.setAnnualIncome(new BigDecimal("50000"));
        app.setLoanAmount(new BigDecimal("9000"));
        app.setDti(new BigDecimal("0.91"));
        CreditProfileSnapshotEntity profile = new CreditProfileSnapshotEntity();
        profile.setFicoLow(700);
        profile.setFicoHigh(720);

        PolicyDecision decision = engine.evaluate(new BigDecimal("0.10"), new LoanDecisionContext(app, profile));
        assertEquals("AUTO_DECLINE", decision.recommendation());
    }
}
