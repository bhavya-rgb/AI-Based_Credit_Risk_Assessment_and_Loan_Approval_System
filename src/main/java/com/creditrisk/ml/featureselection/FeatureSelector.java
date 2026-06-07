package com.creditrisk.ml.featureselection;

import com.creditrisk.ml.preprocessing.PreparedDataset;

public interface FeatureSelector {
    FeatureSelectionResult selectFeatures(PreparedDataset dataset, FeatureSelectionMode mode);
}
