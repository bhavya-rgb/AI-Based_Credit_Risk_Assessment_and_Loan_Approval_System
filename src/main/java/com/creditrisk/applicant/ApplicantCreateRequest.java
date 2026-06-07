package com.creditrisk.applicant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record ApplicantCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Past LocalDate dob,
        @NotBlank String phone,
        @Email @NotBlank String email,
        @NotBlank String governmentId
) {}
