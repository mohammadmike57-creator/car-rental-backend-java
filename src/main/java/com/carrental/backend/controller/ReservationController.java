package com.carrental.backend.controller;

import com.carrental.backend.model.Reservation;
import com.carrental.backend.model.User;
import com.carrental.backend.repository.ReservationRepository;
import com.carrental.backend.repository.UserRepository;
import com.carrental.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String month) {
        List<Reservation> reservations;
        if (year != null && month != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = start.plusYears(1);
            reservations = reservationRepository.findAllByStartDateBetween(start, end);
            // TODO: refine month filtering if needed
        } else {
            reservations = reservationRepository.findAll();
        }
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RESERVATIONS')")
    public ResponseEntity<Reservation> getReservation(@PathVariable String id) {
        return reservationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_RESERVATIONS_ADD')")
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser != null) {
            reservation.setCreatedBy(currentUser);
        }
        Reservation saved = reservationRepository.save(reservation);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_RESERVATIONS_EDIT')")
    public ResponseEntity<Reservation> updateReservation(@PathVariable String id, @RequestBody Reservation reservation, HttpServletRequest request) {
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
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_RESERVATIONS_DELETE')")
    public ResponseEntity<Void> deleteReservation(@PathVariable String id) {
        if (!reservationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reservationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
