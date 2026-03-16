package com.carrental.backend.controller;

import com.carrental.backend.model.ActivityLog;
import com.carrental.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-log")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogRepository logRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_SYSTEM_ACTIVITY_LOG')")
    public ResponseEntity<List<ActivityLog>> getAllLogs() {
        return ResponseEntity.ok(logRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_SYSTEM_ACTIVITY_LOG')")
    public ResponseEntity<ActivityLog> getLog(@PathVariable String id) {
        return logRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
