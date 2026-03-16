package com.carrental.backend.controller;

import com.carrental.backend.model.VehicleDamage;
import com.carrental.backend.repository.VehicleDamageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/damages")
@RequiredArgsConstructor
public class VehicleDamageController {

    private final VehicleDamageRepository damageRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_VEHICLE_DAMAGES')")
    public ResponseEntity<List<VehicleDamage>> getAllDamages() {
        return ResponseEntity.ok(damageRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_VEHICLE_DAMAGES')")
    public ResponseEntity<VehicleDamage> getDamage(@PathVariable String id) {
        return damageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_VEHICLE_DAMAGES_MANAGE')")
    public ResponseEntity<VehicleDamage> createDamage(@RequestBody VehicleDamage damage) {
        VehicleDamage saved = damageRepository.save(damage);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_VEHICLE_DAMAGES_MANAGE')")
    public ResponseEntity<VehicleDamage> updateDamage(@PathVariable String id, @RequestBody VehicleDamage damage) {
        if (!damageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        damage.setId(id);
        VehicleDamage saved = damageRepository.save(damage);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_VEHICLE_DAMAGES_MANAGE')")
    public ResponseEntity<Void> deleteDamage(@PathVariable String id) {
        if (!damageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        damageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
