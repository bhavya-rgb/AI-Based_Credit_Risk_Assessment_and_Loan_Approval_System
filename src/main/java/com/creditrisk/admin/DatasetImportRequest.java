package com.creditrisk.admin;

import jakarta.validation.constraints.NotBlank;

public record DatasetImportRequest(
        @NotBlank String sourcePath,
        String sourceName
) {}
