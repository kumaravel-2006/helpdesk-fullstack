package com.helpdesk.controller;

import com.helpdesk.dto.TicketEvent;
import com.helpdesk.security.UserPrincipal;
import com.helpdesk.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/mine")
    public ResponseEntity<List<TicketEvent>> getMyNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(notificationService.getNotificationsForUser(principal.getUser().getId()));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Map<String, String>> dismissNotification(
            @PathVariable String eventId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && principal.getUser() != null) {
            notificationService.dismissNotification(principal.getUser().getId(), eventId);
        }
        return ResponseEntity.ok(Map.of("message", "Notification dismissed"));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearAllNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && principal.getUser() != null) {
            notificationService.clearAllNotifications(principal.getUser().getId());
        }
        return ResponseEntity.ok(Map.of("message", "All notifications cleared"));
    }
}
