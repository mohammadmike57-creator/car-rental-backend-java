package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "sipp_rates")
@Data
public class SippRate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String sippCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sipp_rate_id")
    private List<RateTier> tiers;
}
