package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedTransactionResponseDto {
    private List<TransactionDto> transactions;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
}