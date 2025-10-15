package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class PortfolioResponseDto {
    private List<PortfolioItemDto> holdings;
    private BigDecimal totalInvestedValue;
    private BigDecimal totalCurrentValue;
}
