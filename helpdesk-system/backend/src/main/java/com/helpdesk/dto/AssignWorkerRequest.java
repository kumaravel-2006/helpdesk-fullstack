package com.helpdesk.dto;

import com.helpdesk.entity.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AssignWorkerRequest {
    @NotNull
    private Long workerId;

    private Priority priority;

    private String expectedCompletionAt; // ISO date-time string
}
