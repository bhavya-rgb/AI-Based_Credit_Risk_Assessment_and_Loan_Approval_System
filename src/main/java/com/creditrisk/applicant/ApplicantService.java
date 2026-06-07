package com.creditrisk.applicant;

import com.creditrisk.audit.AuditService;
import com.creditrisk.common.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicantService {
    private final ApplicantRepository applicantRepository;
    private final AuditService auditService;

    public ApplicantService(ApplicantRepository applicantRepository, AuditService auditService) {
        this.applicantRepository = applicantRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ApplicantResponse create(ApplicantCreateRequest request, Long actorUserId, String ipAddress) {
        ApplicantEntity entity = new ApplicantEntity();
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setDob(request.dob());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setGovernmentIdMasked(maskGovernmentId(request.governmentId()));
        ApplicantEntity saved = applicantRepository.save(entity);
        auditService.log(actorUserId, "APPLICANT_CREATED", "APPLICANT", String.valueOf(saved.getId()), null, ApplicantResponse.from(saved), ipAddress);
        return ApplicantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ApplicantResponse get(Long id) {
        return applicantRepository.findById(id).map(ApplicantResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Applicant not found"));
    }

    @Transactional(readOnly = true)
    public Page<ApplicantResponse> list(int page, int size) {
        Page<ApplicantEntity> p = applicantRepository.findAll(PageRequest.of(page, size));
        return p.map(ApplicantResponse::from);
    }

    private String maskGovernmentId(String value) {
        String trimmed = value.replaceAll("\\s+", "");
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }
}
