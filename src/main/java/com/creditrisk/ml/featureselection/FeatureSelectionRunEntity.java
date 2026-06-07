package com.creditrisk.ml.featureselection;

import com.creditrisk.ml.training.TrainingRunEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "feature_selection_runs")
public class FeatureSelectionRunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_run_id", nullable = false)
    private TrainingRunEntity trainingRun;

    @Column(nullable = false)
    private String algorithm;

    @Column(name = "selected_features_json", nullable = false, columnDefinition = "TEXT")
    private String selectedFeaturesJson;

    @Column(precision = 10, scale = 6)
    private BigDecimal ca;

    @Column(precision = 10, scale = 6)
    private BigDecimal dr;

    @Column(precision = 10, scale = 6)
    private BigDecimal auc;

    @Column(precision = 10, scale = 6)
    private BigDecimal fitness;

    private Long seed;

    private Integer iterations;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingRunEntity getTrainingRun() {
        return trainingRun;
    }

    public void setTrainingRun(TrainingRunEntity trainingRun) {
        this.trainingRun = trainingRun;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSelectedFeaturesJson() {
        return selectedFeaturesJson;
    }

    public void setSelectedFeaturesJson(String selectedFeaturesJson) {
        this.selectedFeaturesJson = selectedFeaturesJson;
    }

    public BigDecimal getCa() {
        return ca;
    }

    public void setCa(BigDecimal ca) {
        this.ca = ca;
    }

    public BigDecimal getDr() {
        return dr;
    }

    public void setDr(BigDecimal dr) {
        this.dr = dr;
    }

    public BigDecimal getAuc() {
        return auc;
    }

    public void setAuc(BigDecimal auc) {
        this.auc = auc;
    }

    public BigDecimal getFitness() {
        return fitness;
    }

    public void setFitness(BigDecimal fitness) {
        this.fitness = fitness;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
