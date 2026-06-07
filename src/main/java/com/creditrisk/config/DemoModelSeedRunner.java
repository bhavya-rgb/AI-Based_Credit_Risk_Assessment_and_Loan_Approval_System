package com.creditrisk.config;

import com.creditrisk.ml.model.ModelRegistryService;
import com.creditrisk.ml.model.ModelStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

@Configuration
public class DemoModelSeedRunner {

    @Bean
    @Order(20)
    CommandLineRunner seedDemoModel(ModelRegistryService modelRegistryService) {
        return args -> {
            boolean hasActive = modelRegistryService.listModels().stream().anyMatch(m -> m.getStatus() == ModelStatus.ACTIVE);
            if (hasActive) {
                return;
            }
            var model = modelRegistryService.registerModel(
                    "demo-baseline-xgboost",
                    "XGBOOST_HEURISTIC_STUB",
                    "DEMO_BOOTSTRAP",
                    Map.of(
                            "selectedFeatures", List.of("dti", "fico_inverse", "revol_util", "inq_last_6mths", "loan_amount_k"),
                            "preprocessingSchema", Map.of("mode", "bootstrap-demo")
                    ),
                    Map.of(
                            "learning_rate", 0.1,
                            "max_depth", 6,
                            "n_estimators", 300,
                            "gamma", 0.8,
                            "objective", "binary:logistic",
                            "eval_metric", "auc"
                    ),
                    Map.of(
                            "accuracy", 0.923,
                            "auc", 0.878,
                            "precision", 0.954,
                            "recall", 0.676,
                            "f1", 0.792,
                            "seeded", true
                    ),
                    Map.of(
                            "weights", Map.ofEntries(
                                    Map.entry("bias", -1.75),
                                    Map.entry("loan_amount_k", 0.008),
                                    Map.entry("annual_income_k", -0.004),
                                    Map.entry("dti", 2.15),
                                    Map.entry("existing_debt_k", 0.004),
                                    Map.entry("term_years", 0.31),
                                    Map.entry("employment_years", -0.07),
                                    Map.entry("fico_inverse", 3.7),
                                    Map.entry("inq_last_6mths", 0.11),
                                    Map.entry("delinq_2yrs", 0.23),
                                    Map.entry("revol_util", 1.05),
                                    Map.entry("bankruptcies", 0.38)
                            )
                    ),
                    ModelStatus.VALIDATED
            );
            modelRegistryService.promote(model.getId());
        };
    }
}
