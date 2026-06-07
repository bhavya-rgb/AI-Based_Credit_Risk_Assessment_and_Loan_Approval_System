package com.creditrisk.admin;

import com.creditrisk.ml.model.ModelRegistryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final DatasetImportService datasetImportService;
    private final TrainingOrchestratorService trainingOrchestratorService;
    private final ModelRegistryService modelRegistryService;
    private final AdminQueryService adminQueryService;

    public AdminController(DatasetImportService datasetImportService,
                           TrainingOrchestratorService trainingOrchestratorService,
                           ModelRegistryService modelRegistryService,
                           AdminQueryService adminQueryService) {
        this.datasetImportService = datasetImportService;
        this.trainingOrchestratorService = trainingOrchestratorService;
        this.modelRegistryService = modelRegistryService;
        this.adminQueryService = adminQueryService;
    }

    @PostMapping("/datasets/import")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST')")
    public DatasetIngestionJobResponse importDataset(@Valid @RequestBody DatasetImportRequest request) {
        return datasetImportService.importDataset(request);
    }

    @GetMapping("/datasets/latest")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST')")
    public LatestDatasetSummaryResponse latestDataset() {
        return datasetImportService.latestDatasetSummary().orElse(null);
    }

    @PostMapping("/training-runs")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST')")
    public TrainingRunResponse startTraining(@Valid @RequestBody ModelTrainRequest request) {
        return trainingOrchestratorService.startTraining(request);
    }

    @GetMapping("/training-runs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST')")
    public TrainingRunResponse getTrainingRun(@PathVariable Long id) {
        return trainingOrchestratorService.getRun(id);
    }

    @GetMapping("/models")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST')")
    public List<MlModelResponse> listModels() {
        return modelRegistryService.listModels().stream().map(MlModelResponse::from).toList();
    }

    @PostMapping("/models/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public MlModelResponse promote(@PathVariable Long id) {
        return MlModelResponse.from(modelRegistryService.promote(id));
    }

    @PostMapping("/models/{id}/retire")
    @PreAuthorize("hasRole('ADMIN')")
    public MlModelResponse retire(@PathVariable Long id) {
        return MlModelResponse.from(modelRegistryService.retire(id));
    }

    @GetMapping("/metrics/summary")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST','LOAN_OFFICER')")
    public AdminMetricsSummaryResponse metricsSummary() {
        return adminQueryService.metricsSummary();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public com.creditrisk.common.PageResponse<AuditLogResponse> auditLogs(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        return adminQueryService.auditLogs(page, size);
    }

    @GetMapping("/reports/decision-outcomes")
    @PreAuthorize("hasAnyRole('ADMIN','RISK_ANALYST')")
    public DecisionOutcomesReportResponse decisionOutcomes() {
        return adminQueryService.decisionOutcomes();
    }
}
