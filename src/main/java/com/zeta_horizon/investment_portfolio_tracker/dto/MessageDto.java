package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private UUID messageId;
    private UUID senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean sentByAdmin;
}
