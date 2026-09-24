package com.helpdesk.dto;

import com.helpdesk.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank @Email
    private String email;

    private String phoneNumber;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull
    private Role role; // STUDENT / WORKER signups; MANAGER/ADMIN created by an ADMIN in practice

    private String specialization; // required if role == WORKER
}
