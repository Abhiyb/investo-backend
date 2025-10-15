package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummaryDto {
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal absoluteReturn;
    private BigDecimal returnPercentage;
}