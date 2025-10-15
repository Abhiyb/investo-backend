package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.*;
import com.zeta_horizon.investment_portfolio_tracker.repository.*;
import com.zeta_horizon.investment_portfolio_tracker.service.PortfolioAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PortfolioAnalyticsServiceImpl implements PortfolioAnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final InvestmentProductRepository investmentProductRepository;

    @Override
    public PortfolioSummaryDto getPortfolioSummary(User user) {
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Portfolio p : portfolios) {
            BigDecimal invested = p.getAvgPurchasePrice().multiply(p.getUnitsOwned());
            BigDecimal current = p.getInvestmentProduct().getCurrentNetAssetValuePerUnit().multiply(p.getUnitsOwned());

            totalInvested = totalInvested.add(invested);
            currentValue = currentValue.add(current);
        }

        BigDecimal absoluteReturn = currentValue.subtract(totalInvested);
        BigDecimal returnPercentage = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? absoluteReturn.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return PortfolioSummaryDto.builder()
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .absoluteReturn(absoluteReturn)
                .returnPercentage(returnPercentage)
                .build();
    }

    @Override
    public List<AssetAllocationDto> getAssetAllocation(User user) {
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        Map<String, BigDecimal> allocationMap = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Portfolio p : portfolios) {
            BigDecimal value = p.getInvestmentProduct().getCurrentNetAssetValuePerUnit().multiply(p.getUnitsOwned());
            totalValue = totalValue.add(value);

            String type = p.getInvestmentProduct().getType().name();
            allocationMap.put(type, allocationMap.getOrDefault(type, BigDecimal.ZERO).add(value));
        }

        List<AssetAllocationDto> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : allocationMap.entrySet()) {
            BigDecimal percent = totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            result.add(new AssetAllocationDto(entry.getKey(), percent));
        }

        return result;
    }

    @Override
    public List<GainLossDto> getGainLossAnalysis(User user) {
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        List<GainLossDto> result = new ArrayList<>();

        for (Portfolio p : portfolios) {
            InvestmentProduct product = p.getInvestmentProduct();
            BigDecimal invested = p.getAvgPurchasePrice().multiply(p.getUnitsOwned());
            BigDecimal current = product.getCurrentNetAssetValuePerUnit().multiply(p.getUnitsOwned());
            BigDecimal gain = current.subtract(invested);

            result.add(GainLossDto.builder()
                    .investmentName(product.getName())
                    .investedAmount(invested)
                    .currentValue(current)
                    .gainOrLoss(gain)
                    .build());
        }

        return result;
    }
}