package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvestmentProductDto {
    private Integer id;
    private String name;
    private InvestmentType type;
    private RiskLevel riskLevel;
    private BigDecimal minimumInvestment;
    private BigDecimal expectedAnnualReturnRate;
    private BigDecimal currentNetAssetValuePerUnit;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

