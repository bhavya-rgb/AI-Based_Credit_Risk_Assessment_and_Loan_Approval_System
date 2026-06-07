package com.creditrisk.admin;

import java.util.Map;

public record AdminMetricsSummaryResponse(
        long applicants,
        long loanApplications,
        long riskAssessments,
        long loanDecisions,
        long models,
        String activeModelVersion,
        Map<String, Long> applicationsByStatus,
        Map<String, Long> decisionsByStatus
) {}
