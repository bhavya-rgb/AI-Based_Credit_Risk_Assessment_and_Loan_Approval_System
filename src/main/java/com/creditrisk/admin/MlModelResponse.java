package com.creditrisk.admin;

import com.creditrisk.ml.model.MlModelEntity;
import com.creditrisk.ml.model.ModelStatus;

import java.time.Instant;

public record MlModelResponse(
        Long id,
        String modelName,
        String modelVersion,
        String modelType,
        ModelStatus status,
        String artifactPath,
        String trainedOnDatasetVersion,
        Instant createdAt,
        Instant promotedAt,
        String metricsJson
) {
    public static MlModelResponse from(MlModelEntity e) {
        return new MlModelResponse(e.getId(), e.getModelName(), e.getModelVersion(), e.getModelType(), e.getStatus(), e.getArtifactPath(), e.getTrainedOnDatasetVersion(), e.getCreatedAt(), e.getPromotedAt(), e.getMetricsJson());
    }
}
