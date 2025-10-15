package com.zeta_horizon.investment_portfolio_tracker.repository;

import com.zeta_horizon.investment_portfolio_tracker.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, UUID> {
    // get the messages from the db based on the ticketId in acsending order by time
    List<TicketMessage> findBySupportTicketIdOrderByTimestampAsc(UUID supportTicketId);
}
