package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_data")
@Data
public class AppData {
    @Id
    private String id = "main";

    @Lob
    @Column(columnDefinition = "text")
    private String data; // JSON string of AllData
}
