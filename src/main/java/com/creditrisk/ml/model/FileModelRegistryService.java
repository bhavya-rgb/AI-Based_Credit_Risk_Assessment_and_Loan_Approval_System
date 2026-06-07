package com.creditrisk.ml.model;

import com.creditrisk.common.ApiException;
import com.creditrisk.common.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileModelRegistryService implements ModelRegistryService {
    private final MlModelRepository repository;
    private final JsonSupport jsonSupport;
    private final Path artifactsDir;

    public FileModelRegistryService(MlModelRepository repository,
                                    JsonSupport jsonSupport,
                                    @Value("${app.model.artifacts-path}") String artifactsPath) {
        this.repository = repository;
        this.jsonSupport = jsonSupport;
        this.artifactsDir = Path.of(artifactsPath);
    }

    @Override
    @Transactional(readOnly = true)
    public MlModelEntity getActiveModel() {
        return repository.findFirstByStatusOrderByPromotedAtDescCreatedAtDesc(ModelStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No active model available"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MlModelEntity> listModels() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public MlModelEntity promote(Long modelId) {
        MlModelEntity model = repository.findById(modelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Model not found"));
        repository.findAll().stream().filter(m -> m.getStatus() == ModelStatus.ACTIVE).forEach(m -> m.setStatus(ModelStatus.RETIRED));
        model.setStatus(ModelStatus.ACTIVE);
        model.setPromotedAt(Instant.now());
        repository.save(model);
        return model;
    }

    @Override
    @Transactional
    public MlModelEntity retire(Long modelId) {
        MlModelEntity model = repository.findById(modelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Model not found"));
        model.setStatus(ModelStatus.RETIRED);
        repository.save(model);
        return model;
    }

    @Override
    @Transactional
    public MlModelEntity registerModel(String modelName, String modelType, String datasetVersion,
                                       Map<String, Object> featureSchema, Map<String, Object> hyperparams,
                                       Map<String, Object> metrics, Map<String, Object> artifactPayload,
                                       ModelStatus status) {
        try {
            Files.createDirectories(artifactsDir);
            String version = modelName.replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase() + "-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now());
            Path artifactPath = artifactsDir.resolve(version + ".json");
            Map<String, Object> fullArtifact = new LinkedHashMap<>();
            fullArtifact.put("modelName", modelName);
            fullArtifact.put("modelType", modelType);
            fullArtifact.put("modelVersion", version);
            fullArtifact.put("datasetVersion", datasetVersion);
            fullArtifact.put("featureSchema", featureSchema);
            fullArtifact.put("hyperparams", hyperparams);
            fullArtifact.put("metrics", metrics);
            fullArtifact.put("payload", artifactPayload);
            Files.writeString(artifactPath, jsonSupport.toJson(fullArtifact));

            MlModelEntity model = new MlModelEntity();
            model.setModelName(modelName);
            model.setModelType(modelType);
            model.setModelVersion(version);
            model.setStatus(status);
            model.setArtifactPath(artifactPath.toString());
            model.setFeatureSchemaJson(jsonSupport.toJson(featureSchema));
            model.setHyperparamsJson(jsonSupport.toJson(hyperparams));
            model.setMetricsJson(jsonSupport.toJson(metrics));
            model.setTrainedOnDatasetVersion(datasetVersion);
            return repository.save(model);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist model artifact: " + e.getMessage());
        }
    }
}
