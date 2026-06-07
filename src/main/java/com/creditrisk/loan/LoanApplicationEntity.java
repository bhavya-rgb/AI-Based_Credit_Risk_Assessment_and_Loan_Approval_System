package com.creditrisk.loan;

import com.creditrisk.applicant.ApplicantEntity;
import com.creditrisk.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private ApplicantEntity applicant;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private String purpose;

    @Column(name = "annual_income", nullable = false)
    private BigDecimal annualIncome;

    @Column(name = "employment_length_years", nullable = false)
    private Integer employmentLengthYears;

    @Column(name = "home_ownership", nullable = false)
    private String homeOwnership;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal dti;

    @Column(name = "existing_debt", nullable = false)
    private BigDecimal existingDebt;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false)
    private LoanApplicationStatus applicationStatus = LoanApplicationStatus.DRAFT;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(name = "raw_feature_snapshot_json", columnDefinition = "TEXT")
    private String rawFeatureSnapshotJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicantEntity getApplicant() {
        return applicant;
    }

    public void setApplicant(ApplicantEntity applicant) {
        this.applicant = applicant;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public Integer getEmploymentLengthYears() {
        return employmentLengthYears;
    }

    public void setEmploymentLengthYears(Integer employmentLengthYears) {
        this.employmentLengthYears = employmentLengthYears;
    }

    public String getHomeOwnership() {
        return homeOwnership;
    }

    public void setHomeOwnership(String homeOwnership) {
        this.homeOwnership = homeOwnership;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public BigDecimal getDti() {
        return dti;
    }

    public void setDti(BigDecimal dti) {
        this.dti = dti;
    }

    public BigDecimal getExistingDebt() {
        return existingDebt;
    }

    public void setExistingDebt(BigDecimal existingDebt) {
        this.existingDebt = existingDebt;
    }

    public LoanApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(LoanApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public String getRawFeatureSnapshotJson() {
        return rawFeatureSnapshotJson;
    }

    public void setRawFeatureSnapshotJson(String rawFeatureSnapshotJson) {
        this.rawFeatureSnapshotJson = rawFeatureSnapshotJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
