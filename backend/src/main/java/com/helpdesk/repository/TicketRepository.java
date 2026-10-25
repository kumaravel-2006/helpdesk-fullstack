package com.helpdesk.repository;

import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.TicketStatus;
import com.helpdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Ticket> findByAssignedWorkerIdOrderByCreatedAtDesc(Long workerId);
    long countByStatus(TicketStatus status);
    List<Ticket> findTop50ByOrderByCreatedAtDesc();
}
