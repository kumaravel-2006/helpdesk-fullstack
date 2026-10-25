package com.helpdesk.controller;

import com.helpdesk.dto.DashboardStats;
import com.helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TicketService ticketService;

    @GetMapping("/stats")
    public DashboardStats stats() {
        return ticketService.getDashboardStats();
    }
}
