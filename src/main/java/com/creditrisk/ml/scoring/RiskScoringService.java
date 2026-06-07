package com.creditrisk.ml.scoring;

import com.creditrisk.audit.AuditService;
import com.creditrisk.common.ApiException;
import com.creditrisk.common.JsonSupport;
import com.creditrisk.decision.DecisionPolicyEngine;
import com.creditrisk.decision.LoanDecisionContext;
import com.creditrisk.decision.PolicyDecision;
import com.creditrisk.loan.CreditProfileSnapshotEntity;
import com.creditrisk.loan.LoanApplicationEntity;
import com.creditrisk.loan.LoanApplicationService;
import com.creditrisk.loan.LoanApplicationStatus;
import com.creditrisk.ml.model.MlModelEntity;
import com.creditrisk.ml.model.ModelRegistryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RiskScoringService {
    private final LoanApplicationService loanApplicationService;
    private final ModelRegistryService modelRegistryService;
    private final RiskScorer riskScorer;
    private final DecisionPolicyEngine decisionPolicyEngine;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AuditService auditService;
    private final JsonSupport jsonSupport;

    public RiskScoringService(LoanApplicationService loanApplicationService,
                              ModelRegistryService modelRegistryService,
                              RiskScorer riskScorer,
                              DecisionPolicyEngine decisionPolicyEngine,
                              RiskAssessmentRepository riskAssessmentRepository,
                              AuditService auditService,
                              JsonSupport jsonSupport) {
        this.loanApplicationService = loanApplicationService;
        this.modelRegistryService = modelRegistryService;
        this.riskScorer = riskScorer;
        this.decisionPolicyEngine = decisionPolicyEngine;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.auditService = auditService;
        this.jsonSupport = jsonSupport;
    }

    @Transactional
    public RiskAssessmentResponse score(Long loanApplicationId, Long actorUserId, String ipAddress) {
        LoanApplicationEntity application = loanApplicationService.getEntity(loanApplicationId);
        if (application.getApplicationStatus() == LoanApplicationStatus.DRAFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Submit application before scoring");
        }
        CreditProfileSnapshotEntity creditProfile = loanApplicationService.getCreditProfile(loanApplicationId);
        MlModelEntity model = modelRegistryService.getActiveModel();

        ScoringComputationResult computation = riskScorer.score(model, application, creditProfile);
        PolicyDecision policy = decisionPolicyEngine.evaluate(computation.defaultProbability(), new LoanDecisionContext(application, creditProfile));

        RiskAssessmentEntity entity = new RiskAssessmentEntity();
        entity.setLoanApplication(application);
        entity.setModel(model);
        entity.setDefaultProbability(computation.defaultProbability());
        int riskScore = BigDecimal.ONE.subtract(computation.defaultProbability())
                .multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        entity.setRiskScore(riskScore);
        entity.setRiskBand(policy.riskBand());
        entity.setRecommendation(policy.recommendation());
        entity.setTopReasonCodesJson(jsonSupport.toJson(computation.topReasonCodes()));
        entity.setFeatureVectorHash(computation.featureVectorHash());
        RiskAssessmentEntity saved = riskAssessmentRepository.save(entity);

        LoanApplicationStatus nextStatus = "MANUAL_REVIEW".equals(policy.recommendation())
                ? LoanApplicationStatus.MANUAL_REVIEW
                : LoanApplicationStatus.SCORED;
        loanApplicationService.updateStatus(loanApplicationId, nextStatus, actorUserId, ipAddress);

        RiskAssessmentResponse response = toResponse(saved, model.getModelVersion(), computation.topReasonCodes());
        auditService.log(actorUserId, "RISK_ASSESSMENT_CREATED", "RISK_ASSESSMENT", String.valueOf(saved.getId()), null, response, ipAddress);
        return response;
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentResponse> listForApplication(Long loanApplicationId) {
        return riskAssessmentRepository.findByLoanApplicationIdOrderByScoredAtDesc(loanApplicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskAssessmentEntity getLatestAssessmentForApplication(Long loanApplicationId) {
        return riskAssessmentRepository.findByLoanApplicationIdOrderByScoredAtDesc(loanApplicationId).stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No risk assessment found for loan application"));
    }

    private RiskAssessmentResponse toResponse(RiskAssessmentEntity e) {
        List<String> reasons = e.getTopReasonCodesJson() == null ? List.of() : jsonSupport.fromJson(e.getTopReasonCodesJson(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        return toResponse(e, e.getModel().getModelVersion(), reasons);
    }

    private RiskAssessmentResponse toResponse(RiskAssessmentEntity e, String modelVersion, List<String> reasons) {
        return new RiskAssessmentResponse(
                e.getId(),
                modelVersion,
                e.getDefaultProbability(),
                e.getRiskScore(),
                e.getRiskBand(),
                e.getRecommendation(),
                reasons,
                e.getScoredAt()
        );
    }
}
