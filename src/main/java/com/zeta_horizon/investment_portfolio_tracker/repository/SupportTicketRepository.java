package com.zeta_horizon.investment_portfolio_tracker.repository;

import com.zeta_horizon.investment_portfolio_tracker.entity.SupportTicket;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    List<SupportTicket> findByPriorityOrderByCreatedAtDesc(Priority priority);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<SupportTicket> findByPriorityAndStatusOrderByCreatedAtDesc(Priority priority, TicketStatus status);

    List<SupportTicket> findByUserIdAndPriorityAndStatusOrderByCreatedAtDesc(UUID userId, Priority priority, TicketStatus status);

    List<SupportTicket> findByUserIdAndPriorityOrderByCreatedAtDesc(UUID userId, Priority priority);

    List<SupportTicket> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, TicketStatus status);

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

}
