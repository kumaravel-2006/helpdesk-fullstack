package com.helpdesk.service;

import com.helpdesk.dto.*;
import com.helpdesk.entity.*;
import com.helpdesk.exception.ApiException;
import com.helpdesk.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CacheManager cacheManager;

    private static final Set<TicketStatus> TERMINAL = Set.of(
            TicketStatus.CLOSED, TicketStatus.REJECTED, TicketStatus.CANCELLED);

    @Caching(evict = {
            @CacheEvict(value = "dashboard_stats", allEntries = true),
            @CacheEvict(value = "all_tickets", allEntries = true)
    })
    @Transactional
    public TicketResponse createTicket(User student, CreateTicketRequest request) {
        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new ApiException("Unknown sub-category", HttpStatus.BAD_REQUEST));

        TicketLocation location = TicketLocation.builder()
                .locationType(request.getLocationType())
                .buildingOrHostelName(request.getBuildingOrHostelName())
                .block(request.getBlock())
                .floor(request.getFloor())
                .roomOrArea(request.getRoomOrArea())
                .build();

        Ticket ticket = Ticket.builder()
                .ticketCode(generateTicketCode())
                .student(student)
                .subCategory(subCategory)
                .location(location)
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
                .status(TicketStatus.OPEN)
                .preferredDate(request.getPreferredDate())
                .preferredTimeSlot(request.getPreferredTimeSlot())
                .permissionToEnterRoom(request.isPermissionToEnterRoom())
                .build();

        if (request.getImageUrls() != null) {
            if (ticket.getImages() == null) {
                ticket.setImages(new ArrayList<>());
            }
            for (String url : request.getImageUrls()) {
                ticket.getImages().add(TicketImage.builder()
                        .ticket(ticket)
                        .url(url)
                        .stage("REPORTED")
                        .build());
            }
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        evictCaches(student.getId(), null);
        return toResponse(savedTicket);
    }

    @Cacheable(value = "student_tickets", key = "#studentId")
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForStudent(Long studentId) {
        return ticketRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Cacheable(value = "worker_tickets", key = "#workerId")
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForWorker(Long workerId) {
        return ticketRepository.findByAssignedWorkerIdOrderByCreatedAtDesc(workerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Cacheable(value = "all_tickets", key = "'all'")
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findTop50ByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketByCode(String ticketCode) {
        return toResponse(findByCode(ticketCode));
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboard_stats", allEntries = true),
            @CacheEvict(value = "all_tickets", allEntries = true)
    })
    @Transactional
    public TicketResponse assignWorker(String ticketCode, AssignWorkerRequest request) {
        Ticket ticket = findByCode(ticketCode);
        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ApiException("Worker not found", HttpStatus.NOT_FOUND));

        if (worker.getRole() != Role.WORKER) {
            throw new ApiException("Selected user is not a WORKER", HttpStatus.BAD_REQUEST);
        }

        ticket.setAssignedWorker(worker);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setAssignedAt(LocalDateTime.now());
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getExpectedCompletionAt() != null) {
            ticket.setExpectedCompletionAt(LocalDateTime.parse(request.getExpectedCompletionAt()));
        }

        ticket = ticketRepository.save(ticket);
        evictCaches(ticket.getStudent().getId(), worker.getId());

        notify(ticket, worker.getId(), "You have been assigned ticket " + ticket.getTicketCode());
        notify(ticket, ticket.getStudent().getId(),
                "Your ticket " + ticket.getTicketCode() + " has been assigned to a worker");

        return toResponse(ticket);
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboard_stats", allEntries = true),
            @CacheEvict(value = "all_tickets", allEntries = true)
    })
    @Transactional
    public TicketResponse updateStatus(String ticketCode, UpdateStatusRequest request, User actor) {
        Ticket ticket = findByCode(ticketCode);

        if (TERMINAL.contains(ticket.getStatus()) && request.getStatus() != TicketStatus.REOPENED) {
            throw new ApiException("Ticket is already in a terminal state: " + ticket.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        ticket.setStatus(request.getStatus());

        switch (request.getStatus()) {
            case RESOLVED -> ticket.setResolvedAt(LocalDateTime.now());
            case CLOSED -> ticket.setClosedAt(LocalDateTime.now());
            default -> {
                /* no timestamp side-effect */ }
        }

        if (request.getNote() != null && !request.getNote().isBlank()) {
            if (request.getStatus() == TicketStatus.RESOLVED) {
                ticket.setResolutionNotes(request.getNote());
            }
            ticket.getNotes().add(TicketNote.builder()
                    .ticket(ticket)
                    .author(actor)
                    .content(request.getNote())
                    .internal(actor.getRole() != Role.STUDENT)
                    .build());
        }

        ticket = ticketRepository.save(ticket);
        evictCaches(ticket.getStudent().getId(),
                ticket.getAssignedWorker() != null ? ticket.getAssignedWorker().getId() : null);

        Long notifyRecipient = actor.getRole() == Role.STUDENT
                ? (ticket.getAssignedWorker() != null ? ticket.getAssignedWorker().getId() : null)
                : ticket.getStudent().getId();

        if (notifyRecipient != null) {
            notify(ticket, notifyRecipient,
                    "Ticket " + ticket.getTicketCode() + " status changed to " + ticket.getStatus());
        }

        return toResponse(ticket);
    }

    @Cacheable(value = "dashboard_stats", key = "'global'")
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .open(ticketRepository.countByStatus(TicketStatus.OPEN))
                .assigned(ticketRepository.countByStatus(TicketStatus.ASSIGNED))
                .inProgress(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS))
                .resolved(ticketRepository.countByStatus(TicketStatus.RESOLVED))
                .closed(ticketRepository.countByStatus(TicketStatus.CLOSED))
                .escalated(ticketRepository.countByStatus(TicketStatus.ESCALATED))
                .build();
    }

    private Ticket findByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ApiException("Ticket not found: " + ticketCode, HttpStatus.NOT_FOUND));
    }

    private void notify(Ticket ticket, Long recipientId, String message) {
        String title = ticket.getStatus() == TicketStatus.ASSIGNED
                ? "Task Assignment"
                : "Ticket Update: " + ticket.getStatus();
        notificationService.publishTicketEvent(TicketEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .ticketId(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .status(ticket.getStatus())
                .title(title)
                .location(formatLocation(ticket.getLocation()))
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : "NORMAL")
                .recipientUserId(recipientId)
                .message(message)
                .build());
    }

    private String generateTicketCode() {
        int suffix = ThreadLocalRandom.current().nextInt(10000, 99999);
        String candidate = "HD" + suffix;
        // avoid the (rare) collision rather than relying on a DB sequence, to keep the
        // scaffold simple
        while (ticketRepository.findByTicketCode(candidate).isPresent()) {
            candidate = "HD" + ThreadLocalRandom.current().nextInt(10000, 99999);
        }
        return candidate;
    }

    private TicketResponse toResponse(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .ticketCode(t.getTicketCode())
                .studentName(t.getStudent() != null ? t.getStudent().getFullName() : null)
                .studentId(t.getStudent() != null ? t.getStudent().getId() : null)
                .categoryName(t.getSubCategory() != null && t.getSubCategory().getCategory() != null
                        ? t.getSubCategory().getCategory().getName()
                        : null)
                .subCategoryName(t.getSubCategory() != null ? t.getSubCategory().getName() : null)
                .locationSummary(t.getLocation() != null ? formatLocation(t.getLocation()) : null)
                .description(t.getDescription())
                .priority(t.getPriority())
                .status(t.getStatus())
                .preferredDate(t.getPreferredDate())
                .preferredTimeSlot(t.getPreferredTimeSlot())
                .assignedWorkerName(t.getAssignedWorker() != null ? t.getAssignedWorker().getFullName() : null)
                .assignedWorkerId(t.getAssignedWorker() != null ? t.getAssignedWorker().getId() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .resolutionNotes(t.getResolutionNotes())
                .imageUrls(t.getImages() != null
                        ? t.getImages().stream().map(TicketImage::getUrl).collect(Collectors.toList())
                        : List.of())
                .build();
    }

    private String formatLocation(TicketLocation loc) {
        if (loc == null)
            return null;
        StringBuilder sb = new StringBuilder(
                loc.getBuildingOrHostelName() != null ? loc.getBuildingOrHostelName() : "");
        if (loc.getBlock() != null && !loc.getBlock().isBlank())
            sb.append(" - Block ").append(loc.getBlock());
        if (loc.getFloor() != null && !loc.getFloor().isBlank())
            sb.append(" - Floor ").append(loc.getFloor());
        if (loc.getRoomOrArea() != null && !loc.getRoomOrArea().isBlank())
            sb.append(" - ").append(loc.getRoomOrArea());
        return sb.toString();
    }

    private void evictCaches(Long studentId, Long workerId) {
        if (cacheManager == null)
            return;
        if (studentId != null) {
            Cache studentCache = cacheManager.getCache("student_tickets");
            if (studentCache != null)
                studentCache.evict(studentId);
        }
        if (workerId != null) {
            Cache workerCache = cacheManager.getCache("worker_tickets");
            if (workerCache != null)
                workerCache.evict(workerId);
        }
        Cache statsCache = cacheManager.getCache("dashboard_stats");
        if (statsCache != null)
            statsCache.clear();
        Cache allTicketsCache = cacheManager.getCache("all_tickets");
        if (allTicketsCache != null)
            allTicketsCache.clear();
    }
}
