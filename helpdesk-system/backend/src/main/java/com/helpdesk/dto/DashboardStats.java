package com.helpdesk.dto;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardStats {
    private long open;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long closed;
    private long escalated;
}
