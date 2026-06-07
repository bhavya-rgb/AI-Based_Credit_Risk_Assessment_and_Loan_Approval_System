package com.creditrisk.ml.scoring;

import com.creditrisk.loan.LoanApplicationEntity;
import com.creditrisk.ml.model.MlModelEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplicationEntity loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private MlModelEntity model;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "default_probability", nullable = false, precision = 8, scale = 6)
    private BigDecimal defaultProbability;

    @Column(name = "risk_band", nullable = false)
    private String riskBand;

    @Column(name = "recommendation", nullable = false)
    private String recommendation;

    @Column(name = "top_reason_codes_json", columnDefinition = "TEXT")
    private String topReasonCodesJson;

    @CreationTimestamp
    @Column(name = "scored_at", nullable = false, updatable = false)
    private Instant scoredAt;

    @Column(name = "feature_vector_hash")
    private String featureVectorHash;

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

    public MlModelEntity getModel() {
        return model;
    }

    public void setModel(MlModelEntity model) {
        this.model = model;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public BigDecimal getDefaultProbability() {
        return defaultProbability;
    }

    public void setDefaultProbability(BigDecimal defaultProbability) {
        this.defaultProbability = defaultProbability;
    }

    public String getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(String riskBand) {
        this.riskBand = riskBand;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getTopReasonCodesJson() {
        return topReasonCodesJson;
    }

    public void setTopReasonCodesJson(String topReasonCodesJson) {
        this.topReasonCodesJson = topReasonCodesJson;
    }

    public Instant getScoredAt() {
        return scoredAt;
    }

    public void setScoredAt(Instant scoredAt) {
        this.scoredAt = scoredAt;
    }

    public String getFeatureVectorHash() {
        return featureVectorHash;
    }

    public void setFeatureVectorHash(String featureVectorHash) {
        this.featureVectorHash = featureVectorHash;
    }
}
