package com.creditrisk.decision;

import com.creditrisk.loan.LoanApplicationEntity;
import com.creditrisk.ml.scoring.RiskAssessmentEntity;
import com.creditrisk.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loan_decisions")
public class LoanDecisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplicationEntity loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_assessment_id")
    private RiskAssessmentEntity riskAssessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_status", nullable = false)
    private LoanDecisionStatus decisionStatus;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    @Column(name = "interest_rate_offer", precision = 8, scale = 4)
    private BigDecimal interestRateOffer;

    @Column(name = "override_flag", nullable = false)
    private Boolean overrideFlag = false;

    @Column(name = "override_reason", length = 1000)
    private String overrideReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private UserEntity decidedBy;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanApplicationEntity getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplicationEntity loanApplication) {
        this.loanApplication = loanApplication;
    }

    public RiskAssessmentEntity getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(RiskAssessmentEntity riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public LoanDecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(LoanDecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public Integer getApprovedTermMonths() {
        return approvedTermMonths;
    }

    public void setApprovedTermMonths(Integer approvedTermMonths) {
        this.approvedTermMonths = approvedTermMonths;
    }

    public BigDecimal getInterestRateOffer() {
        return interestRateOffer;
    }

    public void setInterestRateOffer(BigDecimal interestRateOffer) {
        this.interestRateOffer = interestRateOffer;
    }

    public Boolean getOverrideFlag() {
        return overrideFlag;
    }

    public void setOverrideFlag(Boolean overrideFlag) {
        this.overrideFlag = overrideFlag;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public UserEntity getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(UserEntity decidedBy) {
        this.decidedBy = decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
