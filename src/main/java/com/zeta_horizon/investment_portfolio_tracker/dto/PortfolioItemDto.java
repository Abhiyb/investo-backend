package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class PortfolioItemDto {
    private Integer id;
    private Integer investmentProductId;
    private String investmentProductName;
    private String type;
    private String riskLevel;
    private BigDecimal unitsOwned;
    private BigDecimal avgPurchasePrice;
    private BigDecimal currentNAV;
    private BigDecimal investedValue;
    private BigDecimal currentValue;
    private BigDecimal absoluteReturn;
    private BigDecimal percentageReturn;
}
