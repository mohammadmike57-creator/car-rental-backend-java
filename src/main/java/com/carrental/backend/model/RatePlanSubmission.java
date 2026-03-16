package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "rate_plan_submissions")
@Data
public class RatePlanSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String submittedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "submission_id")
    private List<RatePlan> ratePlans;
}
