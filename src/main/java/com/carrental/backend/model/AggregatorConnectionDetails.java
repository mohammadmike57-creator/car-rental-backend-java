package com.carrental.backend.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class AggregatorConnectionDetails {
    private String apiUrl;
    private String username;
    private String apiKey;
}
