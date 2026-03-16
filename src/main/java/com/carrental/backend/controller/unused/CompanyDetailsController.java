package com.carrental.backend.controller;

import com.carrental.backend.model.CompanyDetails;
import com.carrental.backend.repository.CompanyDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyDetailsController {

    private final CompanyDetailsRepository companyRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ADMIN_PANEL')")
    public ResponseEntity<CompanyDetails> getCompanyDetails() {
        List<CompanyDetails> list = companyRepository.findAll();
        if (list.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list.get(0));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_COMPANY_DETAILS')")
    public ResponseEntity<CompanyDetails> updateCompanyDetails(@RequestBody CompanyDetails details) {
        List<CompanyDetails> list = companyRepository.findAll();
        CompanyDetails toSave;
        if (list.isEmpty()) {
            toSave = details;
        } else {
            toSave = list.get(0);
            toSave.setName(details.getName());
            toSave.setSubName(details.getSubName());
            toSave.setAddress(details.getAddress());
            toSave.setPhone(details.getPhone());
            toSave.setEmail(details.getEmail());
            toSave.setTaxNumber(details.getTaxNumber());
            toSave.setRequirePaymentApproval(details.getRequirePaymentApproval());
        }
        CompanyDetails saved = companyRepository.save(toSave);
        return ResponseEntity.ok(saved);
    }
}
