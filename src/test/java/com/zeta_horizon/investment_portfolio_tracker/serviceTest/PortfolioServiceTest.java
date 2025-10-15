package com.zeta_horizon.investment_portfolio_tracker.serviceTest;

import com.zeta_horizon.investment_portfolio_tracker.dto.BuyInvestmentRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.PortfolioItemDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.PortfolioResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SellInvestmentRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.entity.Portfolio;
import com.zeta_horizon.investment_portfolio_tracker.entity.Transaction;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.exception.InsufficientUnitsException;
import com.zeta_horizon.investment_portfolio_tracker.exception.InvalidInvestmentException;
import com.zeta_horizon.investment_portfolio_tracker.exception.MinimumInvestmentException;
import com.zeta_horizon.investment_portfolio_tracker.repository.InvestmentProductRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.PortfolioRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.TransactionRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.implementation.PortfolioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private InvestmentProductRepository investmentProductRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private User user;
    private InvestmentProduct investmentProduct;
    private Portfolio portfolio;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        investmentProduct = InvestmentProduct.builder()
                .id(1)
                .name("Test Investment")
                .type(InvestmentType.MUTUAL_FUND)
                .riskLevel(RiskLevel.MEDIUM)
                .minimumInvestment(new BigDecimal("1000.00"))
                .expectedAnnualReturnRate(new BigDecimal("8.00"))
                .currentNetAssetValuePerUnit(new BigDecimal("100.00"))
                .isActive(true)
                .build();

        portfolio = Portfolio.builder()
                .id(1)
                .user(user)
                .investmentProduct(investmentProduct)
                .unitsOwned(BigDecimal.valueOf(10))
                .avgPurchasePrice(BigDecimal.valueOf(90.00))
                .build();
    }

    @Test
    void getUserPortfolioShouldReturnCorrectPortfolioResponseDto() {
        when(portfolioRepository.findByUser(user))
                .thenReturn(List.of(portfolio));

        PortfolioResponseDto response = portfolioService.getUserPortfolio(user);

        assertNotNull(response);
        assertEquals(1, response.getHoldings().size());

        PortfolioItemDto item = response.getHoldings().get(0);
        assertEquals(portfolio.getId(), item.getId());
        assertEquals(investmentProduct.getName(), item.getInvestmentProductName());

        // Check total invested and current values
        BigDecimal expectedInvested = portfolio.getUnitsOwned().multiply(portfolio.getAvgPurchasePrice());
        BigDecimal expectedCurrent = portfolio.getUnitsOwned().multiply(investmentProduct.getCurrentNetAssetValuePerUnit());

        assertEquals(0, expectedInvested.compareTo(response.getTotalInvestedValue()));
        assertEquals(0, expectedCurrent.compareTo(response.getTotalCurrentValue()));
    }

    @Test
    void buyInvestmentShouldBuyInvestmentSuccessfully() {
        BuyInvestmentRequestDto request = BuyInvestmentRequestDto.builder()
                .investmentProductId(investmentProduct.getId())
                .units(new BigDecimal("15"))
                .build();

        when(investmentProductRepository.findById(investmentProduct.getId()))
                .thenReturn(Optional.of(investmentProduct));

        when(portfolioRepository.findByUserAndInvestmentProduct(user, investmentProduct))
                .thenReturn(Optional.of(portfolio));

        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioItemDto result = portfolioService.buyInvestment(user, request);

        assertNotNull(result);
        assertEquals(investmentProduct.getId(), result.getInvestmentProductId());
        // Units should be old + new = 10 + 15 = 25
        assertEquals(0, new BigDecimal("25").compareTo(result.getUnitsOwned()));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    void buyInvestmentShouldThrowInvalidInvestmentException_whenProductInactive() {
        investmentProduct.setActive(false);

        BuyInvestmentRequestDto request = BuyInvestmentRequestDto.builder()
                .investmentProductId(investmentProduct.getId())
                .units(new BigDecimal("15"))
                .build();

        when(investmentProductRepository.findById(investmentProduct.getId()))
                .thenReturn(Optional.of(investmentProduct));

        InvalidInvestmentException ex = assertThrows(InvalidInvestmentException.class, () ->
                portfolioService.buyInvestment(user, request));
        assertEquals("Investment product is not active", ex.getMessage());
    }

    @Test
    void buyInvestmentShouldThrowMinimumInvestmentException_whenInvestmentBelowMinimum() {
        BuyInvestmentRequestDto request = BuyInvestmentRequestDto.builder()
                .investmentProductId(investmentProduct.getId())
                .units(new BigDecimal("5")) // 5 * 100 = 500 < minimum 1000
                .build();

        when(investmentProductRepository.findById(investmentProduct.getId()))
                .thenReturn(Optional.of(investmentProduct));

        MinimumInvestmentException ex = assertThrows(MinimumInvestmentException.class, () ->
                portfolioService.buyInvestment(user, request));

        assertTrue(ex.getMessage().contains("Minimum investment required"));
    }

    @Test
    void sellInvestmentShouldSellInvestmentSuccessfully() {
        SellInvestmentRequestDto request = SellInvestmentRequestDto.builder()
                .investmentProductId(investmentProduct.getId())
                .units(new BigDecimal("5"))
                .build();

        when(investmentProductRepository.findById(investmentProduct.getId()))
                .thenReturn(Optional.of(investmentProduct));

        when(portfolioRepository.findByUserAndInvestmentProduct(user, investmentProduct))
                .thenReturn(Optional.of(portfolio));

        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioItemDto result = portfolioService.sellInvestment(user, request);

        assertNotNull(result);
        // Units should be 10 - 5 = 5
        assertEquals(0, new BigDecimal("5").compareTo(result.getUnitsOwned()));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    void sellInvestmentShouldThrowInsufficientUnitsException_whenNotEnoughUnits() {
        SellInvestmentRequestDto request = SellInvestmentRequestDto.builder()
                .investmentProductId(investmentProduct.getId())
                .units(new BigDecimal("20")) // more than owned 10
                .build();

        when(investmentProductRepository.findById(investmentProduct.getId()))
                .thenReturn(Optional.of(investmentProduct));

        when(portfolioRepository.findByUserAndInvestmentProduct(user, investmentProduct))
                .thenReturn(Optional.of(portfolio));

        InsufficientUnitsException ex = assertThrows(InsufficientUnitsException.class, () ->
                portfolioService.sellInvestment(user, request));

        assertTrue(ex.getMessage().contains("Not enough units to sell"));
    }

    @Test
    void getInvestmentByIdShouldReturnPortfolioItemDto() {
        when(portfolioRepository.findById(portfolio.getId()))
                .thenReturn(Optional.of(portfolio));

        PortfolioItemDto result = portfolioService.getInvestmentById(user, portfolio.getId());

        assertNotNull(result);
        assertEquals(portfolio.getId(), result.getId());
    }


}
