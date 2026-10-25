package com.helpdesk.controller;

import com.helpdesk.dto.*;
import com.helpdesk.entity.Role;
import com.helpdesk.entity.User;
import com.helpdesk.security.UserPrincipal;
import com.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public TicketResponse create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(principal.getUser(), request);
    }

    @GetMapping("/mine")
    public List<TicketResponse> myTickets(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        return user.getRole() == Role.WORKER
                ? ticketService.getTicketsForWorker(user.getId())
                : ticketService.getTicketsForStudent(user.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<TicketResponse> all() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{ticketCode}")
    public TicketResponse getOne(@PathVariable String ticketCode) {
        return ticketService.getTicketByCode(ticketCode);
    }

    @PostMapping("/{ticketCode}/assign")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TicketResponse assign(@PathVariable String ticketCode, @Valid @RequestBody AssignWorkerRequest request) {
        return ticketService.assignWorker(ticketCode, request);
    }

    @PatchMapping("/{ticketCode}/status")
    public TicketResponse updateStatus(@PathVariable String ticketCode,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ticketService.updateStatus(ticketCode, request, principal.getUser());
    }
}
