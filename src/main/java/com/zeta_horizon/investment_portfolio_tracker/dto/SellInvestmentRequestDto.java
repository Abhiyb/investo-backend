package com.zeta_horizon.investment_portfolio_tracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellInvestmentRequestDto {

    @NotNull(message = "Investment product ID is required")
    private Integer investmentProductId;

    @NotNull(message = "Units are required")
    @Min(value = 0, message = "Units must be positive")
    private BigDecimal units;
}
