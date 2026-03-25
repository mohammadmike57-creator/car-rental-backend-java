package com.carrental.backend.controller;

import com.carrental.backend.dto.AuthRequest;
import com.carrental.backend.dto.AuthResponse;
import com.carrental.backend.dto.RegisterRequest;
import com.carrental.backend.dto.ChangePasswordRequest;
import com.carrental.backend.model.User;
import com.carrental.backend.model.UserPermission;
import com.carrental.backend.model.UserStatus;
import com.carrental.backend.repository.UserRepository;
import com.carrental.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.badRequest().body("Account is inactive");
        }
        if (user.getWebAppAccess() == null || !user.getWebAppAccess()) {
            return ResponseEntity.badRequest().body("Web access denied");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        List<UserPermission> permissions = request.getPermissions() == null ? List.of() :
            request.getPermissions().stream()
                .map(p -> {
                    try {
                        return UserPermission.valueOf(p);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPermissions(permissions);
        user.setStatus(UserStatus.ACTIVE);
        user.setWebAppAccess(true);
        user.setIsOnline(false);
        user.setLastSeen(null);
        user.setDeviceType(null);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordLastChanged(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok().body("Password changed successfully");
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        return ResponseEntity.ok("Authenticated as " + userDetails.getUsername());
    }
}
