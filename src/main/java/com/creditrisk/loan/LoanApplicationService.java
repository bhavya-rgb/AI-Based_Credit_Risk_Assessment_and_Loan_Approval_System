package com.creditrisk.loan;

import com.creditrisk.applicant.ApplicantEntity;
import com.creditrisk.applicant.ApplicantRepository;
import com.creditrisk.audit.AuditService;
import com.creditrisk.common.ApiException;
import com.creditrisk.common.JsonSupport;
import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final CreditProfileSnapshotRepository creditProfileSnapshotRepository;
    private final ApplicantRepository applicantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final JsonSupport jsonSupport;

    public LoanApplicationService(LoanApplicationRepository loanApplicationRepository,
                                  CreditProfileSnapshotRepository creditProfileSnapshotRepository,
                                  ApplicantRepository applicantRepository,
                                  UserRepository userRepository,
                                  AuditService auditService,
                                  JsonSupport jsonSupport) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.creditProfileSnapshotRepository = creditProfileSnapshotRepository;
        this.applicantRepository = applicantRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.jsonSupport = jsonSupport;
    }

    @Transactional
    public LoanApplicationResponse create(LoanApplicationCreateRequest request, Long actorUserId, String ipAddress) {
        ApplicantEntity applicant = applicantRepository.findById(request.applicantId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Applicant not found"));

        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.setApplicant(applicant);
        entity.setLoanAmount(request.loanAmount());
        entity.setTermMonths(request.termMonths());
        entity.setPurpose(request.purpose());
        entity.setAnnualIncome(request.annualIncome());
        entity.setEmploymentLengthYears(request.employmentLengthYears());
        entity.setHomeOwnership(request.homeOwnership());
        entity.setVerificationStatus(request.verificationStatus());
        entity.setDti(request.dti());
        entity.setExistingDebt(request.existingDebt());
        entity.setApplicationStatus(LoanApplicationStatus.DRAFT);
        if (actorUserId != null) {
            userRepository.findById(actorUserId).ifPresent(entity::setCreatedBy);
        }
        entity.setRawFeatureSnapshotJson(jsonSupport.toJson(buildFeatureSnapshotMap(request)));
        LoanApplicationEntity saved = loanApplicationRepository.save(entity);

        CreditProfileSnapshotEntity snapshot = new CreditProfileSnapshotEntity();
        snapshot.setLoanApplication(saved);
        mapCreditProfile(request.creditProfile(), snapshot);
        creditProfileSnapshotRepository.save(snapshot);

        LoanApplicationResponse response = toResponse(saved, snapshot);
        auditService.log(actorUserId, "LOAN_APPLICATION_CREATED", "LOAN_APPLICATION", String.valueOf(saved.getId()), null, response, ipAddress);
        return response;
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse get(Long id) {
        LoanApplicationEntity entity = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan application not found"));
        CreditProfileSnapshotEntity snapshot = creditProfileSnapshotRepository.findByLoanApplicationId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Credit profile snapshot not found"));
        return toResponse(entity, snapshot);
    }

    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> list(LoanApplicationStatus status, int page, int size) {
        Page<LoanApplicationEntity> entities = (status == null)
                ? loanApplicationRepository.findAll(PageRequest.of(page, size))
                : loanApplicationRepository.findAllByApplicationStatus(status, PageRequest.of(page, size));
        return entities.map(entity -> {
            CreditProfileSnapshotEntity snapshot = creditProfileSnapshotRepository.findByLoanApplicationId(entity.getId()).orElse(null);
            return toResponse(entity, snapshot);
        });
    }

    @Transactional
    public LoanApplicationResponse submit(Long id, Long actorUserId, String ipAddress) {
        LoanApplicationEntity entity = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan application not found"));
        LoanApplicationStatus before = entity.getApplicationStatus();
        if (entity.getApplicationStatus() == LoanApplicationStatus.DECIDED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot submit a decided application");
        }
        entity.setApplicationStatus(LoanApplicationStatus.SUBMITTED);
        entity.setSubmittedAt(Instant.now());
        LoanApplicationEntity saved = loanApplicationRepository.save(entity);
        CreditProfileSnapshotEntity snapshot = creditProfileSnapshotRepository.findByLoanApplicationId(id).orElse(null);
        LoanApplicationResponse response = toResponse(saved, snapshot);
        auditService.log(actorUserId, "LOAN_APPLICATION_SUBMITTED", "LOAN_APPLICATION", String.valueOf(saved.getId()), Map.of("status", before), Map.of("status", saved.getApplicationStatus()), ipAddress);
        return response;
    }

    @Transactional
    public LoanApplicationResponse updateStatus(Long id, LoanApplicationStatus status, Long actorUserId, String ipAddress) {
        LoanApplicationEntity entity = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan application not found"));
        LoanApplicationStatus before = entity.getApplicationStatus();
        entity.setApplicationStatus(status);
        LoanApplicationEntity saved = loanApplicationRepository.save(entity);
        CreditProfileSnapshotEntity snapshot = creditProfileSnapshotRepository.findByLoanApplicationId(id).orElse(null);
        LoanApplicationResponse response = toResponse(saved, snapshot);
        auditService.log(actorUserId, "LOAN_APPLICATION_STATUS_UPDATED", "LOAN_APPLICATION", String.valueOf(saved.getId()), Map.of("status", before), Map.of("status", status), ipAddress);
        return response;
    }

    @Transactional(readOnly = true)
    public LoanApplicationEntity getEntity(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan application not found"));
    }

    @Transactional(readOnly = true)
    public CreditProfileSnapshotEntity getCreditProfile(Long loanApplicationId) {
        return creditProfileSnapshotRepository.findByLoanApplicationId(loanApplicationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Credit profile snapshot not found"));
    }

    private LoanApplicationResponse toResponse(LoanApplicationEntity entity, CreditProfileSnapshotEntity snapshot) {
        return new LoanApplicationResponse(
                entity.getId(),
                entity.getApplicant().getId(),
                entity.getLoanAmount(),
                entity.getTermMonths(),
                entity.getPurpose(),
                entity.getAnnualIncome(),
                entity.getEmploymentLengthYears(),
                entity.getHomeOwnership(),
                entity.getVerificationStatus(),
                entity.getDti(),
                entity.getExistingDebt(),
                entity.getApplicationStatus(),
                entity.getSubmittedAt(),
                snapshot == null ? null : toDto(snapshot),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CreditProfileDto toDto(CreditProfileSnapshotEntity s) {
        return new CreditProfileDto(s.getFicoLow(), s.getFicoHigh(), s.getInqLast6Mths(), s.getDelinq2Yrs(), s.getOpenAcc(), s.getPubRec(), s.getRevolBal(), s.getRevolUtil(), s.getTotalAcc(), s.getMortAcc(), s.getPubRecBankruptcies());
    }

    private void mapCreditProfile(CreditProfileDto dto, CreditProfileSnapshotEntity entity) {
        entity.setFicoLow(dto.ficoLow());
        entity.setFicoHigh(dto.ficoHigh());
        entity.setInqLast6Mths(dto.inqLast6Months());
        entity.setDelinq2Yrs(dto.delinq2Yrs());
        entity.setOpenAcc(dto.openAccounts());
        entity.setPubRec(dto.publicRecords());
        entity.setRevolBal(dto.revolvingBalance());
        entity.setRevolUtil(dto.revolvingUtilization());
        entity.setTotalAcc(dto.totalAccounts());
        entity.setMortAcc(dto.mortgageAccounts());
        entity.setPubRecBankruptcies(dto.bankruptcies());
    }

    private Map<String, Object> buildFeatureSnapshotMap(LoanApplicationCreateRequest request) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("loan_amount", request.loanAmount());
        map.put("term_months", request.termMonths());
        map.put("purpose", request.purpose());
        map.put("annual_income", request.annualIncome());
        map.put("employment_length_years", request.employmentLengthYears());
        map.put("home_ownership", request.homeOwnership());
        map.put("verification_status", request.verificationStatus());
        map.put("dti", request.dti());
        map.put("existing_debt", request.existingDebt());
        map.put("credit_profile", request.creditProfile());
        return map;
    }
}
