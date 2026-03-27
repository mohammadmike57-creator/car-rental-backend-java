package com.carrental.backend.service;

import com.carrental.backend.model.CompanyDetails;
import com.carrental.backend.model.Reservation;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Service
public class VoucherPdfService {

    public byte[] generateVoucherPdf(Reservation reservation, CompanyDetails companyDetails) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDoc);
        document.setMargins(36, 36, 36, 36); // 0.5 inch margins

        PdfFont normalFont = PdfFontFactory.createFont();
        PdfFont boldFont = PdfFontFactory.createFont();

        // Logo (if present)
        if (companyDetails.getLogoBase64() != null && !companyDetails.getLogoBase64().isEmpty()) {
            try {
                byte[] logoBytes = Base64.getDecoder().decode(companyDetails.getLogoBase64());
                Image logo = new Image(ImageDataFactory.create(logoBytes));
                logo.setWidth(60);
                logo.setHeight(60);
                logo.setHorizontalAlignment(TextAlignment.CENTER);
                document.add(logo);
            } catch (Exception e) {
                // ignore
            }
        }

        // Company header
        Paragraph companyName = new Paragraph(companyDetails.getName())
                .setFont(boldFont).setFontSize(18).setTextAlignment(TextAlignment.CENTER);
        document.add(companyName);
        document.add(new Paragraph(companyDetails.getAddress()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Tel: " + companyDetails.getPhone() + " | Email: " + companyDetails.getEmail())
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Tax Number: " + companyDetails.getTaxNumber())
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        // Title
        Paragraph title = new Paragraph("RENTAL AGREEMENT / VOUCHER")
                .setFont(boldFont).setFontSize(16).setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("Booking ID: " + reservation.getBookingId())
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        // Renter Information
        document.add(new Paragraph("Renter Information").setFont(boldFont));
        Table renterTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        renterTable.useAllAvailableWidth();
        addRow(renterTable, "Name:", reservation.getPersonName());
        addRow(renterTable, "Contact:", reservation.getContactNumber());
        addRow(renterTable, "Email:", reservation.getCustomerEmail());
        addRow(renterTable, "Booking Source:", reservation.getSource());
        addRow(renterTable, "Booking Date:", reservation.getBookingDate());
        document.add(renterTable);
        document.add(new Paragraph(" "));

        // Vehicle & Rental Details
        document.add(new Paragraph("Vehicle & Rental Details").setFont(boldFont));
        Table vehicleTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        vehicleTable.useAllAvailableWidth();
        addRow(vehicleTable, "Vehicle Model:", reservation.getCarModel());
        addRow(vehicleTable, "License Plate:", reservation.getLicensePlate());
        addRow(vehicleTable, "Pickup Date/Time:", formatDateTime(reservation.getStartDate()));
        addRow(vehicleTable, "Return Date/Time:", formatDateTime(reservation.getEndDate()));
        addRow(vehicleTable, "Pickup Location:", reservation.getLocationName());
        addRow(vehicleTable, "Return Location:", reservation.getLocationName());
        int days = calculateDays(reservation.getStartDate(), reservation.getEndDate());
        addRow(vehicleTable, "Duration:", days + " day(s)");
        document.add(vehicleTable);
        document.add(new Paragraph(" "));

        // Financial Summary
        document.add(new Paragraph("Financial Summary").setFont(boldFont));
        Table financialTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        financialTable.useAllAvailableWidth();
        double baseAmount = reservation.getBaseAmount() != null ? reservation.getBaseAmount() : reservation.getAmount();
        addRow(financialTable, "Base Rental Fee:", "$" + String.format("%.2f", baseAmount));

        // Extras (if any)
        Object extrasObj = reservation.getExtras();
        if (extrasObj != null && extrasObj instanceof List) {
            List<?> extrasList = (List<?>) extrasObj;
            if (!extrasList.isEmpty()) {
                addRow(financialTable, "Extras:", "");
                for (Object extra : extrasList) {
                    // Assume extra is a map with name and price
                    if (extra instanceof java.util.Map) {
                        java.util.Map<?, ?> extraMap = (java.util.Map<?, ?>) extra;
                        String name = extraMap.get("name") != null ? extraMap.get("name").toString() : "Unknown";
                        double price = 0;
                        if (extraMap.get("price") instanceof Number) {
                            price = ((Number) extraMap.get("price")).doubleValue();
                        }
                        addRow(financialTable, "  " + name, "$" + String.format("%.2f", price));
                    } else {
                        addRow(financialTable, "  Extra", "");
                    }
                }
            }
        }

        addRow(financialTable, "Total Amount:", "$" + String.format("%.2f", reservation.getAmount()));
        addRow(financialTable, "Security Deposit:", "$" + String.format("%.2f", reservation.getSecurityDeposit() != null ? reservation.getSecurityDeposit() : 0));
        // Excess and damage markers omitted for now (fields may not exist in entity)
        document.add(financialTable);
        document.add(new Paragraph(" "));

        // Checklist (simplified)
        document.add(new Paragraph("Vehicle Condition").setFont(boldFont));
        Table checklistTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        checklistTable.useAllAvailableWidth();
        addRow(checklistTable, "Fuel Level:", reservation.getPickupFuelLevel() != null ? reservation.getPickupFuelLevel() + "/8" : "N/A");
        addRow(checklistTable, "Kilometers Out:", reservation.getPickupKmOut() != null ? reservation.getPickupKmOut().toString() : "N/A");
        if (reservation.getDropOffKmIn() != null) {
            addRow(checklistTable, "Kilometers In:", reservation.getDropOffKmIn().toString());
        }
        document.add(checklistTable);
        document.add(new Paragraph(" "));

        // Signatures
        document.add(new Paragraph("Signatures").setFont(boldFont));
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        signatureTable.useAllAvailableWidth();

        // Renter signature
        if (reservation.getPickupRenterSignature() != null && !reservation.getPickupRenterSignature().isEmpty()) {
            try {
                String base64Data = reservation.getPickupRenterSignature().split(",")[1];
                byte[] sigBytes = Base64.getDecoder().decode(base64Data);
                Image renterSig = new Image(ImageDataFactory.create(sigBytes));
                renterSig.setWidth(100);
                renterSig.setHeight(40);
                Cell sigCell = new Cell().add(renterSig);
                signatureTable.addCell(sigCell);
            } catch (Exception e) {
                signatureTable.addCell(new Cell().add(new Paragraph("Signed")));
            }
        } else {
            signatureTable.addCell(new Cell().add(new Paragraph("Not signed")));
        }
        signatureTable.addCell(new Cell().add(new Paragraph("Renter Signature")));

        // Agent signature
        if (reservation.getPickupAgentSignature() != null && !reservation.getPickupAgentSignature().isEmpty()) {
            try {
                String base64Data = reservation.getPickupAgentSignature().split(",")[1];
                byte[] sigBytes = Base64.getDecoder().decode(base64Data);
                Image agentSig = new Image(ImageDataFactory.create(sigBytes));
                agentSig.setWidth(100);
                agentSig.setHeight(40);
                Cell sigCell = new Cell().add(agentSig);
                signatureTable.addCell(sigCell);
            } catch (Exception e) {
                signatureTable.addCell(new Cell().add(new Paragraph("Signed")));
            }
        } else {
            signatureTable.addCell(new Cell().add(new Paragraph("Not signed")));
        }
        signatureTable.addCell(new Cell().add(new Paragraph("Agent Signature")));
        document.add(signatureTable);
        document.add(new Paragraph(" "));

        // Footer
        document.add(new Paragraph("Thank you for choosing " + companyDetails.getName() + "!")
                .setTextAlignment(TextAlignment.CENTER).setFontSize(10));
        document.add(new Paragraph(companyDetails.getAddress() + " | " + companyDetails.getPhone())
                .setTextAlignment(TextAlignment.CENTER).setFontSize(8));
        document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .setTextAlignment(TextAlignment.CENTER).setFontSize(8));

        document.close();
        return out.toByteArray();
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "—").setFontSize(10)));
    }

    private String formatDateTime(String dateStr) {
        if (dateStr == null) return "—";
        try {
            LocalDateTime dt = LocalDateTime.parse(dateStr);
            return dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private int calculateDays(String start, String end) {
        try {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);
            return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}
