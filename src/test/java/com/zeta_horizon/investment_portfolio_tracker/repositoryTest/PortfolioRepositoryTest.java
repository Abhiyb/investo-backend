package com.zeta_horizon.investment_portfolio_tracker.repositoryTest;

import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.entity.Portfolio;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PortfolioRepositoryTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    private User testUser;
    private InvestmentProduct testProduct;
    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testProduct = new InvestmentProduct();
        testProduct.setId(1);

        testPortfolio = Portfolio.builder()
                .id(1)
                .user(testUser)
                .investmentProduct(testProduct)
                .unitsOwned(BigDecimal.valueOf(100.50))
                .build();
    }

    @Test
    void findByUserReturnsUserPortfolios() {
        List<Portfolio> expectedPortfolios = List.of(testPortfolio);
        when(portfolioRepository.findByUser(testUser)).thenReturn(expectedPortfolios);

        List<Portfolio> result = portfolioRepository.findByUser(testUser);

        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
        verify(portfolioRepository, times(1)).findByUser(testUser);
    }

    @Test
    void findByUserAndInvestmentProductReturnsCorrectPortfolio() {
        when(portfolioRepository.findByUserAndInvestmentProduct(testUser, testProduct))
                .thenReturn(Optional.of(testPortfolio));

        Optional<Portfolio> result = portfolioRepository.findByUserAndInvestmentProduct(testUser, testProduct);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get().getUser());
        assertEquals(testProduct, result.get().getInvestmentProduct());
        verify(portfolioRepository, times(1)).findByUserAndInvestmentProduct(testUser, testProduct);
    }

    @Test
    void findByUserAndInvestmentProductReturnsEmptyIfNotFound() {
        when(portfolioRepository.findByUserAndInvestmentProduct(testUser, testProduct))
                .thenReturn(Optional.empty());

        Optional<Portfolio> result = portfolioRepository.findByUserAndInvestmentProduct(testUser, testProduct);

        assertFalse(result.isPresent());
        verify(portfolioRepository, times(1)).findByUserAndInvestmentProduct(testUser, testProduct);
    }
}
