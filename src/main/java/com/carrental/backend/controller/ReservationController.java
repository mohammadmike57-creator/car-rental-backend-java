package com.carrental.backend.controller;

import com.carrental.backend.model.Reservation;
import com.carrental.backend.model.User;
import com.carrental.backend.repository.ReservationRepository;
import com.carrental.backend.repository.UserRepository;
import com.carrental.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private User getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable String id) {
        return reservationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    // Temporarily removed @PreAuthorize to debug
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser != null) {
                reservation.setCreatedBy(currentUser);
            }
            if (reservation.getPersonName() == null || reservation.getStartDate() == null || reservation.getEndDate() == null) {
                return ResponseEntity.badRequest().body("Missing required fields: personName, startDate, endDate");
            }
            Reservation saved = reservationRepository.save(reservation);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating reservation: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable String id, @RequestBody Reservation reservation, HttpServletRequest request) {
        try {
            if (!reservationRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            reservation.setId(id);
            User currentUser = getCurrentUser(request);
            if (currentUser != null) {
                reservation.setLastEditedBy(currentUser);
            }
            Reservation saved = reservationRepository.save(reservation);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating reservation: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable String id) {
        if (!reservationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reservationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
