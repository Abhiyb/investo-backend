package com.zeta_horizon.investment_portfolio_tracker.dto;


import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponseDto {

    private UUID ticketId;

    private UUID userId;

    private String userName;

    private Integer investmentProductId;

    private String investmentProductName;

    private String subject;

    private String description;

    private TicketStatus status;

    private Priority priority;

    private String response;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<TicketMessageDto> messages;
}

