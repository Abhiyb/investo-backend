package com.zeta_horizon.investment_portfolio_tracker.mapper;

import org.springframework.stereotype.Component;

import com.zeta_horizon.investment_portfolio_tracker.dto.TicketMessageDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.TicketMessage;

@Component
public class TicketMessageMapper {

    public TicketMessageDto toDto(TicketMessage msg) {
        return TicketMessageDto.builder()
                .senderName(msg.getSender().getName())
                .message(msg.getMessage())
                .senderType(msg.getSenderType().name())
                .timestamp(msg.getTimestamp())
                .build();
    }

}
