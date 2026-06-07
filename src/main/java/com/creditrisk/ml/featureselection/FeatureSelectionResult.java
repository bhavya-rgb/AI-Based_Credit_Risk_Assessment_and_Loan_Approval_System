package com.creditrisk.ml.featureselection;

import java.util.List;
import java.util.Map;

public record FeatureSelectionResult(
        String algorithm,
        List<String> selectedFeatures,
        double ca,
        double dr,
        double auc,
        double fitness,
        long seed,
        int iterations,
        Map<String, Object> debug
) {}
