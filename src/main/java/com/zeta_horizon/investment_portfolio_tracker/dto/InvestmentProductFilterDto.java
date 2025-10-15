package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentProductFilterDto {
    private InvestmentType type;
    private RiskLevel riskLevel;
    private BigDecimal maximumInvestmentAmount;
    private String searchTerm;
}
