package com.helpdesk.dto;

import com.helpdesk.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateStatusRequest {
    @NotNull
    private TicketStatus status;

    private String note; // resolution notes / rejection reason etc.
}
