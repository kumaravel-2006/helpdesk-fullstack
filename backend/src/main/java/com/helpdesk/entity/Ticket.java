package com.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human friendly ticket code, e.g. HD10231
    @Column(nullable = false, unique = true)
    private String ticketCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private TicketLocation location;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    private String preferredDate; // ISO date string, kept simple for the scaffold
    private String preferredTimeSlot; // e.g. "17:00-19:00" or "MORNING"/"ANYTIME"

    @Builder.Default
    private boolean permissionToEnterRoom = false;

    @ManyToOne
    @JoinColumn(name = "assigned_worker_id")
    private User assignedWorker;

    private LocalDateTime assignedAt;
    private LocalDateTime expectedCompletionAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    @Column(length = 2000)
    private String resolutionNotes;

    @Column(length = 1000)
    private String studentFeedback;
    private Integer feedbackRating; // 1-5

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketNote> notes = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
