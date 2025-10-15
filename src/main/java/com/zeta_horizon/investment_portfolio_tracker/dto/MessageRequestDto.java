package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDto {
    private String ticketId;
    private String responseMessage;
    private String status;
}
