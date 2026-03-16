package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "aggregators")
@Data
public class Aggregator {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Embedded
    private AggregatorConnectionDetails connectionDetails;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "aggregator_id")
    private List<RatePlan> currentRatePlans;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "aggregator_id")
    private List<RatePlanSubmission> ratePlanHistory;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "aggregator_id")
    private List<AggregatorExtra> extras;

    @Lob
    private String termsAndConditions;
}
