package com.zeta_horizon.investment_portfolio_tracker.service;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import jakarta.validation.Valid;

import java.util.List;

public interface InvestmentProductService {
    List<InvestmentProductListDto> getAllActiveProducts();

    List<InvestmentProductListDto> getAllProducts();

    InvestmentProductDto getProductById(Integer id);

    InvestmentProductDto createProduct(InvestmentProductCreateDto createDto);

    InvestmentProductDto updateProduct(Integer id, InvestmentProductUpdateDto updateDto);

    void deleteProduct(Integer id);

    List<InvestmentProductListDto> filterProducts(InvestmentProductFilterDto filterDto);

    List<InvestmentProductListDto> getProductsByRiskLevel(RiskLevel riskLevel);

    List<InvestmentProductListDto> getProductsByType(InvestmentType type);

    List<String> getInvestmentTypes();

    void updateIsActive(Integer id, Boolean active);
}
