package com.zeta_horizon.investment_portfolio_tracker.exception;

public class InvestmentProductNotFoundException extends RuntimeException {
    public InvestmentProductNotFoundException(String message) {
        super(message);
    }
}
