package com.creditrisk.admin;

import com.creditrisk.ml.dataset.DatasetIngestionJobEntity;
import com.creditrisk.ml.dataset.DatasetIngestionStatus;
import com.creditrisk.ml.dataset.DatasetIngestionTriggerMode;

import java.time.Instant;

public record DatasetIngestionJobResponse(
        Long id,
        String sourceName,
        String sourcePath,
        String sourceFingerprint,
        String datasetVersion,
        DatasetIngestionStatus status,
        DatasetIngestionTriggerMode triggerMode,
        Boolean generatedFile,
        Long rowsRead,
        Long rowsLoaded,
        Instant startedAt,
        Instant endedAt,
        String logSummary
) {
    public static DatasetIngestionJobResponse from(DatasetIngestionJobEntity e) {
        return new DatasetIngestionJobResponse(
                e.getId(),
                e.getSourceName(),
                e.getSourcePath(),
                e.getSourceFingerprint(),
                e.getDatasetVersion(),
                e.getStatus(),
                e.getTriggerMode(),
                e.getGeneratedFile(),
                e.getRowsRead(),
                e.getRowsLoaded(),
                e.getStartedAt(),
                e.getEndedAt(),
                e.getLogSummary()
        );
    }
}
