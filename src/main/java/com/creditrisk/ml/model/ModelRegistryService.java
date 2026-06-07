package com.creditrisk.ml.model;

import java.util.List;
import java.util.Map;

public interface ModelRegistryService {
    MlModelEntity getActiveModel();
    List<MlModelEntity> listModels();
    MlModelEntity promote(Long modelId);
    MlModelEntity retire(Long modelId);
    MlModelEntity registerModel(String modelName, String modelType, String datasetVersion,
                                Map<String, Object> featureSchema, Map<String, Object> hyperparams,
                                Map<String, Object> metrics, Map<String, Object> artifactPayload,
                                ModelStatus status);
}
