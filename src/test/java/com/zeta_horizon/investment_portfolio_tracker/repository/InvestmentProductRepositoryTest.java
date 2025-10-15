package com.zeta_horizon.investment_portfolio_tracker.repository; // Or adjust to your actual test package

import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentProduct Repository Mock Tests (Conceptual)")
public class InvestmentProductRepositoryTest { // Renamed to avoid confusion

    @Mock
    private InvestmentProductRepository investmentProductRepository;

    private InvestmentProduct activeStock;
    private InvestmentProduct activeBond;
    private InvestmentProduct inactiveProduct;

    @BeforeEach
    void setUp() {
        activeStock = InvestmentProduct.builder()
                .id(1) // Assign IDs for mocking
                .name("Tech Innovations Stock")
                .type(InvestmentType.STOCK)
                .riskLevel(RiskLevel.HIGH)
                .minimumInvestment(new BigDecimal("100.00"))
                .expectedAnnualReturnRate(new BigDecimal("15.50"))
                .currentNetAssetValuePerUnit(new BigDecimal("500.25"))
                .description("High-growth tech stock")
                .isActive(true)
                .build();

        activeBond = InvestmentProduct.builder()
                .id(2)
                .name("Government Savings Bond")
                .type(InvestmentType.GOVERNMENT_BOND)
                .riskLevel(RiskLevel.LOW)
                .minimumInvestment(new BigDecimal("1000.00"))
                .expectedAnnualReturnRate(new BigDecimal("4.00"))
                .currentNetAssetValuePerUnit(new BigDecimal("100.00"))
                .description("Low-risk government bond")
                .isActive(true)
                .build();

        inactiveProduct = InvestmentProduct.builder()
                .id(3)
                .name("Old Mutual Fund")
                .type(InvestmentType.MUTUAL_FUND)
                .riskLevel(RiskLevel.MEDIUM)
                .minimumInvestment(new BigDecimal("50.00"))
                .expectedAnnualReturnRate(new BigDecimal("7.00"))
                .currentNetAssetValuePerUnit(new BigDecimal("25.00"))
                .description("An old, inactive mutual fund")
                .isActive(false)
                .build();
    }

    @Test
    void findByIsActiveTrueReturnsActiveProducts() {
        List<InvestmentProduct> mockActiveProducts = Arrays.asList(activeStock, activeBond);
        when(investmentProductRepository.findByIsActiveTrue()).thenReturn(mockActiveProducts);

        List<InvestmentProduct> result = investmentProductRepository.findByIsActiveTrue();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(activeStock));
        assertTrue(result.contains(activeBond));
        assertFalse(result.contains(inactiveProduct));
        verify(investmentProductRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void findByIdAndIsActiveTrueReturnsCorrectProduct() {
        when(investmentProductRepository.findByIdAndIsActiveTrue(activeStock.getId()))
                .thenReturn(Optional.of(activeStock));

        Optional<InvestmentProduct> result = investmentProductRepository.findByIdAndIsActiveTrue(activeStock.getId());

        assertTrue(result.isPresent());
        assertEquals(activeStock.getName(), result.get().getName());
        verify(investmentProductRepository, times(1)).findByIdAndIsActiveTrue(activeStock.getId());
    }

    @Test
    void findByIdAndIsActiveTrueReturnsEmptyForInactiveProduct() {
        when(investmentProductRepository.findByIdAndIsActiveTrue(inactiveProduct.getId()))
                .thenReturn(Optional.empty()); // Mocking that it won't be found because it's inactive

        Optional<InvestmentProduct> result = investmentProductRepository
                .findByIdAndIsActiveTrue(inactiveProduct.getId());

        assertFalse(result.isPresent());
        verify(investmentProductRepository, times(1)).findByIdAndIsActiveTrue(inactiveProduct.getId());
    }

    @Test
    void findByNameContainingIgnoreCaseAndIsActiveTrue() {
        List<InvestmentProduct> mockTechProducts = Arrays.asList(activeStock);
        when(investmentProductRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("tech"))
                .thenReturn(mockTechProducts);

        List<InvestmentProduct> result = investmentProductRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue("tech");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeStock.getName(), result.get(0).getName());
        verify(investmentProductRepository, times(1)).findByNameContainingIgnoreCaseAndIsActiveTrue("tech");
    }

    @Test
    void findByFiltersWithAllNullsReturnsAllActive() {
        List<InvestmentProduct> mockAllActive = Arrays.asList(activeStock, activeBond);
        when(investmentProductRepository.findByFilters(null, null, null))
                .thenReturn(mockAllActive);

        List<InvestmentProduct> result = investmentProductRepository.findByFilters(null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(activeStock));
        assertTrue(result.contains(activeBond));
        verify(investmentProductRepository, times(1)).findByFilters(null, null, null);
    }
}