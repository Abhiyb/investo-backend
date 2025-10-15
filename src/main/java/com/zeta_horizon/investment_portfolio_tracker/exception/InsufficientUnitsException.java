package com.zeta_horizon.investment_portfolio_tracker.exception;

public class InsufficientUnitsException extends RuntimeException {
    public InsufficientUnitsException(String message) {
        super(message);
    }
}
