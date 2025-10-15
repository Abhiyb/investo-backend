package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.exception.ResourceNotFoundException;
import com.zeta_horizon.investment_portfolio_tracker.service.InvestmentProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentProductControllerTest {

    @Mock
    private InvestmentProductService investmentProductService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private InvestmentProductController investmentProductController;

    private InvestmentProductDto productDto;
    private InvestmentProductListDto productListDto;
    private InvestmentProductCreateDto createDto;
    private InvestmentProductUpdateDto updateDto;
    private InvestmentProductFilterDto filterDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        productDto = new InvestmentProductDto();
        productDto.setId(1);
        productDto.setName("Test Product");
        productDto.setType(InvestmentType.STOCK);
        productDto.setRiskLevel(RiskLevel.MEDIUM);
        productDto.setMinimumInvestment(BigDecimal.valueOf(1000));
        productDto.setExpectedAnnualReturnRate(BigDecimal.valueOf(8.5));
        productDto.setCurrentNetAssetValuePerUnit(BigDecimal.valueOf(150));
        productDto.setDescription("Test Description");
        productDto.setActive(true);
        productDto.setCreatedAt(LocalDateTime.now());
        productDto.setUpdatedAt(LocalDateTime.now());

        productListDto = new InvestmentProductListDto();
        productListDto.setId(1);
        productListDto.setName("Test Product");
        productListDto.setType(InvestmentType.STOCK);
        productListDto.setRiskLevel(RiskLevel.MEDIUM);
        productListDto.setMinimumInvestment(BigDecimal.valueOf(1000));
        productListDto.setExpectedAnnualReturnRate(BigDecimal.valueOf(8.5));
        productListDto.setCurrentNetAssetValuePerUnit(BigDecimal.valueOf(150));
        productListDto.setActive(true);

        createDto = new InvestmentProductCreateDto();
        createDto.setName("New Product");
        createDto.setType(InvestmentType.MUTUAL_FUND);
        createDto.setRiskLevel(RiskLevel.LOW);
        createDto.setMinimumInvestment(BigDecimal.valueOf(500));
        createDto.setExpectedAnnualReturnRate(BigDecimal.valueOf(6.5));
        createDto.setCurrentNetAssetValuePerUnit(BigDecimal.valueOf(100));
        createDto.setDescription("New Description");

        updateDto = new InvestmentProductUpdateDto();
        updateDto.setName("Updated Product");
        updateDto.setType(InvestmentType.STOCK);
        updateDto.setRiskLevel(RiskLevel.HIGH);
        updateDto.setMinimumInvestment(BigDecimal.valueOf(2000));
        updateDto.setExpectedAnnualReturnRate(BigDecimal.valueOf(10.5));
        updateDto.setCurrentNetAssetValuePerUnit(BigDecimal.valueOf(200));
        updateDto.setDescription("Updated Description");
        updateDto.setIsActive(true);

        filterDto = new InvestmentProductFilterDto();
        filterDto.setType(InvestmentType.STOCK);
        filterDto.setRiskLevel(RiskLevel.MEDIUM);
        filterDto.setMaximumInvestmentAmount(BigDecimal.valueOf(5000));
    }

    // =================================================================
    // PUBLIC ENDPOINT TESTS
    // =================================================================

    @Test
    void getAllActiveProducts_ShouldReturnListOfProducts() {
        // Arrange
        List<InvestmentProductListDto> products = Collections.singletonList(productListDto);
        when(investmentProductService.getAllActiveProducts()).thenReturn(products);

        // Act
        ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> response =
                investmentProductController.getAllActiveProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Test Product", response.getBody().getData().get(0).getName());
        verify(investmentProductService, times(1)).getAllActiveProducts();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        when(investmentProductService.getProductById(1)).thenReturn(productDto);

        // Act
        ResponseEntity<SuccessResponse<InvestmentProductDto>> response =
                investmentProductController.getProductById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getData().getName());
        verify(investmentProductService, times(1)).getProductById(1);
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.getProductById(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.getProductById(null);
        });

        verify(investmentProductService, never()).getProductById(anyInt());
    }

    @Test
    void getProductById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(investmentProductService.getProductById(999)).thenThrow(
                new ResourceNotFoundException("Investment product not found with id: 999"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            investmentProductController.getProductById(999);
        });

        verify(investmentProductService, times(1)).getProductById(999);
    }

    @Test
    void getInvestmentTypes_ShouldReturnListOfTypes() {
        // Arrange
        List<String> types = Arrays.asList("STOCK", "BOND", "MUTUAL_FUND");
        when(investmentProductService.getInvestmentTypes()).thenReturn(types);

        // Act
        ResponseEntity<SuccessResponse<List<String>>> response =
                investmentProductController.getInvestmentTypes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getData().size());
        verify(investmentProductService, times(1)).getInvestmentTypes();
    }

    @Test
    void getProductsByType_WithValidType_ShouldReturnProducts() {
        // Arrange
        List<InvestmentProductListDto> products = Collections.singletonList(productListDto);
        when(investmentProductService.getProductsByType(InvestmentType.STOCK)).thenReturn(products);

        // Act
        ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> response =
                investmentProductController.getProductsByType(InvestmentType.STOCK);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Test Product", response.getBody().getData().get(0).getName());
        verify(investmentProductService, times(1)).getProductsByType(InvestmentType.STOCK);
    }

    @Test
    void getProductsByType_WithNullType_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.getProductsByType(null);
        });

        verify(investmentProductService, never()).getProductsByType(any());
    }

    @Test
    void getProductsByRiskLevel_WithValidRiskLevel_ShouldReturnProducts() {
        // Arrange
        List<InvestmentProductListDto> products = Collections.singletonList(productListDto);
        when(investmentProductService.getProductsByRiskLevel(RiskLevel.MEDIUM)).thenReturn(products);

        // Act
        ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> response =
                investmentProductController.getProductsByRiskLevel(RiskLevel.MEDIUM);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Test Product", response.getBody().getData().get(0).getName());
        verify(investmentProductService, times(1)).getProductsByRiskLevel(RiskLevel.MEDIUM);
    }

    @Test
    void getProductsByRiskLevel_WithNullRiskLevel_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.getProductsByRiskLevel(null);
        });

        verify(investmentProductService, never()).getProductsByRiskLevel(any());
    }

    @Test
    void filterProducts_WithValidFilter_ShouldReturnProducts() {
        // Arrange
        List<InvestmentProductListDto> products = Collections.singletonList(productListDto);
        when(investmentProductService.filterProducts(filterDto)).thenReturn(products);

        // Act
        ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> response =
                investmentProductController.filterProducts(filterDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Test Product", response.getBody().getData().get(0).getName());
        verify(investmentProductService, times(1)).filterProducts(filterDto);
    }

    @Test
    void filterProducts_WithNullFilter_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.filterProducts(null);
        });

        verify(investmentProductService, never()).filterProducts(any());
    }

    // =================================================================
    // ADMIN ENDPOINT TESTS
    // =================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        // Arrange
        when(investmentProductService.createProduct(createDto)).thenReturn(productDto);

        // Act
        ResponseEntity<SuccessResponse<InvestmentProductDto>> response =
                investmentProductController.createProduct(createDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getData().getName());
        verify(investmentProductService, times(1)).createProduct(createDto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<InvestmentProductListDto> products = Collections.singletonList(productListDto);
        when(investmentProductService.getAllProducts()).thenReturn(products);

        // Act
        ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> response =
                investmentProductController.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Test Product", response.getBody().getData().get(0).getName());
        verify(investmentProductService, times(1)).getAllProducts();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.updateProduct(0, updateDto);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.updateProduct(null, updateDto);
        });

        verify(investmentProductService, never()).updateProduct(anyInt(), any());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_WithValidId_ShouldReturnSuccess() {
        // Arrange
        doNothing().when(investmentProductService).deleteProduct(1);

        // Act
        ResponseEntity<SuccessResponse<String>> response =
                investmentProductController.deleteProduct(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Deleted", response.getBody().getData());
        verify(investmentProductService, times(1)).deleteProduct(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.deleteProduct(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            investmentProductController.deleteProduct(null);
        });

        verify(investmentProductService, never()).deleteProduct(anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_WithNonExistentId_ShouldThrowException() {
        // Arrange
        doThrow(new ResourceNotFoundException("Investment product not found with id: 999"))
                .when(investmentProductService).deleteProduct(999);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            investmentProductController.deleteProduct(999);
        });

        verify(investmentProductService, times(1)).deleteProduct(999);
    }
}