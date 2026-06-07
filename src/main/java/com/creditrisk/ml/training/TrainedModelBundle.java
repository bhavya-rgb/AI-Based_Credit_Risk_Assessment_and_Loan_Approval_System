package com.creditrisk.ml.training;

import java.util.Map;

public record TrainedModelBundle(
        Map<String, Object> featureSchema,
        Map<String, Object> hyperparameters,
        Map<String, Object> metrics,
        Map<String, Object> artifactPayload
) {}
