package com.carrental.backend.controller;

import com.carrental.backend.model.TrafficTicket;
import com.carrental.backend.repository.TrafficTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TrafficTicketController {

    private final TrafficTicketRepository ticketRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_TRAFFIC_TICKETS')")
    public ResponseEntity<List<TrafficTicket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_TRAFFIC_TICKETS')")
    public ResponseEntity<TrafficTicket> getTicket(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACTION_TRAFFIC_TICKETS_MANAGE')")
    public ResponseEntity<TrafficTicket> createTicket(@RequestBody TrafficTicket ticket) {
        TrafficTicket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_TRAFFIC_TICKETS_MANAGE')")
    public ResponseEntity<TrafficTicket> updateTicket(@PathVariable String id, @RequestBody TrafficTicket ticket) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticket.setId(id);
        TrafficTicket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTION_TRAFFIC_TICKETS_MANAGE')")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
