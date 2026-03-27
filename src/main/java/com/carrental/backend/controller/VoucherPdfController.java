package com.carrental.backend.controller;

import com.carrental.backend.model.CompanyDetails;
import com.carrental.backend.model.Reservation;
import com.carrental.backend.repository.ReservationRepository;
import com.carrental.backend.repository.CompanyDetailsRepository;
import com.carrental.backend.service.VoucherPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/voucher")
public class VoucherPdfController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private VoucherPdfService pdfService;

    @Autowired
    private CompanyDetailsRepository companyDetailsRepository;

    @GetMapping("/pdf/{reservationId}")
    public ResponseEntity<byte[]> generateVoucherPdf(@PathVariable String reservationId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Reservation reservation = opt.get();

        // Get company details (assume only one record in the DB)
        CompanyDetails companyDetails = companyDetailsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company details not found"));

        byte[] pdfBytes;
        try {
            pdfBytes = pdfService.generateVoucherPdf(reservation, companyDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=voucher_" + reservationId + ".pdf")
                .body(pdfBytes);
    }
}
