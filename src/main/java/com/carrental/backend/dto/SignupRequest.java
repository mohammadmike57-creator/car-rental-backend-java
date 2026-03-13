package com.carrental.backend.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String fullName;
    private String nationalId;
    private String hireDate;
    private String position;
    private Double baseSalaryJOD;
}
