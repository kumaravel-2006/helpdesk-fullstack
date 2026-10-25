package com.helpdesk.repository;

import com.helpdesk.entity.TicketLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketLocationRepository extends JpaRepository<TicketLocation, Long> {
}
