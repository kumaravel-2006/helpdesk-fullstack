package com.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    private String url; // object storage URL/key

    @Builder.Default
    private String stage = "REPORTED"; // REPORTED, BEFORE, AFTER
}
