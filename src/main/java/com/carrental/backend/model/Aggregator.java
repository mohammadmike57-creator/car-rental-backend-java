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

@Embeddable
@Data
class AggregatorConnectionDetails {
    private String apiUrl;
    private String username;
    private String apiKey;
}

@Entity
@Table(name = "rate_plans")
@Data
class RatePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String startDate;
    private String endDate;
    // ... other fields as needed
}

@Entity
@Table(name = "rate_plan_submissions")
@Data
class RatePlanSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String submittedAt;
    // ... other fields
}

@Entity
@Table(name = "aggregator_extras")
@Data
class AggregatorExtra {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private double price;
}
