package com.creditrisk.applicant;

import com.creditrisk.auth.SecurityPrincipal;
import com.creditrisk.common.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applicants")
public class ApplicantController {
    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @PostMapping
    public ApplicantResponse create(@Valid @RequestBody ApplicantCreateRequest request,
                                    @AuthenticationPrincipal SecurityPrincipal principal,
                                    HttpServletRequest httpRequest) {
        return applicantService.create(request, principal == null ? null : principal.userId(), httpRequest.getRemoteAddr());
    }

    @GetMapping("/{id}")
    public ApplicantResponse get(@PathVariable Long id) {
        return applicantService.get(id);
    }

    @GetMapping
    public PageResponse<ApplicantResponse> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Page<ApplicantResponse> result = applicantService.list(page, size);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
