package com.creditrisk.admin;

import com.creditrisk.common.ApiException;
import com.creditrisk.ml.dataset.DatasetIngestionJobEntity;
import com.creditrisk.ml.dataset.DatasetIngestionJobRepository;
import com.creditrisk.ml.dataset.DatasetIngestionStatus;
import com.creditrisk.ml.dataset.DatasetIngestionTriggerMode;
import com.creditrisk.ml.preprocessing.FeaturePreprocessor;
import com.creditrisk.ml.preprocessing.PreparedDataset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
public class DatasetImportService {
    private final DatasetIngestionJobRepository repository;
    private final FeaturePreprocessor featurePreprocessor;

    public DatasetImportService(DatasetIngestionJobRepository repository, FeaturePreprocessor featurePreprocessor) {
        this.repository = repository;
        this.featurePreprocessor = featurePreprocessor;
    }

    @Transactional
    public DatasetIngestionJobResponse importDataset(DatasetImportRequest request) {
        Path path = Path.of(request.sourcePath());
        String sourceName = request.sourceName() == null || request.sourceName().isBlank()
                ? path.getFileName().toString()
                : request.sourceName();
        return importDataset(path, sourceName, DatasetIngestionTriggerMode.MANUAL, false, false);
    }

    @Transactional
    public DatasetIngestionJobResponse importDataset(Path sourcePath,
                                                     String sourceName,
                                                     DatasetIngestionTriggerMode triggerMode,
                                                     boolean generatedFile,
                                                     boolean skipIfSameFingerprint) {
        if (sourcePath == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Source file not found: null");
        }
        Path path = sourcePath.toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Source file not found: " + path);
        }

        String fingerprint = sha256Hex(path);
        if (skipIfSameFingerprint) {
            Optional<DatasetIngestionJobEntity> existing = findLatestCompletedBySourcePathAndFingerprint(path.toString(), fingerprint);
            if (existing.isPresent()) {
                return DatasetIngestionJobResponse.from(existing.get());
            }
        }

        DatasetIngestionJobEntity job = new DatasetIngestionJobEntity();
        job.setSourcePath(path.toString());
        job.setSourceFingerprint(fingerprint);
        job.setSourceName(sourceName == null || sourceName.isBlank() ? path.getFileName().toString() : sourceName);
        job.setDatasetVersion("PENDING");
        job.setStatus(DatasetIngestionStatus.RUNNING);
        job.setTriggerMode(triggerMode == null ? DatasetIngestionTriggerMode.MANUAL : triggerMode);
        job.setGeneratedFile(generatedFile);
        job = repository.save(job);

        try {
            PreparedDataset prepared = featurePreprocessor.preprocess(path);
            job.setDatasetVersion(prepared.datasetVersion());
            job.setRowsRead(prepared.rowsRead());
            job.setRowsLoaded(prepared.rowsLoaded());
            job.setStatus(DatasetIngestionStatus.COMPLETED);
            job.setLogSummary("Imported and profiled dataset: selectedColumns="
                    + prepared.selectedColumns().size()
                    + ", droppedColumns="
                    + prepared.droppedColumns().size());
        } catch (Exception ex) {
            job.setStatus(DatasetIngestionStatus.FAILED);
            job.setLogSummary("FAILED: " + ex.getMessage());
            throw ex;
        } finally {
            job.setEndedAt(Instant.now());
            repository.save(job);
        }

        return DatasetIngestionJobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public DatasetIngestionJobEntity findLatestByDatasetVersion(String datasetVersion) {
        return repository.findAll().stream()
                .filter(j -> datasetVersion.equals(j.getDatasetVersion()))
                .max(java.util.Comparator.comparing(DatasetIngestionJobEntity::getStartedAt))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Dataset ingestion job not found for version: " + datasetVersion));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> latestIngestionSummary() {
        return repository.findTopByOrderByStartedAtDesc()
                .<Map<String, Object>>map(job -> {
                    Map<String, Object> out = new java.util.LinkedHashMap<>();
                    out.put("jobId", job.getId());
                    out.put("datasetVersion", job.getDatasetVersion());
                    out.put("status", job.getStatus().name());
                    out.put("rowsRead", job.getRowsRead());
                    out.put("rowsLoaded", job.getRowsLoaded());
                    return out;
                })
                .orElseGet(java.util.LinkedHashMap::new);
    }

    @Transactional(readOnly = true)
    public Optional<DatasetIngestionJobEntity> findLatestCompletedBySourcePathAndFingerprint(String sourcePath, String sourceFingerprint) {
        if (sourcePath == null || sourceFingerprint == null || sourceFingerprint.isBlank()) {
            return Optional.empty();
        }
        return repository.findFirstBySourcePathAndSourceFingerprintAndStatusOrderByStartedAtDesc(
                sourcePath, sourceFingerprint, DatasetIngestionStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public String computeFingerprint(Path sourcePath) {
        if (sourcePath == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Source file not found: null");
        }
        Path path = sourcePath.toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Source file not found: " + path);
        }
        return sha256Hex(path);
    }

    @Transactional(readOnly = true)
    public Optional<LatestDatasetSummaryResponse> latestDatasetSummary() {
        return repository.findTopByOrderByStartedAtDesc().map(LatestDatasetSummaryResponse::from);
    }

    private String sha256Hex(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                digest.update(buf, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read dataset for fingerprint: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
