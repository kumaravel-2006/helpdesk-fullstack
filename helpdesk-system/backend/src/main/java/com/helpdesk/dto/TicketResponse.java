package com.helpdesk.dto;

import com.helpdesk.entity.Priority;
import com.helpdesk.entity.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String ticketCode;
    private String studentName;
    private String categoryName;
    private String subCategoryName;
    private String locationSummary;
    private String description;
    private Priority priority;
    private TicketStatus status;
    private String preferredDate;
    private String preferredTimeSlot;
    private String assignedWorkerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String resolutionNotes;
    private List<String> imageUrls;
}
