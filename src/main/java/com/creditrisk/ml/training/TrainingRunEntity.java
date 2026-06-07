package com.creditrisk.ml.training;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "training_runs")
public class TrainingRunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false)
    private TrainingRunType runType;

    @Column(name = "dataset_version", nullable = false)
    private String datasetVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingRunStatus status;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingRunType getRunType() {
        return runType;
    }

    public void setRunType(TrainingRunType runType) {
        this.runType = runType;
    }

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public TrainingRunStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingRunStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public String getMetricsJson() {
        return metricsJson;
    }

    public void setMetricsJson(String metricsJson) {
        this.metricsJson = metricsJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
