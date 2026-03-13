package com.carrental.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String personName;
    private String contactNumber;
    private String bookingId;

    @ManyToOne
    private RentalSource source;

    private LocalDate bookingDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reservationVehicle;

    @ManyToOne
    private Vehicle assignedVehicle;

    private String locationName;
    private String notes;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Double amount;
    private Double baseAmount;
    private Boolean voucherSubmitted;
    private Boolean dropOffCompleted;
    private String customerEmail;

    // Pickup fields
    private LocalDateTime pickupDateTime;
    private String pickupRenterSignature;
    private String pickupAgentSignature;
    private String pickupAgentName;
    private Integer pickupFuelLevel;
    private Integer pickupKmOut;
    private String pickupNotes;

    // Drop-off fields
    private LocalDateTime dropOffDateTime;
    private String dropOffRenterSignature;
    private String dropOffAgentSignature;
    private String dropOffAgentName;
    private Integer dropOffFuelLevel;
    private Integer dropOffKmIn;
    private String dropOffNotes;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User lastEditedBy;
}
