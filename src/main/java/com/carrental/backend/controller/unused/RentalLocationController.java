package com.carrental.backend.controller;

import com.carrental.backend.model.RentalLocation;
import com.carrental.backend.repository.RentalLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class RentalLocationController {

    private final RentalLocationRepository locationRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<List<RentalLocation>> getAllLocations() {
        return ResponseEntity.ok(locationRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<RentalLocation> getLocation(@PathVariable String id) {
        return locationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_LOCATIONS')")
    public ResponseEntity<RentalLocation> createLocation(@RequestBody RentalLocation location) {
        RentalLocation saved = locationRepository.save(location);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_LOCATIONS')")
    public ResponseEntity<RentalLocation> updateLocation(@PathVariable String id, @RequestBody RentalLocation location) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        location.setId(id);
        RentalLocation saved = locationRepository.save(location);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_LOCATIONS')")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        locationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
