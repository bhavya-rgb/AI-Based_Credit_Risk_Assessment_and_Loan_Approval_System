package com.creditrisk.ml.preprocessing;

import java.util.List;
import java.util.Map;

public record PreparedDataset(
        String datasetVersion,
        String sourcePath,
        long rowsRead,
        long rowsLoaded,
        List<String> originalColumns,
        List<String> selectedColumns,
        List<String> droppedColumns,
        Map<String, Long> missingCounts,
        Map<Integer, Long> labelDistribution,
        Map<String, Object> preprocessingSchema
) {}
