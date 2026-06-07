package com.creditrisk.ml.dataset;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "dataset_ingestion_jobs")
public class DatasetIngestionJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "source_path", nullable = false, length = 1024)
    private String sourcePath;

    @Column(name = "source_fingerprint", length = 128)
    private String sourceFingerprint;

    @Column(name = "dataset_version", nullable = false)
    private String datasetVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatasetIngestionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_mode", nullable = false)
    private DatasetIngestionTriggerMode triggerMode = DatasetIngestionTriggerMode.MANUAL;

    @Column(name = "generated_file", nullable = false)
    private Boolean generatedFile = Boolean.FALSE;

    @Column(name = "rows_read", nullable = false)
    private Long rowsRead = 0L;

    @Column(name = "rows_loaded", nullable = false)
    private Long rowsLoaded = 0L;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "log_summary", columnDefinition = "TEXT")
    private String logSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourceFingerprint() {
        return sourceFingerprint;
    }

    public void setSourceFingerprint(String sourceFingerprint) {
        this.sourceFingerprint = sourceFingerprint;
    }

    public String getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(String datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public DatasetIngestionStatus getStatus() {
        return status;
    }

    public void setStatus(DatasetIngestionStatus status) {
        this.status = status;
    }

    public DatasetIngestionTriggerMode getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(DatasetIngestionTriggerMode triggerMode) {
        this.triggerMode = triggerMode;
    }

    public Boolean getGeneratedFile() {
        return generatedFile;
    }

    public void setGeneratedFile(Boolean generatedFile) {
        this.generatedFile = generatedFile;
    }

    public Long getRowsRead() {
        return rowsRead;
    }

    public void setRowsRead(Long rowsRead) {
        this.rowsRead = rowsRead;
    }

    public Long getRowsLoaded() {
        return rowsLoaded;
    }

    public void setRowsLoaded(Long rowsLoaded) {
        this.rowsLoaded = rowsLoaded;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getLogSummary() {
        return logSummary;
    }

    public void setLogSummary(String logSummary) {
        this.logSummary = logSummary;
    }
}
