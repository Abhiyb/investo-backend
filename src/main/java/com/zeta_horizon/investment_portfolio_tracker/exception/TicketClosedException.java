package com.zeta_horizon.investment_portfolio_tracker.exception;

public class TicketClosedException extends RuntimeException {
    public TicketClosedException(String message) {
        super(message);
    }
}
