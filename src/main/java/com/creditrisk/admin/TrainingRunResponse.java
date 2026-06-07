package com.creditrisk.admin;

import com.creditrisk.ml.training.TrainingRunEntity;
import com.creditrisk.ml.training.TrainingRunStatus;
import com.creditrisk.ml.training.TrainingRunType;

import java.time.Instant;
import java.util.Map;

public record TrainingRunResponse(
        Long runId,
        TrainingRunStatus status,
        TrainingRunType runType,
        Map<String, Object> metrics,
        Instant startedAt,
        Instant endedAt,
        String errorMessage
) {
    public static TrainingRunResponse of(TrainingRunEntity entity, Map<String, Object> metrics) {
        return new TrainingRunResponse(entity.getId(), entity.getStatus(), entity.getRunType(), metrics, entity.getStartedAt(), entity.getEndedAt(), entity.getErrorMessage());
    }
}
