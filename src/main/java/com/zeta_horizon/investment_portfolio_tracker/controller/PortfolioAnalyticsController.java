package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.dto.AssetAllocationDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.GainLossDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.PortfolioSummaryDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.entity.UserPrincipal;
import com.zeta_horizon.investment_portfolio_tracker.service.PortfolioAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService portfolioAnalyticsService;

    @GetMapping("/summary")
    public PortfolioSummaryDto getSummary(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        return portfolioAnalyticsService.getPortfolioSummary(user);
    }

    @GetMapping("/allocation")
    public List<AssetAllocationDto> getAllocation(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        return portfolioAnalyticsService.getAssetAllocation(user);
    }

    @GetMapping("/gains")
    public List<GainLossDto> getGains(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        return portfolioAnalyticsService.getGainLossAnalysis(user);
    }
}