package com.creditrisk.decision;

import com.creditrisk.audit.AuditService;
import com.creditrisk.common.ApiException;
import com.creditrisk.loan.LoanApplicationEntity;
import com.creditrisk.loan.LoanApplicationService;
import com.creditrisk.loan.LoanApplicationStatus;
import com.creditrisk.ml.scoring.RiskAssessmentEntity;
import com.creditrisk.ml.scoring.RiskScoringService;
import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LoanDecisionService {
    private final LoanDecisionRepository loanDecisionRepository;
    private final LoanApplicationService loanApplicationService;
    private final RiskScoringService riskScoringService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public LoanDecisionService(LoanDecisionRepository loanDecisionRepository,
                               LoanApplicationService loanApplicationService,
                               RiskScoringService riskScoringService,
                               UserRepository userRepository,
                               AuditService auditService) {
        this.loanDecisionRepository = loanDecisionRepository;
        this.loanApplicationService = loanApplicationService;
        this.riskScoringService = riskScoringService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public LoanDecisionResponse createDecision(Long loanApplicationId, LoanDecisionRequest request, Long actorUserId, String ipAddress) {
        LoanApplicationEntity application = loanApplicationService.getEntity(loanApplicationId);
        RiskAssessmentEntity latestAssessment = riskScoringService.getLatestAssessmentForApplication(loanApplicationId);

        String recommendedStatus = mapRecommendationToDecision(latestAssessment.getRecommendation()).name();
        boolean override = !request.decisionStatus().name().equals(recommendedStatus);
        if (override && (request.overrideReason() == null || request.overrideReason().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Override reason is required when overriding model recommendation");
        }

        LoanDecisionEntity entity = new LoanDecisionEntity();
        entity.setLoanApplication(application);
        entity.setRiskAssessment(latestAssessment);
        entity.setDecisionStatus(request.decisionStatus());
        entity.setApprovedAmount(request.approvedAmount());
        entity.setApprovedTermMonths(request.approvedTermMonths());
        entity.setInterestRateOffer(request.interestRateOffer());
        entity.setOverrideFlag(override);
        entity.setOverrideReason(request.overrideReason());
        entity.setDecidedAt(Instant.now());
        if (actorUserId != null) {
            UserEntity user = userRepository.findById(actorUserId).orElse(null);
            entity.setDecidedBy(user);
        }
        LoanDecisionEntity saved = loanDecisionRepository.save(entity);

        loanApplicationService.updateStatus(loanApplicationId, LoanApplicationStatus.DECIDED, actorUserId, ipAddress);
        LoanDecisionResponse response = LoanDecisionResponse.from(saved);
        auditService.log(actorUserId, "LOAN_DECISION_CREATED", "LOAN_DECISION", String.valueOf(saved.getId()), null, response, ipAddress);
        return response;
    }

    @Transactional(readOnly = true)
    public LoanDecisionResponse get(Long id) {
        return loanDecisionRepository.findById(id)
                .map(LoanDecisionResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan decision not found"));
    }

    private LoanDecisionStatus mapRecommendationToDecision(String recommendation) {
        return switch (recommendation) {
            case "AUTO_APPROVE" -> LoanDecisionStatus.APPROVED;
            case "AUTO_DECLINE" -> LoanDecisionStatus.DECLINED;
            default -> LoanDecisionStatus.MANUAL_REVIEW;
        };
    }
}
