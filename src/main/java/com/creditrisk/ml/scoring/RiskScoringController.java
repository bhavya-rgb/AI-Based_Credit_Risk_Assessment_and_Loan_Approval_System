package com.creditrisk.ml.scoring;

import com.creditrisk.auth.SecurityPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-applications/{loanApplicationId}")
public class RiskScoringController {
    private final RiskScoringService riskScoringService;

    public RiskScoringController(RiskScoringService riskScoringService) {
        this.riskScoringService = riskScoringService;
    }

    @PostMapping("/score")
    public RiskAssessmentResponse score(@PathVariable Long loanApplicationId,
                                        @AuthenticationPrincipal SecurityPrincipal principal,
                                        HttpServletRequest request) {
        return riskScoringService.score(loanApplicationId, principal == null ? null : principal.userId(), request.getRemoteAddr());
    }

    @GetMapping("/risk-assessments")
    public List<RiskAssessmentResponse> listAssessments(@PathVariable Long loanApplicationId) {
        return riskScoringService.listForApplication(loanApplicationId);
    }
}
