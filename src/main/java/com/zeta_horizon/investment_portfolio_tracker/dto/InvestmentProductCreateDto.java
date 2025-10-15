package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentProductCreateDto {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Investment type is required")
    private InvestmentType type;

    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel;

    @NotNull(message = "Minimum investment amount is required")
    @Positive(message = "Minimum investment must be positive")
    private BigDecimal minimumInvestment;

    @NotNull(message = "Expected return rate is required")
    @DecimalMin(value = "0.0", message = "Expected return rate must be positive")
    private BigDecimal expectedAnnualReturnRate;

    @NotNull(message = "Current NAV is required")
    @Positive(message = "Current NAV must be positive")
    private BigDecimal currentNetAssetValuePerUnit;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
