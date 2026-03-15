package com.carrental.backend.controller;

import com.carrental.backend.model.StopSale;
import com.carrental.backend.repository.StopSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stop-sales")
@RequiredArgsConstructor
public class StopSaleController {

    private final StopSaleRepository stopSaleRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<List<StopSale>> getAllStopSales() {
        return ResponseEntity.ok(stopSaleRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<StopSale> getStopSale(@PathVariable String id) {
        return stopSaleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<StopSale> createStopSale(@RequestBody StopSale stopSale) {
        StopSale saved = stopSaleRepository.save(stopSale);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<StopSale> updateStopSale(@PathVariable String id, @RequestBody StopSale stopSale) {
        if (!stopSaleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stopSale.setId(id);
        StopSale saved = stopSaleRepository.save(stopSale);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<Void> deleteStopSale(@PathVariable String id) {
        if (!stopSaleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stopSaleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
