package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> {
    private int status;
    private T data;
    private String message;
    private LocalDateTime timestamp;
}
