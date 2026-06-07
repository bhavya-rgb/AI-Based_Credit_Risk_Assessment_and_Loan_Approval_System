package com.creditrisk.decision;

import com.creditrisk.auth.SecurityPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LoanDecisionController {
    private final LoanDecisionService loanDecisionService;

    public LoanDecisionController(LoanDecisionService loanDecisionService) {
        this.loanDecisionService = loanDecisionService;
    }

    @PostMapping("/loan-applications/{loanApplicationId}/decision")
    public LoanDecisionResponse create(@PathVariable Long loanApplicationId,
                                       @Valid @RequestBody LoanDecisionRequest request,
                                       @AuthenticationPrincipal SecurityPrincipal principal,
                                       HttpServletRequest httpRequest) {
        return loanDecisionService.createDecision(loanApplicationId, request, principal == null ? null : principal.userId(), httpRequest.getRemoteAddr());
    }

    @GetMapping("/loan-decisions/{id}")
    public LoanDecisionResponse get(@PathVariable Long id) {
        return loanDecisionService.get(id);
    }
}
