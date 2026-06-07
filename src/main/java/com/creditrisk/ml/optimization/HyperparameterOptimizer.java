package com.creditrisk.ml.optimization;

import com.creditrisk.ml.featureselection.FeatureSelectionResult;
import com.creditrisk.ml.preprocessing.PreparedDataset;

import java.util.Map;

public interface HyperparameterOptimizer {
    OptimizationResult optimize(PreparedDataset dataset, FeatureSelectionResult featureSelectionResult, Map<String, Object> searchSpace);
}
