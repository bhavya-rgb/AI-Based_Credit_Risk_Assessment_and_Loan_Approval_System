package com.creditrisk.ml.scoring;

import java.math.BigDecimal;
import java.util.List;

public record ScoringComputationResult(BigDecimal defaultProbability, List<String> topReasonCodes, String featureVectorHash) {
}
