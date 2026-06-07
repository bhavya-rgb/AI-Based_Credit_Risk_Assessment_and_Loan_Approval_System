package com.creditrisk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.dataset.bootstrap")
public class DatasetBootstrapProperties {
    private boolean enabled = true;
    private String sourcePath = "./data/lending_club_synthetic.csv";
    private String sourceName = "lending-club-synthetic";
    private boolean generateIfMissing = true;
    private int rows = 5000;
    private long randomSeed = 42L;
    private boolean skipIfAlreadyImported = true;
    private boolean failOnError = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public boolean isGenerateIfMissing() {
        return generateIfMissing;
    }

    public void setGenerateIfMissing(boolean generateIfMissing) {
        this.generateIfMissing = generateIfMissing;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public boolean isSkipIfAlreadyImported() {
        return skipIfAlreadyImported;
    }

    public void setSkipIfAlreadyImported(boolean skipIfAlreadyImported) {
        this.skipIfAlreadyImported = skipIfAlreadyImported;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
}
