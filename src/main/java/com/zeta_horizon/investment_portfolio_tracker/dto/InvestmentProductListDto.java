package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentProductListDto {
    private Integer id;
    private String name;
    private InvestmentType type;
    private RiskLevel riskLevel;
    private BigDecimal minimumInvestment;
    private BigDecimal expectedAnnualReturnRate;
    private BigDecimal currentNetAssetValuePerUnit;
    private boolean isActive;
}

