ALTER TABLE dataset_ingestion_jobs ADD COLUMN source_fingerprint VARCHAR(128) NULL;
ALTER TABLE dataset_ingestion_jobs ADD COLUMN trigger_mode VARCHAR(32) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE dataset_ingestion_jobs ADD COLUMN generated_file BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_dataset_ingestion_fingerprint_status_started
    ON dataset_ingestion_jobs(source_fingerprint, status, started_at);
