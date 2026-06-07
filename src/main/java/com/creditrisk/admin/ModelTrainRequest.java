package com.creditrisk.admin;

import com.creditrisk.ml.training.TrainingRunType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ModelTrainRequest(
        @NotBlank String datasetVersion,
        @NotNull TrainingRunType mode,
        String featurePolicy,
        Map<String, Object> optimizerConfig,
        Map<String, Object> xgboostSearchSpace,
        Map<String, Object> evaluationStrategy
) {}
