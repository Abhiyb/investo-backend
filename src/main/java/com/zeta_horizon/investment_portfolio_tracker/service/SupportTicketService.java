package com.zeta_horizon.investment_portfolio_tracker.service;


import com.zeta_horizon.investment_portfolio_tracker.dto.MessageRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface SupportTicketService {
    SupportTicketResponseDto createTicket(SupportTicketRequestDto requestDto, String email);

    List<SupportTicketResponseDto> getTicketsForCurrentUser(String email);

    List<SupportTicketResponseDto> getAllTickets();

    SupportTicketResponseDto respondToTicket(MessageRequestDto messageRequestDto, String email);

    List<SupportTicketResponseDto> filterTickets(Priority priority, TicketStatus status);

    List<SupportTicketResponseDto> filterTicketsForUser(String emailId, Priority priority, TicketStatus status);

    SupportTicketResponseDto getTicketViaId(UUID ticketId);
}
