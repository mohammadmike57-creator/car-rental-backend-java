package com.carrental.backend.repository;

import com.carrental.backend.model.CompanyDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyDetailsRepository extends JpaRepository<CompanyDetails, String> {
}
