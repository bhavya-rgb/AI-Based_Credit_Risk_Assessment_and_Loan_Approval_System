package com.creditrisk.ml.preprocessing;

import java.nio.file.Path;

public interface FeaturePreprocessor {
    PreparedDataset preprocess(Path csvPath);
}
