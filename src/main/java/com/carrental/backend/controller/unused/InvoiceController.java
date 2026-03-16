package com.carrental.backend.controller;

import com.carrental.backend.model.InvoiceData;
import com.carrental.backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_REPORTS_INVOICE_GENERATION')")
    public ResponseEntity<List<InvoiceData>> getAllInvoices() {
        return ResponseEntity.ok(invoiceRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_REPORTS_INVOICE_GENERATION')")
    public ResponseEntity<InvoiceData> getInvoice(@PathVariable String id) {
        return invoiceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_REPORTS_INVOICE_GENERATION')")
    public ResponseEntity<InvoiceData> createInvoice(@RequestBody InvoiceData invoice) {
        InvoiceData saved = invoiceRepository.save(invoice);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_REPORTS_INVOICE_GENERATION')")
    public ResponseEntity<InvoiceData> updateInvoice(@PathVariable String id, @RequestBody InvoiceData invoice) {
        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        invoice.setId(id);
        InvoiceData saved = invoiceRepository.save(invoice);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_REPORTS_INVOICE_GENERATION')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable String id) {
        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        invoiceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
