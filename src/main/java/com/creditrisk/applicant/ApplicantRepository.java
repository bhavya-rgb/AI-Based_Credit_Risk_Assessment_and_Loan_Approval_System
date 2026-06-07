package com.creditrisk.applicant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<ApplicantEntity, Long> {
}
