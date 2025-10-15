package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionHistoryResponseDto {
    private List<TransactionDto> transactions;
}
