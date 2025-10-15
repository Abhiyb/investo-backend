package com.zeta_horizon.investment_portfolio_tracker.service;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;


public interface PortfolioService {

    PortfolioResponseDto getUserPortfolio(User user);

    PortfolioItemDto buyInvestment(User user, BuyInvestmentRequestDto request);

    PortfolioItemDto sellInvestment(User user, SellInvestmentRequestDto request);

    PortfolioItemDto getInvestmentById(User user, Integer id);
}
