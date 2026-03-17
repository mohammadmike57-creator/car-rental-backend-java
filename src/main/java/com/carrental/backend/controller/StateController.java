package com.carrental.backend.controller;

import com.carrental.backend.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/state")
@RequiredArgsConstructor
public class StateController {

    private final StateService stateService;

    @GetMapping
    public ResponseEntity<String> getState() {
        return ResponseEntity.ok(stateService.getFullState());
    }

    @PostMapping
    public ResponseEntity<?> updateState(@RequestBody String updates) {
        stateService.mergeState(updates);
        return ResponseEntity.ok().build();
    }
}
