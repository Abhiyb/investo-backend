package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TicketMessageDto {
    private String senderName;
    private String message;
    private String senderType;
    private LocalDateTime timestamp;
}
