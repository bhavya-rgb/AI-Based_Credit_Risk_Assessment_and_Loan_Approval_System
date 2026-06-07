package com.creditrisk.ml.training;

import com.creditrisk.ml.featureselection.FeatureSelectionResult;
import com.creditrisk.ml.optimization.OptimizationResult;
import com.creditrisk.ml.preprocessing.PreparedDataset;

public interface RiskModelTrainer {
    TrainedModelBundle train(PreparedDataset dataset, FeatureSelectionResult fsResult, OptimizationResult optResult, TrainingRunType runType);
}
