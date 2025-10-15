package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionFilterDto {
    private String searchQuery;
    private TransactionType txnType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String sortBy; // "txnDate", "amount", "units"
    private String sortOrder; // "asc", "desc"
    private Integer page;
    private Integer size;
}