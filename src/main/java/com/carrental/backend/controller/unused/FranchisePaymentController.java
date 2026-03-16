package com.carrental.backend.controller;

import com.carrental.backend.model.FranchisePayment;
import com.carrental.backend.repository.FranchisePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/franchise-payments")
@RequiredArgsConstructor
public class FranchisePaymentController {

    private final FranchisePaymentRepository paymentRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_FINANCIALS_ACCOUNTING')")
    public ResponseEntity<List<FranchisePayment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_FINANCIALS_ACCOUNTING')")
    public ResponseEntity<FranchisePayment> getPayment(@PathVariable String id) {
        return paymentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_FINANCIALS_MANAGE_FRANCHISE_PAYMENTS')")
    public ResponseEntity<FranchisePayment> createPayment(@RequestBody FranchisePayment payment) {
        FranchisePayment saved = paymentRepository.save(payment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_FINANCIALS_MANAGE_FRANCHISE_PAYMENTS')")
    public ResponseEntity<FranchisePayment> updatePayment(@PathVariable String id, @RequestBody FranchisePayment payment) {
        if (!paymentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        payment.setId(id);
        FranchisePayment saved = paymentRepository.save(payment);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_FINANCIALS_MANAGE_FRANCHISE_PAYMENTS')")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        if (!paymentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        paymentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
