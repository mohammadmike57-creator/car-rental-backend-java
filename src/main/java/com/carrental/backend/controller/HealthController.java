package com.carrental.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class HealthController {

    /** Address of the website (the combined service that serves the app). */
    @Value("${app.base-url:https://www.nctrental.com}")
    private String appBaseUrl;

    /**
     * This service only runs the API. People who open its address in a browser (for example a phone
     * with an old bookmark, or the domain without "www") are sent to the website instead of a blank
     * "OK" page. Health checks and API clients, which do not ask for HTML, still get "OK".
     */
    @GetMapping("/")
    public ResponseEntity<String> root(@RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
                                       HttpServletRequest request) {
        boolean browser = accept != null && accept.contains("text/html");
        URI target = URI.create(appBaseUrl);
        boolean alreadyOnWebsite = target.getHost() != null && target.getHost().equalsIgnoreCase(request.getServerName());
        if (browser && !alreadyOnWebsite) {
            return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
