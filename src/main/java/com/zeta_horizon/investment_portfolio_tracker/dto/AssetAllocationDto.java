package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetAllocationDto {
    private String assetType;
    private BigDecimal percentage;
}