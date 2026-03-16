package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rate_tiers")
@Data
public class RateTier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer fromDay;
    private Integer toDay;
    private Double dailyPrice;
}
