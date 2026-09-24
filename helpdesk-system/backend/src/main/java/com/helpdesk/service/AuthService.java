package com.helpdesk.service;

import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.LoginRequest;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.entity.Role;
import com.helpdesk.entity.User;
import com.helpdesk.exception.ApiException;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.JwtService;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email is already registered", HttpStatus.CONFLICT);
        }
        if (request.getRole() == Role.ADMIN || request.getRole() == Role.MANAGER) {
            throw new ApiException("MANAGER/ADMIN accounts must be created by an existing admin", HttpStatus.FORBIDDEN);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .specialization(request.getSpecialization())
                // NOTE: in production, emailVerified/phoneVerified start false and are flipped
                // by OTP-verification endpoints (not implemented in this scaffold).
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user), Map.of(
                "role", user.getRole().name(),
                "userId", user.getId(),
                "fullName", user.getFullName()
        ));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
