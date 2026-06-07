package com.creditrisk.admin;

import com.creditrisk.ml.dataset.DatasetIngestionJobEntity;
import com.creditrisk.ml.dataset.DatasetIngestionStatus;
import com.creditrisk.ml.dataset.DatasetIngestionTriggerMode;

import java.time.Instant;

public record LatestDatasetSummaryResponse(
        Long jobId,
        String sourceName,
        String sourcePath,
        String datasetVersion,
        DatasetIngestionStatus status,
        Long rowsRead,
        Long rowsLoaded,
        DatasetIngestionTriggerMode triggerMode,
        Instant startedAt,
        Instant endedAt
) {
    public static LatestDatasetSummaryResponse from(DatasetIngestionJobEntity e) {
        return new LatestDatasetSummaryResponse(
                e.getId(),
                e.getSourceName(),
                e.getSourcePath(),
                e.getDatasetVersion(),
                e.getStatus(),
                e.getRowsRead(),
                e.getRowsLoaded(),
                e.getTriggerMode(),
                e.getStartedAt(),
                e.getEndedAt()
        );
    }
}
