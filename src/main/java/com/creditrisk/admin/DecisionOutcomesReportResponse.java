package com.creditrisk.admin;

import java.util.Map;

public record DecisionOutcomesReportResponse(
        Map<String, Long> decisionsByStatus,
        Map<String, Long> recommendationsByType,
        long totalDecisions
) {}
