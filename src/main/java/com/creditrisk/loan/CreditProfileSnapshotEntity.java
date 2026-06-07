package com.creditrisk.loan;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "credit_profile_snapshots")
public class CreditProfileSnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    private LoanApplicationEntity loanApplication;

    @Column(name = "fico_low")
    private Integer ficoLow;

    @Column(name = "fico_high")
    private Integer ficoHigh;

    @Column(name = "inq_last_6mths")
    private Integer inqLast6Mths;

    @Column(name = "delinq_2yrs")
    private Integer delinq2Yrs;

    @Column(name = "open_acc")
    private Integer openAcc;

    @Column(name = "pub_rec")
    private Integer pubRec;

    @Column(name = "revol_bal")
    private BigDecimal revolBal;

    @Column(name = "revol_util", precision = 8, scale = 4)
    private BigDecimal revolUtil;

    @Column(name = "total_acc")
    private Integer totalAcc;

    @Column(name = "mort_acc")
    private Integer mortAcc;

    @Column(name = "pub_rec_bankruptcies")
    private Integer pubRecBankruptcies;

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

    public Integer getFicoLow() {
        return ficoLow;
    }

    public void setFicoLow(Integer ficoLow) {
        this.ficoLow = ficoLow;
    }

    public Integer getFicoHigh() {
        return ficoHigh;
    }

    public void setFicoHigh(Integer ficoHigh) {
        this.ficoHigh = ficoHigh;
    }

    public Integer getInqLast6Mths() {
        return inqLast6Mths;
    }

    public void setInqLast6Mths(Integer inqLast6Mths) {
        this.inqLast6Mths = inqLast6Mths;
    }

    public Integer getDelinq2Yrs() {
        return delinq2Yrs;
    }

    public void setDelinq2Yrs(Integer delinq2Yrs) {
        this.delinq2Yrs = delinq2Yrs;
    }

    public Integer getOpenAcc() {
        return openAcc;
    }

    public void setOpenAcc(Integer openAcc) {
        this.openAcc = openAcc;
    }

    public Integer getPubRec() {
        return pubRec;
    }

    public void setPubRec(Integer pubRec) {
        this.pubRec = pubRec;
    }

    public BigDecimal getRevolBal() {
        return revolBal;
    }

    public void setRevolBal(BigDecimal revolBal) {
        this.revolBal = revolBal;
    }

    public BigDecimal getRevolUtil() {
        return revolUtil;
    }

    public void setRevolUtil(BigDecimal revolUtil) {
        this.revolUtil = revolUtil;
    }

    public Integer getTotalAcc() {
        return totalAcc;
    }

    public void setTotalAcc(Integer totalAcc) {
        this.totalAcc = totalAcc;
    }

    public Integer getMortAcc() {
        return mortAcc;
    }

    public void setMortAcc(Integer mortAcc) {
        this.mortAcc = mortAcc;
    }

    public Integer getPubRecBankruptcies() {
        return pubRecBankruptcies;
    }

    public void setPubRecBankruptcies(Integer pubRecBankruptcies) {
        this.pubRecBankruptcies = pubRecBankruptcies;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
