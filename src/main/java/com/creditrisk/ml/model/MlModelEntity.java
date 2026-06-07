package com.creditrisk.ml.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ml_models")
public class MlModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_version", nullable = false, unique = true)
    private String modelVersion;

    @Column(name = "model_type", nullable = false)
    private String modelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelStatus status;

    @Column(name = "artifact_path", nullable = false, length = 1024)
    private String artifactPath;

    @Column(name = "feature_schema_json", columnDefinition = "TEXT")
    private String featureSchemaJson;

    @Column(name = "hyperparams_json", columnDefinition = "TEXT")
    private String hyperparamsJson;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "trained_on_dataset_version")
    private String trainedOnDatasetVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "promoted_at")
    private Instant promotedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public ModelStatus getStatus() {
        return status;
    }

    public void setStatus(ModelStatus status) {
        this.status = status;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    public String getFeatureSchemaJson() {
        return featureSchemaJson;
    }

    public void setFeatureSchemaJson(String featureSchemaJson) {
        this.featureSchemaJson = featureSchemaJson;
    }

    public String getHyperparamsJson() {
        return hyperparamsJson;
    }

    public void setHyperparamsJson(String hyperparamsJson) {
        this.hyperparamsJson = hyperparamsJson;
    }

    public String getMetricsJson() {
        return metricsJson;
    }

    public void setMetricsJson(String metricsJson) {
        this.metricsJson = metricsJson;
    }

    public String getTrainedOnDatasetVersion() {
        return trainedOnDatasetVersion;
    }

    public void setTrainedOnDatasetVersion(String trainedOnDatasetVersion) {
        this.trainedOnDatasetVersion = trainedOnDatasetVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPromotedAt() {
        return promotedAt;
    }

    public void setPromotedAt(Instant promotedAt) {
        this.promotedAt = promotedAt;
    }
}
