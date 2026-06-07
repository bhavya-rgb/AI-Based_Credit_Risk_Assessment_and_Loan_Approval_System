package com.creditrisk.applicant;

import java.time.Instant;
import java.time.LocalDate;

public record ApplicantResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDate dob,
        String phone,
        String email,
        String governmentIdMasked,
        Instant createdAt
) {
    public static ApplicantResponse from(ApplicantEntity entity) {
        return new ApplicantResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDob(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getGovernmentIdMasked(),
                entity.getCreatedAt()
        );
    }
}
