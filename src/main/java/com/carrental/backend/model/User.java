package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    private String fullName;
    private String username;

    private String password; // hashed

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<UserPermission> permissions;

    private String nationalId;
    private String sscNumber;
    private String hireDate;
    private String position;
    private Double baseSalaryJOD;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Boolean isOnline;
    private LocalDateTime lastSeen;
    private String deviceType;
    private Boolean webAppAccess;
    private LocalDateTime passwordLastChanged;
}
