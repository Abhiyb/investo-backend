package com.zeta_horizon.investment_portfolio_tracker.service;

import com.zeta_horizon.investment_portfolio_tracker.dto.AssetAllocationDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.GainLossDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.PortfolioSummaryDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;

import java.util.List;

public interface PortfolioAnalyticsService {
    PortfolioSummaryDto getPortfolioSummary(User user);
    List<AssetAllocationDto> getAssetAllocation(User user);
    List<GainLossDto> getGainLossAnalysis(User user);
}