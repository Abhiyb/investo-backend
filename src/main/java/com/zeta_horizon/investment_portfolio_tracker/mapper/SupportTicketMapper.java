package com.zeta_horizon.investment_portfolio_tracker.mapper;

import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.entity.TicketMessage;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.SupportTicket;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SupportTicketMapper {

    private final TicketMessageMapper ticketMessageMapper;

    public SupportTicketResponseDto toResponseDto(SupportTicket ticket) {
        return SupportTicketResponseDto.builder()
                .ticketId(ticket.getId())
                .userId(ticket.getUser().getId())
                .userName(ticket.getUser().getName())
                .investmentProductId(ticket.getInvestmentProduct() != null ?
                        ticket.getInvestmentProduct().getId() : null)
                .investmentProductName(ticket.getInvestmentProduct() != null ?
                        ticket.getInvestmentProduct().getName() : null)
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .messages(ticket.getMessages() != null ?
                        ticket.getMessages().stream()
                                .sorted(Comparator.comparing(TicketMessage::getTimestamp))
                                .map(ticketMessageMapper::toDto)
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

}

