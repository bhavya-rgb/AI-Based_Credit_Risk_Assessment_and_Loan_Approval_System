package com.creditrisk.config;

import com.creditrisk.admin.DatasetImportService;
import com.creditrisk.ml.dataset.DatasetIngestionTriggerMode;
import com.creditrisk.ml.dataset.bootstrap.LendingClubSyntheticDatasetGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Order(30)
public class DatasetBootstrapRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatasetBootstrapRunner.class);

    private final DatasetBootstrapProperties properties;
    private final LendingClubSyntheticDatasetGenerator generator;
    private final DatasetImportService datasetImportService;

    public DatasetBootstrapRunner(DatasetBootstrapProperties properties,
                                  LendingClubSyntheticDatasetGenerator generator,
                                  DatasetImportService datasetImportService) {
        this.properties = properties;
        this.generator = generator;
        this.datasetImportService = datasetImportService;
    }

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            log.info("Dataset bootstrap is disabled");
            return;
        }

        try {
            Path path = Path.of(properties.getSourcePath()).toAbsolutePath().normalize();
            boolean generated = false;

            if (!Files.exists(path)) {
                if (!properties.isGenerateIfMissing()) {
                    String msg = "Dataset bootstrap source file not found and generateIfMissing=false: " + path;
                    if (properties.isFailOnError()) {
                        throw new IllegalStateException(msg);
                    }
                    log.warn(msg);
                    return;
                }
                int rows = Math.max(properties.getRows(), 1);
                generated = true;
                Path generatedPath = generator.generate(path, rows, properties.getRandomSeed());
                log.info("Generated synthetic Lending Club dataset at {} (rows={}, seed={})", generatedPath, rows, properties.getRandomSeed());
            }

            String fingerprint = datasetImportService.computeFingerprint(path);
            if (properties.isSkipIfAlreadyImported()) {
                var existing = datasetImportService.findLatestCompletedBySourcePathAndFingerprint(path.toString(), fingerprint);
                if (existing.isPresent()) {
                    log.info("Skipping dataset bootstrap import; existing completed import found (jobId={}, datasetVersion={}, sourcePath={})",
                            existing.get().getId(), existing.get().getDatasetVersion(), path);
                    return;
                }
            }

            var imported = datasetImportService.importDataset(
                    path,
                    properties.getSourceName(),
                    DatasetIngestionTriggerMode.STARTUP_BOOTSTRAP,
                    generated,
                    false
            );
            log.info("Startup dataset bootstrap import completed: datasetVersion={}, rowsLoaded={}, rowsRead={}, sourcePath={}",
                    imported.datasetVersion(), imported.rowsLoaded(), imported.rowsRead(), imported.sourcePath());
        } catch (Exception ex) {
            if (properties.isFailOnError()) {
                throw ex;
            }
            log.error("Dataset bootstrap failed but startup will continue: {}", ex.getMessage(), ex);
        }
    }
}
