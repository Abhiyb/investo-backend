package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentProductUpdateDto {
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private InvestmentType type;

    private RiskLevel riskLevel;

    @Positive(message = "Minimum investment must be positive")
    private BigDecimal minimumInvestment;

    @DecimalMin(value = "0.0", message = "Expected annual return rate must be positive")
    private BigDecimal expectedAnnualReturnRate;

    @Positive(message = "Current NAV must be positive")
    private BigDecimal currentNetAssetValuePerUnit;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean isActive;
}
