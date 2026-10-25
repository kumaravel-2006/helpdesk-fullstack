package com.helpdesk.controller;

import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.LoginRequest;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // NOTE (scaffold): wire these to an OTP service (email + SMS) backed by Redis
    // for short-lived codes. Endpoints stubbed here to match the spec's auth flow.
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok("Email verification not yet implemented in this scaffold");
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhone(@RequestParam String phone, @RequestParam String otp) {
        return ResponseEntity.ok("Phone verification not yet implemented in this scaffold");
    }
}
