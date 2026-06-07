package com.creditrisk.ml.optimization;

import java.util.List;
import java.util.Map;

public record OptimizationResult(
        String algorithm,
        Map<String, Object> bestHyperparameters,
        double bestScore,
        int iterations,
        List<Map<String, Object>> trajectory
) {}
