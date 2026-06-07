package com.creditrisk.ml.scoring;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RiskAssessmentResponse(
        Long assessmentId,
        String modelVersion,
        BigDecimal defaultProbability,
        Integer riskScore,
        String riskBand,
        String recommendation,
        List<String> topReasons,
        Instant scoredAt
) {}
