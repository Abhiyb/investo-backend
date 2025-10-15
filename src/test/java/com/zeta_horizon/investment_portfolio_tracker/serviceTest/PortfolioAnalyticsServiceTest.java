package com.zeta_horizon.investment_portfolio_tracker.serviceTest;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.repository.*;
import com.zeta_horizon.investment_portfolio_tracker.service.implementation.PortfolioAnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioAnalyticsServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvestmentProductRepository investmentProductRepository;

    @InjectMocks
    private PortfolioAnalyticsServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
    }

    @Test
    void testGetPortfolioSummary() {
        InvestmentProduct product = InvestmentProduct.builder()
                .name("Bitcoin")
                .type(InvestmentType.CRYPTOCURRENCY)
                .currentNetAssetValuePerUnit(new BigDecimal("5700000.00"))
                .build();

        Portfolio portfolio = Portfolio.builder()
                .investmentProduct(product)
                .unitsOwned(new BigDecimal("2"))
                .avgPurchasePrice(new BigDecimal("5000000.00"))
                .user(user)
                .build();

        when(portfolioRepository.findByUser(user)).thenReturn(List.of(portfolio));

        PortfolioSummaryDto summary = service.getPortfolioSummary(user);

        assertNotNull(summary);
        assertEquals(new BigDecimal("10000000.00"), summary.getTotalInvested());
        assertEquals(new BigDecimal("11400000.00"), summary.getCurrentValue());
        assertEquals(new BigDecimal("1400000.00"), summary.getAbsoluteReturn());
        assertTrue(summary.getReturnPercentage().compareTo(new BigDecimal("14.00")) >= 0); // ~14%
    }

    @Test
    void testGetAssetAllocation() {
        InvestmentProduct product = InvestmentProduct.builder()
                .name("Bitcoin")
                .type(InvestmentType.CRYPTOCURRENCY)
                .currentNetAssetValuePerUnit(new BigDecimal("5700000.00"))
                .build();

        Portfolio portfolio = Portfolio.builder()
                .investmentProduct(product)
                .unitsOwned(new BigDecimal("1"))
                .user(user)
                .build();

        when(portfolioRepository.findByUser(user)).thenReturn(List.of(portfolio));

        List<AssetAllocationDto> allocations = service.getAssetAllocation(user);

        assertEquals(1, allocations.size());
        assertEquals("CRYPTOCURRENCY", allocations.get(0).getAssetType());
        assertEquals(new BigDecimal("100.0000"), allocations.get(0).getPercentage());
    }

    @Test
    void testGetGainLossAnalysis() {
        InvestmentProduct product = InvestmentProduct.builder()
                .name("Bitcoin")
                .type(InvestmentType.CRYPTOCURRENCY)
                .currentNetAssetValuePerUnit(new BigDecimal("6000000.00"))
                .build();

        Portfolio portfolio = Portfolio.builder()
                .investmentProduct(product)
                .avgPurchasePrice(new BigDecimal("5000000.00"))
                .unitsOwned(new BigDecimal("1"))
                .user(user)
                .build();

        when(portfolioRepository.findByUser(user)).thenReturn(List.of(portfolio));

        List<GainLossDto> result = service.getGainLossAnalysis(user);

        assertEquals(1, result.size());
        assertEquals("Bitcoin", result.get(0).getInvestmentName());
        assertEquals(new BigDecimal("1000000.00"), result.get(0).getGainOrLoss());
    }
}