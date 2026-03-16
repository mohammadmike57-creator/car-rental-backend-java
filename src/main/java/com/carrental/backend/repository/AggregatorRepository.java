package com.carrental.backend.repository;

import com.carrental.backend.model.Aggregator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AggregatorRepository extends JpaRepository<Aggregator, String> {
}
