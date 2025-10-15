package com.zeta_horizon.investment_portfolio_tracker.service;

import com.zeta_horizon.investment_portfolio_tracker.dto.PaginatedTransactionResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionFilterDto;

import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionHistoryResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;


public interface TransactionService {

    TransactionHistoryResponseDto getTransactionHistory(User user);

    PaginatedTransactionResponseDto getFilteredTransactions(
            User user,
            TransactionFilterDto filterDto
    );
}