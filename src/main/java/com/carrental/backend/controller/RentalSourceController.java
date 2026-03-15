package com.carrental.backend.controller;

import com.carrental.backend.model.RentalSource;
import com.carrental.backend.repository.RentalSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class RentalSourceController {

    private final RentalSourceRepository sourceRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<List<RentalSource>> getAllSources() {
        return ResponseEntity.ok(sourceRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<RentalSource> getSource(@PathVariable String id) {
        return sourceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_SOURCES')")
    public ResponseEntity<RentalSource> createSource(@RequestBody RentalSource source) {
        RentalSource saved = sourceRepository.save(source);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_SOURCES')")
    public ResponseEntity<RentalSource> updateSource(@PathVariable String id, @RequestBody RentalSource source) {
        if (!sourceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        source.setId(id);
        RentalSource saved = sourceRepository.save(source);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_SOURCES')")
    public ResponseEntity<Void> deleteSource(@PathVariable String id) {
        if (!sourceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sourceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
