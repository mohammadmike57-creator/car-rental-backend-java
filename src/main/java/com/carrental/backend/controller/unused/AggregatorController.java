package com.carrental.backend.controller;

import com.carrental.backend.model.Aggregator;
import com.carrental.backend.repository.AggregatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aggregators")
@RequiredArgsConstructor
public class AggregatorController {

    private final AggregatorRepository aggregatorRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<List<Aggregator>> getAllAggregators() {
        return ResponseEntity.ok(aggregatorRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<Aggregator> getAggregator(@PathVariable String id) {
        return aggregatorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<Aggregator> createAggregator(@RequestBody Aggregator aggregator) {
        Aggregator saved = aggregatorRepository.save(aggregator);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<Aggregator> updateAggregator(@PathVariable String id, @RequestBody Aggregator aggregator) {
        if (!aggregatorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        aggregator.setId(id);
        Aggregator saved = aggregatorRepository.save(aggregator);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_AGGREGATOR_SETUP')")
    public ResponseEntity<Void> deleteAggregator(@PathVariable String id) {
        if (!aggregatorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        aggregatorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
