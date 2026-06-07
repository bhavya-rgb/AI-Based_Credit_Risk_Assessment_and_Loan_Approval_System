CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE applicants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    dob DATE NOT NULL,
    phone VARCHAR(32) NOT NULL,
    email VARCHAR(255) NOT NULL,
    government_id_masked VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    applicant_id BIGINT NOT NULL,
    loan_amount DECIMAL(15,2) NOT NULL,
    term_months INT NOT NULL,
    purpose VARCHAR(128) NOT NULL,
    annual_income DECIMAL(15,2) NOT NULL,
    employment_length_years INT NOT NULL,
    home_ownership VARCHAR(64) NOT NULL,
    verification_status VARCHAR(64) NOT NULL,
    dti DECIMAL(8,4) NOT NULL,
    existing_debt DECIMAL(15,2) NOT NULL,
    application_status VARCHAR(64) NOT NULL,
    submitted_at TIMESTAMP NULL,
    created_by BIGINT NULL,
    raw_feature_snapshot_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_applications_applicant FOREIGN KEY (applicant_id) REFERENCES applicants(id),
    CONSTRAINT fk_loan_applications_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE credit_profile_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_application_id BIGINT NOT NULL UNIQUE,
    fico_low INT NULL,
    fico_high INT NULL,
    inq_last_6mths INT NULL,
    delinq_2yrs INT NULL,
    open_acc INT NULL,
    pub_rec INT NULL,
    revol_bal DECIMAL(15,2) NULL,
    revol_util DECIMAL(8,4) NULL,
    total_acc INT NULL,
    mort_acc INT NULL,
    pub_rec_bankruptcies INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_profile_loan_application FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id)
);

CREATE TABLE ml_models (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(255) NOT NULL,
    model_version VARCHAR(128) NOT NULL UNIQUE,
    model_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    artifact_path VARCHAR(1024) NOT NULL,
    feature_schema_json TEXT NULL,
    hyperparams_json TEXT NULL,
    metrics_json TEXT NULL,
    trained_on_dataset_version VARCHAR(128) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    promoted_at TIMESTAMP NULL
);

CREATE TABLE risk_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_application_id BIGINT NOT NULL,
    model_id BIGINT NOT NULL,
    risk_score INT NOT NULL,
    default_probability DECIMAL(8,6) NOT NULL,
    risk_band VARCHAR(32) NOT NULL,
    recommendation VARCHAR(64) NOT NULL,
    top_reason_codes_json TEXT NULL,
    scored_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    feature_vector_hash VARCHAR(128) NULL,
    CONSTRAINT fk_risk_assessments_loan_application FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id),
    CONSTRAINT fk_risk_assessments_model FOREIGN KEY (model_id) REFERENCES ml_models(id)
);

CREATE TABLE loan_decisions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_application_id BIGINT NOT NULL,
    risk_assessment_id BIGINT NULL,
    decision_status VARCHAR(64) NOT NULL,
    approved_amount DECIMAL(15,2) NULL,
    approved_term_months INT NULL,
    interest_rate_offer DECIMAL(8,4) NULL,
    override_flag BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason VARCHAR(1000) NULL,
    decided_by BIGINT NULL,
    decided_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_decisions_loan_application FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id),
    CONSTRAINT fk_loan_decisions_risk_assessment FOREIGN KEY (risk_assessment_id) REFERENCES risk_assessments(id),
    CONSTRAINT fk_loan_decisions_user FOREIGN KEY (decided_by) REFERENCES users(id)
);

CREATE TABLE training_runs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_type VARCHAR(64) NOT NULL,
    dataset_version VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    config_json TEXT NULL,
    metrics_json TEXT NULL,
    error_message TEXT NULL
);

CREATE TABLE feature_selection_runs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_run_id BIGINT NOT NULL,
    algorithm VARCHAR(64) NOT NULL,
    selected_features_json TEXT NOT NULL,
    ca DECIMAL(10,6) NULL,
    dr DECIMAL(10,6) NULL,
    auc DECIMAL(10,6) NULL,
    fitness DECIMAL(10,6) NULL,
    seed BIGINT NULL,
    iterations INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feature_selection_training_run FOREIGN KEY (training_run_id) REFERENCES training_runs(id)
);

CREATE TABLE dataset_ingestion_jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_name VARCHAR(255) NOT NULL,
    source_path VARCHAR(1024) NOT NULL,
    dataset_version VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    rows_read BIGINT NOT NULL DEFAULT 0,
    rows_loaded BIGINT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    log_summary TEXT NULL
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action_type VARCHAR(128) NOT NULL,
    entity_type VARCHAR(128) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    before_json TEXT NULL,
    after_json TEXT NULL,
    ip_address VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (actor_user_id) REFERENCES users(id)
);

CREATE TABLE system_configs (
    config_key VARCHAR(128) PRIMARY KEY,
    config_value VARCHAR(1024) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loan_applications_status_submitted_at ON loan_applications(application_status, submitted_at);
CREATE INDEX idx_risk_assessments_loan_scored_at ON risk_assessments(loan_application_id, scored_at);
CREATE INDEX idx_loan_decisions_loan_decided_at ON loan_decisions(loan_application_id, decided_at);
CREATE INDEX idx_ml_models_status_created_at ON ml_models(status, created_at);
CREATE INDEX idx_training_runs_status_started_at ON training_runs(status, started_at);
CREATE INDEX idx_refresh_tokens_user_expires_at ON refresh_tokens(user_id, expires_at);

INSERT INTO roles(name) VALUES ('ADMIN');
INSERT INTO roles(name) VALUES ('LOAN_OFFICER');
INSERT INTO roles(name) VALUES ('RISK_ANALYST');

INSERT INTO system_configs(config_key, config_value) VALUES ('AUTO_APPROVE_PD_MAX', '0.12');
INSERT INTO system_configs(config_key, config_value) VALUES ('MANUAL_REVIEW_PD_MAX', '0.25');
INSERT INTO system_configs(config_key, config_value) VALUES ('AUTO_DECLINE_PD_MIN', '0.25');
