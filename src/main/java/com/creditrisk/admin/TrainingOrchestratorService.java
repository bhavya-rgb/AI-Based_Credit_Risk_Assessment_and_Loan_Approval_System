package com.creditrisk.admin;

import com.creditrisk.common.ApiException;
import com.creditrisk.common.JsonSupport;
import com.creditrisk.ml.dataset.DatasetIngestionJobEntity;
import com.creditrisk.ml.featureselection.*;
import com.creditrisk.ml.model.ModelRegistryService;
import com.creditrisk.ml.model.ModelStatus;
import com.creditrisk.ml.optimization.HyperparameterOptimizer;
import com.creditrisk.ml.optimization.OptimizationResult;
import com.creditrisk.ml.preprocessing.FeaturePreprocessor;
import com.creditrisk.ml.preprocessing.PreparedDataset;
import com.creditrisk.ml.training.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TrainingOrchestratorService {
    private final TrainingRunRepository trainingRunRepository;
    private final FeatureSelectionRunRepository featureSelectionRunRepository;
    private final DatasetImportService datasetImportService;
    private final FeaturePreprocessor featurePreprocessor;
    private final FeatureSelector featureSelector;
    private final HyperparameterOptimizer hyperparameterOptimizer;
    private final RiskModelTrainer riskModelTrainer;
    private final ModelRegistryService modelRegistryService;
    private final JsonSupport jsonSupport;

    public TrainingOrchestratorService(TrainingRunRepository trainingRunRepository,
                                       FeatureSelectionRunRepository featureSelectionRunRepository,
                                       DatasetImportService datasetImportService,
                                       FeaturePreprocessor featurePreprocessor,
                                       FeatureSelector featureSelector,
                                       HyperparameterOptimizer hyperparameterOptimizer,
                                       RiskModelTrainer riskModelTrainer,
                                       ModelRegistryService modelRegistryService,
                                       JsonSupport jsonSupport) {
        this.trainingRunRepository = trainingRunRepository;
        this.featureSelectionRunRepository = featureSelectionRunRepository;
        this.datasetImportService = datasetImportService;
        this.featurePreprocessor = featurePreprocessor;
        this.featureSelector = featureSelector;
        this.hyperparameterOptimizer = hyperparameterOptimizer;
        this.riskModelTrainer = riskModelTrainer;
        this.modelRegistryService = modelRegistryService;
        this.jsonSupport = jsonSupport;
    }

    @Transactional
    public TrainingRunResponse startTraining(ModelTrainRequest request) {
        TrainingRunEntity run = new TrainingRunEntity();
        run.setRunType(request.mode());
        run.setDatasetVersion(request.datasetVersion());
        run.setStatus(TrainingRunStatus.RUNNING);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("featurePolicy", request.featurePolicy());
        config.put("optimizerConfig", request.optimizerConfig());
        config.put("xgboostSearchSpace", request.xgboostSearchSpace());
        config.put("evaluationStrategy", request.evaluationStrategy());
        run.setConfigJson(jsonSupport.toJson(config));
        run = trainingRunRepository.save(run);

        try {
            DatasetIngestionJobEntity datasetJob = datasetImportService.findLatestByDatasetVersion(request.datasetVersion());
            PreparedDataset prepared = featurePreprocessor.preprocess(java.nio.file.Path.of(datasetJob.getSourcePath()));

            FeatureSelectionMode mode = switch (request.mode()) {
                case BASELINE -> FeatureSelectionMode.BASELINE_CHI_SQUARE;
                case HSFSFOA, ISSA_XGBOOST -> FeatureSelectionMode.HSFSFOA;
            };
            FeatureSelectionResult fs = featureSelector.selectFeatures(prepared, mode);
            persistFeatureSelection(run, fs);

            OptimizationResult opt = null;
            if (request.mode() == TrainingRunType.ISSA_XGBOOST) {
                Map<String, Object> searchSpace = request.xgboostSearchSpace() == null ? defaultSearchSpace() : request.xgboostSearchSpace();
                opt = hyperparameterOptimizer.optimize(prepared, fs, searchSpace);
            } else {
                opt = new OptimizationResult("DEFAULT_XGBOOST_PARAMS", Map.of(
                        "learning_rate", 0.1,
                        "max_depth", 6,
                        "n_estimators", 300,
                        "gamma", 0.8,
                        "objective", "binary:logistic",
                        "eval_metric", "auc",
                        "subsample", 0.8,
                        "colsample_bytree", 0.8,
                        "seed", 42
                ), 0.90, 1, java.util.List.of());
            }

            TrainedModelBundle modelBundle = riskModelTrainer.train(prepared, fs, opt, request.mode());
            var model = modelRegistryService.registerModel(
                    request.mode().name().toLowerCase() + "-xgboost",
                    "XGBOOST_HEURISTIC_STUB",
                    prepared.datasetVersion(),
                    modelBundle.featureSchema(),
                    modelBundle.hyperparameters(),
                    modelBundle.metrics(),
                    modelBundle.artifactPayload(),
                    ModelStatus.VALIDATED
            );

            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.putAll(modelBundle.metrics());
            metrics.put("registeredModelId", model.getId());
            metrics.put("registeredModelVersion", model.getModelVersion());
            metrics.put("optimizer", opt.algorithm());
            run.setMetricsJson(jsonSupport.toJson(metrics));
            run.setStatus(TrainingRunStatus.COMPLETED);
            run.setEndedAt(Instant.now());
            trainingRunRepository.save(run);
            return TrainingRunResponse.of(run, metrics);
        } catch (Exception ex) {
            run.setStatus(TrainingRunStatus.FAILED);
            run.setEndedAt(Instant.now());
            run.setErrorMessage(ex.getMessage());
            trainingRunRepository.save(run);
            if (ex instanceof ApiException api) {
                throw api;
            }
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Training failed: " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public TrainingRunResponse getRun(Long id) {
        TrainingRunEntity entity = trainingRunRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Training run not found"));
        Map<String, Object> metrics = entity.getMetricsJson() == null ? Map.of() : jsonSupport.fromJson(entity.getMetricsJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        return TrainingRunResponse.of(entity, metrics);
    }

    private void persistFeatureSelection(TrainingRunEntity run, FeatureSelectionResult fs) {
        FeatureSelectionRunEntity entity = new FeatureSelectionRunEntity();
        entity.setTrainingRun(run);
        entity.setAlgorithm(fs.algorithm());
        entity.setSelectedFeaturesJson(jsonSupport.toJson(fs.selectedFeatures()));
        entity.setCa(BigDecimal.valueOf(fs.ca()));
        entity.setDr(BigDecimal.valueOf(fs.dr()));
        entity.setAuc(BigDecimal.valueOf(fs.auc()));
        entity.setFitness(BigDecimal.valueOf(fs.fitness()));
        entity.setSeed(fs.seed());
        entity.setIterations(fs.iterations());
        featureSelectionRunRepository.save(entity);
    }

    private Map<String, Object> defaultSearchSpace() {
        return Map.of(
                "learning_rate", java.util.List.of(0.01, 0.30),
                "max_depth", java.util.List.of(3, 10),
                "n_estimators", java.util.List.of(100, 600),
                "gamma", java.util.List.of(0.0, 5.0)
        );
    }
}
