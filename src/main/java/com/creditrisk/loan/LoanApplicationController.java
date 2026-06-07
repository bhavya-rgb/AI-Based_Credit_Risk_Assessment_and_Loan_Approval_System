package com.creditrisk.loan;

import com.creditrisk.auth.SecurityPrincipal;
import com.creditrisk.common.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-applications")
public class LoanApplicationController {
    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    public LoanApplicationResponse create(@Valid @RequestBody LoanApplicationCreateRequest request,
                                          @AuthenticationPrincipal SecurityPrincipal principal,
                                          HttpServletRequest httpRequest) {
        return loanApplicationService.create(request, principal == null ? null : principal.userId(), httpRequest.getRemoteAddr());
    }

    @GetMapping("/{id}")
    public LoanApplicationResponse get(@PathVariable Long id) {
        return loanApplicationService.get(id);
    }

    @GetMapping
    public PageResponse<LoanApplicationResponse> list(@RequestParam(required = false) LoanApplicationStatus status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        Page<LoanApplicationResponse> result = loanApplicationService.list(status, page, size);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @PatchMapping("/{id}/submit")
    public LoanApplicationResponse submit(@PathVariable Long id,
                                          @AuthenticationPrincipal SecurityPrincipal principal,
                                          HttpServletRequest httpRequest) {
        return loanApplicationService.submit(id, principal == null ? null : principal.userId(), httpRequest.getRemoteAddr());
    }

    @PatchMapping("/{id}/status")
    public LoanApplicationResponse updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody LoanApplicationStatusUpdateRequest request,
                                                @AuthenticationPrincipal SecurityPrincipal principal,
                                                HttpServletRequest httpRequest) {
        return loanApplicationService.updateStatus(id, request.status(), principal == null ? null : principal.userId(), httpRequest.getRemoteAddr());
    }
}
