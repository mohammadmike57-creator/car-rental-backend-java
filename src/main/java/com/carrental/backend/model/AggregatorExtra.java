package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "aggregator_extras")
@Data
public class AggregatorExtra {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name; // Links to AvailableExtra id
    private Double price;
}
