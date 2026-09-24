package com.helpdesk.dto;

import com.helpdesk.entity.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;

/** Kafka event payload published whenever a ticket changes status. */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketEvent {
    private Long ticketId;
    private String ticketCode;
    private TicketStatus status;
    private Long recipientUserId; // who should be notified (student or worker)
    private String message;
    private LocalDateTime timestamp;
}
