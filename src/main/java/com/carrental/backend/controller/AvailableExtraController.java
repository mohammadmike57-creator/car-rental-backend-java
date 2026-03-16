package com.carrental.backend.controller;

import com.carrental.backend.model.AvailableExtra;
import com.carrental.backend.repository.AvailableExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extras")
@RequiredArgsConstructor
public class AvailableExtraController {

    private final AvailableExtraRepository extraRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<List<AvailableExtra>> getAllExtras() {
        return ResponseEntity.ok(extraRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<AvailableExtra> getExtra(@PathVariable String id) {
        return extraRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_EXTRAS')")
    public ResponseEntity<AvailableExtra> createExtra(@RequestBody AvailableExtra extra) {
        AvailableExtra saved = extraRepository.save(extra);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_EXTRAS')")
    public ResponseEntity<AvailableExtra> updateExtra(@PathVariable String id, @RequestBody AvailableExtra extra) {
        if (!extraRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        extra.setId(id);
        AvailableExtra saved = extraRepository.save(extra);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_ADMIN_MANAGE_EXTRAS')")
    public ResponseEntity<Void> deleteExtra(@PathVariable String id) {
        if (!extraRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        extraRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
