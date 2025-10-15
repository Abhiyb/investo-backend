package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.exception.ResourceNotFoundException;
import com.zeta_horizon.investment_portfolio_tracker.repository.InvestmentProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentProduct Service Tests")
class InvestmentProductServiceImplTest {

    @Mock
    private InvestmentProductRepository investmentProductRepository;

    @InjectMocks
    private InvestmentProductServiceImpl investmentProductService;

    private InvestmentProduct testProduct;
    private InvestmentProductCreateDto createDto;
    private InvestmentProductUpdateDto updateDto;
    private InvestmentProductFilterDto filterDto;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct();
        createDto = createTestCreateDto();
        updateDto = createTestUpdateDto();
        filterDto = createTestFilterDto();
    }

    @Test
    @DisplayName("Should retrieve all active products successfully")
    void testGetAllActiveProducts() {
        // Given
        List<InvestmentProduct> mockProducts = Arrays.asList(testProduct, createTestProduct());
        when(investmentProductRepository.findByIsActiveTrue()).thenReturn(mockProducts);

        // When
        List<InvestmentProductListDto> result = investmentProductService.getAllActiveProducts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Test Mutual Fund");
        verify(investmentProductRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should handle empty list when no active products exist")
    void testGetAllActiveProducts_EmptyList() {
        // Given
        when(investmentProductRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        // When
        List<InvestmentProductListDto> result = investmentProductService.getAllActiveProducts();

        // Then
        assertThat(result).isEmpty();
        verify(investmentProductRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should retrieve all products including inactive ones")
    void testGetAllProducts() {
        // Given
        List<InvestmentProduct> mockProducts = Arrays.asList(testProduct);
        when(investmentProductRepository.findAll()).thenReturn(mockProducts);

        // When
        List<InvestmentProductListDto> result = investmentProductService.getAllProducts();

        // Then
        assertThat(result).hasSize(1);
        verify(investmentProductRepository).findAll();
    }

    @Test
    @DisplayName("Should retrieve product by ID successfully")
    void testGetProductById() {
        // Given
        Integer productId = 1;
        when(investmentProductRepository.findByIdAndIsActiveTrue(productId))
                .thenReturn(Optional.of(testProduct));

        // When
        InvestmentProductDto result = investmentProductService.getProductById(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Mutual Fund");
        assertThat(result.getId()).isEqualTo(productId);
        verify(investmentProductRepository).findByIdAndIsActiveTrue(productId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testGetProductById_NotFound() {
        // Given
        Integer productId = 999;
        when(investmentProductRepository.findByIdAndIsActiveTrue(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> investmentProductService.getProductById(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Investment product not found with id: " + productId);

        verify(investmentProductRepository).findByIdAndIsActiveTrue(productId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid product ID")
    void testGetProductById_InvalidId() {
        // When & Then
        assertThatThrownBy(() -> investmentProductService.getProductById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID must be a positive integer");

        assertThatThrownBy(() -> investmentProductService.getProductById(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID must be a positive integer");

        assertThatThrownBy(() -> investmentProductService.getProductById(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID must be a positive integer");
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() {
        // Given
        InvestmentProduct savedProduct = createTestProduct();
        savedProduct.setId(1);
        when(investmentProductRepository.save(any(InvestmentProduct.class))).thenReturn(savedProduct);

        // When
        InvestmentProductDto result = investmentProductService.createProduct(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Test Mutual Fund");
        assertThat(result.isActive()).isTrue();
        verify(investmentProductRepository).save(any(InvestmentProduct.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        // Given
        Integer productId = 1;
        InvestmentProduct existingProduct = createTestProduct();
        existingProduct.setId(productId);

        when(investmentProductRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(investmentProductRepository.save(any(InvestmentProduct.class))).thenReturn(existingProduct);

        // When
        InvestmentProductDto result = investmentProductService.updateProduct(productId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Mutual Fund");
        verify(investmentProductRepository).findById(productId);
        verify(investmentProductRepository).save(any(InvestmentProduct.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void testUpdateProduct_NotFound() {
        // Given
        Integer productId = 999;
        when(investmentProductRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> investmentProductService.updateProduct(productId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Investment product not found with id: " + productId);

        verify(investmentProductRepository).findById(productId);
        verify(investmentProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete product successfully")
    void testDeleteProduct() {
        // Given
        Integer productId = 1;
        InvestmentProduct existingProduct = createTestProduct();
        existingProduct.setId(productId);
        existingProduct.setActive(true);

        when(investmentProductRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(investmentProductRepository.save(any(InvestmentProduct.class))).thenReturn(existingProduct);

        // When
        investmentProductService.deleteProduct(productId);

        // Then
        verify(investmentProductRepository).findById(productId);
        verify(investmentProductRepository).save(argThat(product -> !product.isActive()));
    }

    @Test
    @DisplayName("Should handle deletion of already inactive product")
    void testDeleteProduct_AlreadyInactive() {
        // Given
        Integer productId = 1;
        InvestmentProduct existingProduct = createTestProduct();
        existingProduct.setId(productId);
        existingProduct.setActive(false);

        when(investmentProductRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // When
        investmentProductService.deleteProduct(productId);

        // Then
        verify(investmentProductRepository).findById(productId);
        verify(investmentProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should filter products by search term")
    void testFilterProducts_WithSearchTerm() {
        // Given
        filterDto.setSearchTerm("mutual");
        List<InvestmentProduct> mockProducts = Arrays.asList(testProduct);
        when(investmentProductRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("mutual"))
                .thenReturn(mockProducts);

        // When
        List<InvestmentProductListDto> result = investmentProductService.filterProducts(filterDto);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Mutual Fund");
        verify(investmentProductRepository).findByNameContainingIgnoreCaseAndIsActiveTrue("mutual");
    }

    @Test
    @DisplayName("Should filter products by criteria when no search term")
    void testFilterProducts_WithCriteria() {
        // Given
        filterDto.setSearchTerm(null);
        List<InvestmentProduct> mockProducts = Arrays.asList(testProduct);
        when(investmentProductRepository.findByFilters(
                filterDto.getType(), filterDto.getRiskLevel(), filterDto.getMaximumInvestmentAmount()))
                .thenReturn(mockProducts);

        // When
        List<InvestmentProductListDto> result = investmentProductService.filterProducts(filterDto);

        // Then
        assertThat(result).hasSize(1);
        verify(investmentProductRepository).findByFilters(
                filterDto.getType(), filterDto.getRiskLevel(), filterDto.getMaximumInvestmentAmount());
    }

    @Test
    @DisplayName("Should get products by risk level")
    void testGetProductsByRiskLevel() {
        // Given
        RiskLevel riskLevel = RiskLevel.MEDIUM;
        List<InvestmentProduct> mockProducts = Arrays.asList(testProduct);
        when(investmentProductRepository.findByRiskLevelAndIsActiveTrue(riskLevel))
                .thenReturn(mockProducts);

        // When
        List<InvestmentProductListDto> result = investmentProductService.getProductsByRiskLevel(riskLevel);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRiskLevel()).isEqualTo(riskLevel);
        verify(investmentProductRepository).findByRiskLevelAndIsActiveTrue(riskLevel);
    }

    @Test
    @DisplayName("Should get products by type")
    void testGetProductsByType() {
        // Given
        InvestmentType type = InvestmentType.MUTUAL_FUND;
        List<InvestmentProduct> mockProducts = Arrays.asList(testProduct);
        when(investmentProductRepository.findByTypeAndIsActiveTrue(type)).thenReturn(mockProducts);

        // When
        List<InvestmentProductListDto> result = investmentProductService.getProductsByType(type);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(type);
        verify(investmentProductRepository).findByTypeAndIsActiveTrue(type);
    }

    @Test
    @DisplayName("Should get all investment types")
    void testGetInvestmentTypes() {
        // When
        List<String> result = investmentProductService.getInvestmentTypes();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).contains("MUTUAL_FUND", "STOCK", "GOVERNMENT_BOND");
        assertThat(result).hasSize(InvestmentType.values().length);
    }

    // Helper methods
    private InvestmentProduct createTestProduct() {
        InvestmentProduct product = new InvestmentProduct();
        product.setId(1);
        product.setName("Test Mutual Fund");
        product.setType(InvestmentType.MUTUAL_FUND);
        product.setRiskLevel(RiskLevel.MEDIUM);
        product.setMinimumInvestment(new BigDecimal("1000"));
        product.setExpectedAnnualReturnRate(new BigDecimal("12.5"));
        product.setCurrentNetAssetValuePerUnit(new BigDecimal("15.50"));
        product.setDescription("Test description");
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private InvestmentProductCreateDto createTestCreateDto() {
        InvestmentProductCreateDto dto = new InvestmentProductCreateDto();
        dto.setName("Test Mutual Fund");
        dto.setType(InvestmentType.MUTUAL_FUND);
        dto.setRiskLevel(RiskLevel.MEDIUM);
        dto.setMinimumInvestment(new BigDecimal("1000"));
        dto.setExpectedAnnualReturnRate(new BigDecimal("12.5"));
        dto.setCurrentNetAssetValuePerUnit(new BigDecimal("15.50"));
        dto.setDescription("Test description");
        return dto;
    }

    private InvestmentProductUpdateDto createTestUpdateDto() {
        InvestmentProductUpdateDto dto = new InvestmentProductUpdateDto();
        dto.setName("Updated Mutual Fund");
        dto.setType(InvestmentType.MUTUAL_FUND);
        dto.setRiskLevel(RiskLevel.HIGH);
        dto.setMinimumInvestment(new BigDecimal("2000"));
        dto.setExpectedAnnualReturnRate(new BigDecimal("15.0"));
        dto.setCurrentNetAssetValuePerUnit(new BigDecimal("18.75"));
        dto.setDescription("Updated description");
        dto.setIsActive(true);
        return dto;
    }

    private InvestmentProductFilterDto createTestFilterDto() {
        InvestmentProductFilterDto dto = new InvestmentProductFilterDto();
        dto.setType(InvestmentType.MUTUAL_FUND);
        dto.setRiskLevel(RiskLevel.MEDIUM);
        dto.setMaximumInvestmentAmount(new BigDecimal("5000"));
        return dto;
    }
}