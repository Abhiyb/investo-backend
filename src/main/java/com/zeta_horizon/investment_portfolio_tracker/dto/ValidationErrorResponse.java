package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ValidationErrorResponse {
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> errors = new HashMap<>();

    public void addError(String field, String message) {
        errors.put(field, message);
    }
}

