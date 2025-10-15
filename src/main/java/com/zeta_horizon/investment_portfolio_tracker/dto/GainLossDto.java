package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GainLossDto {
    private String investmentName;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal gainOrLoss;
}