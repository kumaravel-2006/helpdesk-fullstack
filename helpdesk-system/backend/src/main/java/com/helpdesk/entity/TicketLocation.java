package com.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType;

    // Hostel case: "Boys Hostel A"; Academic case: "IT Block"
    private String buildingOrHostelName;

    private String block;

    private String floor;

    private String roomOrArea; // e.g. "B204" or "Lab 302"
}
