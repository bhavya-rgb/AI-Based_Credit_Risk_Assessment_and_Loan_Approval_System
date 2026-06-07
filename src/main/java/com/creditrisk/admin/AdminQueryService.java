package com.creditrisk.admin;

import com.creditrisk.audit.AuditLogRepository;
import com.creditrisk.common.PageResponse;
import com.creditrisk.decision.LoanDecisionRepository;
import com.creditrisk.loan.LoanApplicationRepository;
import com.creditrisk.ml.model.MlModelRepository;
import com.creditrisk.ml.model.ModelStatus;
import com.creditrisk.ml.scoring.RiskAssessmentRepository;
import com.creditrisk.applicant.ApplicantRepository;
import com.creditrisk.ml.scoring.RiskAssessmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminQueryService {
    private final ApplicantRepository applicantRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final LoanDecisionRepository loanDecisionRepository;
    private final MlModelRepository mlModelRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminQueryService(ApplicantRepository applicantRepository,
                             LoanApplicationRepository loanApplicationRepository,
                             RiskAssessmentRepository riskAssessmentRepository,
                             LoanDecisionRepository loanDecisionRepository,
                             MlModelRepository mlModelRepository,
                             AuditLogRepository auditLogRepository) {
        this.applicantRepository = applicantRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.loanDecisionRepository = loanDecisionRepository;
        this.mlModelRepository = mlModelRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminMetricsSummaryResponse metricsSummary() {
        String activeModelVersion = mlModelRepository.findFirstByStatusOrderByPromotedAtDescCreatedAtDesc(ModelStatus.ACTIVE)
                .map(m -> m.getModelVersion())
                .orElse(null);

        Map<String, Long> applicationsByStatus = loanApplicationRepository.findAll().stream()
                .collect(Collectors.groupingBy(a -> a.getApplicationStatus().name(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> decisionsByStatus = loanDecisionRepository.findAll().stream()
                .collect(Collectors.groupingBy(d -> d.getDecisionStatus().name(), LinkedHashMap::new, Collectors.counting()));

        return new AdminMetricsSummaryResponse(
                applicantRepository.count(),
                loanApplicationRepository.count(),
                riskAssessmentRepository.count(),
                loanDecisionRepository.count(),
                mlModelRepository.count(),
                activeModelVersion,
                applicationsByStatus,
                decisionsByStatus
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> auditLogs(int page, int size) {
        Page<AuditLogResponse> result = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).map(AuditLogResponse::from);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public DecisionOutcomesReportResponse decisionOutcomes() {
        Map<String, Long> decisions = loanDecisionRepository.findAll().stream()
                .collect(Collectors.groupingBy(d -> d.getDecisionStatus().name(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> recommendations = riskAssessmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(RiskAssessmentEntity::getRecommendation, LinkedHashMap::new, Collectors.counting()));

        return new DecisionOutcomesReportResponse(decisions, recommendations, loanDecisionRepository.count());
    }
}
