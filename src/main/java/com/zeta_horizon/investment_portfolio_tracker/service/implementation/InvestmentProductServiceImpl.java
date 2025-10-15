package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.exception.ResourceNotFoundException;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.repository.InvestmentProductRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.InvestmentProductService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for InvestmentProduct business logic operations.
 *
 * This service class provides comprehensive business logic for managing investment products:
 * - CRUD operations with proper validation and error handling
 * - Entity-DTO mapping using ModelMapper
 * - Transactional support for data consistency
 * - Advanced filtering and search capabilities
 * - Performance monitoring and detailed logging
 * - Soft delete functionality
 */
@Slf4j
@Service
public class InvestmentProductServiceImpl implements InvestmentProductService {

    private final InvestmentProductRepository investmentProductRepository;
    private final ModelMapper modelMapper;

    /**
     * Constructor with dependency injection and ModelMapper configuration.
     *
     * Configures ModelMapper with STRICT matching strategy to ensure:
     * - Precise field mapping between entities and DTOs
     * - Prevention of accidental field mapping
     * - Better error detection during mapping operations
     *
     * @param investmentProductRepository Repository for database operations
     */
    @Autowired
    public InvestmentProductServiceImpl(InvestmentProductRepository investmentProductRepository) {
        log.info("Initializing InvestmentProductServiceImpl");

        this.investmentProductRepository = investmentProductRepository;
        this.modelMapper = new ModelMapper();

        // Configure ModelMapper for strict mapping to prevent mapping errors
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        log.debug("ModelMapper configured with STRICT matching strategy");
        log.info("InvestmentProductServiceImpl initialized successfully");
    }

    /**
     * Retrieves all active investment products.
     *
     * This method fetches all products that are currently active (not soft-deleted)
     * and converts them to DTOs for presentation layer consumption.
     * Uses read-only transaction for better performance.
     *
     * @return List of InvestmentProductListDto containing active products
     * @throws RuntimeException if database operation fails
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvestmentProductListDto> getAllActiveProducts() {
        log.info("Fetching all active investment products");

        try {
            List<InvestmentProduct> products = investmentProductRepository.findByIsActiveTrue();

            log.debug("Found {} active investment products in database", products.size());

            List<InvestmentProductListDto> productDtos = products.stream()
                    .map(product -> {
                        log.trace("Mapping product to DTO: {}", product.getName());
                        return modelMapper.map(product, InvestmentProductListDto.class);
                    })
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} active products",
                    productDtos.size());

            return productDtos;

        } catch (Exception e) {
            log.error("Error occurred while fetching active products", e);
            throw new RuntimeException("Failed to retrieve active investment products", e);
        }
    }

    /**
     * Retrieves all investment products (including inactive ones).
     *
     * @return List of InvestmentProductListDto containing all products
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvestmentProductListDto> getAllProducts() {
        log.info("Fetching all investment products (including inactive)");

        try {
            List<InvestmentProduct> products = investmentProductRepository.findAll();

            log.debug("Found {} total investment products in database", products.size());

            List<InvestmentProductListDto> productDtos = products.stream()
                    .map(product -> modelMapper.map(product, InvestmentProductListDto.class))
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} total products", productDtos.size());

            return productDtos;

        } catch (Exception e) {
            log.error("Error occurred while fetching all products", e);
            throw new RuntimeException("Failed to retrieve all investment products", e);
        }
    }

    /**
     * Retrieves a specific investment product by its ID
     *
     * @param id The unique identifier of the investment product
     * @return InvestmentProductDto containing detailed product information
     * @throws ResourceNotFoundException if product is not found or inactive
     */
    @Override
    @Transactional(readOnly = true)
    public InvestmentProductDto getProductById(Integer id) {
        log.info("Fetching investment product with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid product ID provided: {}", id);
            throw new IllegalArgumentException("Product ID must be a positive integer");
        }

        try {
            InvestmentProduct product = investmentProductRepository.findByIdAndIsActiveTrue(id)
                    .orElseThrow(() -> {
                        log.warn("Investment product not found or inactive with id: {}", id);
                        return new ResourceNotFoundException("Investment product not found with id: " + id);
                    });

            log.debug("Found investment product: {} (Type: {}, Risk: {})",
                    product.getName(), product.getType(), product.getRiskLevel());

            InvestmentProductDto productDto = modelMapper.map(product, InvestmentProductDto.class);

            log.info("Successfully retrieved product with id: {}", id);

            return productDto;

        } catch (ResourceNotFoundException e) {
            log.error("Product not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while fetching product with id: {}", id, e);
            throw new RuntimeException("Failed to retrieve investment product", e);
        }
    }

    /**
     * Creates a new investment product.
     *
     * @param createDto DTO containing product creation data
     * @return InvestmentProductDto of the created product
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    @Transactional
    public InvestmentProductDto createProduct(InvestmentProductCreateDto createDto) {
        log.info("Creating new investment product: {}", createDto.getName());
        InvestmentProduct product = modelMapper.map(createDto, InvestmentProduct.class);
        product.setActive(true);
        InvestmentProduct savedProduct = investmentProductRepository.save(product);
        log.info("Investment product created with id: {}", savedProduct.getId());
        return modelMapper.map(savedProduct, InvestmentProductDto.class);
    }

    /**
     * Updates an existing investment product.
     *
     * @param id The ID of the product to update
     * @param updateDto DTO containing update data
     * @return InvestmentProductDto of the updated product
     * @throws ResourceNotFoundException if product is not found
     */
    @Override
    @Transactional
    public InvestmentProductDto updateProduct(Integer id, InvestmentProductUpdateDto updateDto) {
        log.info("Updating investment product with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid product ID provided for update: {}", id);
            throw new IllegalArgumentException("Product ID must be a positive integer");
        }

        try {
            // Fetch existing product
            InvestmentProduct existingProduct = investmentProductRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Product not found for update with id: {}", id);
                        return new ResourceNotFoundException("Investment product not found with id: " + id);
                    });

            log.debug("Found existing product for update: {}", existingProduct.getName());

            // Track what fields are being updated
            StringBuilder updateLog = new StringBuilder("Updating fields: ");
            boolean hasUpdates = false;

            // Perform partial updates only for non-null fields
            if (updateDto.getName() != null && !updateDto.getName().equals(existingProduct.getName())) {
                log.debug("Updating name from '{}' to '{}'", existingProduct.getName(), updateDto.getName());
                existingProduct.setName(updateDto.getName());
                updateLog.append("name, ");
                hasUpdates = true;
            }

            if (updateDto.getType() != null && !updateDto.getType().equals(existingProduct.getType())) {
                log.debug("Updating type from '{}' to '{}'", existingProduct.getType(), updateDto.getType());
                existingProduct.setType(updateDto.getType());
                updateLog.append("type, ");
                hasUpdates = true;
            }

            if (updateDto.getRiskLevel() != null && !updateDto.getRiskLevel().equals(existingProduct.getRiskLevel())) {
                log.debug("Updating risk level from '{}' to '{}'", existingProduct.getRiskLevel(), updateDto.getRiskLevel());
                existingProduct.setRiskLevel(updateDto.getRiskLevel());
                updateLog.append("riskLevel, ");
                hasUpdates = true;
            }

            if (updateDto.getMinimumInvestment() != null &&
                    !updateDto.getMinimumInvestment().equals(existingProduct.getMinimumInvestment())) {
                log.debug("Updating minimum investment from '{}' to '{}'",
                        existingProduct.getMinimumInvestment(), updateDto.getMinimumInvestment());
                existingProduct.setMinimumInvestment(updateDto.getMinimumInvestment());
                updateLog.append("minimumInvestment, ");
                hasUpdates = true;
            }

            if (updateDto.getExpectedAnnualReturnRate() != null &&
                    !updateDto.getExpectedAnnualReturnRate().equals(existingProduct.getExpectedAnnualReturnRate())) {
                log.debug("Updating expected return rate from '{}' to '{}'",
                        existingProduct.getExpectedAnnualReturnRate(), updateDto.getExpectedAnnualReturnRate());
                existingProduct.setExpectedAnnualReturnRate(updateDto.getExpectedAnnualReturnRate());
                updateLog.append("expectedReturnRate, ");
                hasUpdates = true;
            }

            if (updateDto.getCurrentNetAssetValuePerUnit() != null &&
                    !updateDto.getCurrentNetAssetValuePerUnit().equals(existingProduct.getCurrentNetAssetValuePerUnit())) {
                log.debug("Updating NAV per unit from '{}' to '{}'",
                        existingProduct.getCurrentNetAssetValuePerUnit(), updateDto.getCurrentNetAssetValuePerUnit());
                existingProduct.setCurrentNetAssetValuePerUnit(updateDto.getCurrentNetAssetValuePerUnit());
                updateLog.append("navPerUnit, ");
                hasUpdates = true;
            }

            if (updateDto.getDescription() != null &&
                    !updateDto.getDescription().equals(existingProduct.getDescription())) {
                log.debug("Updating description");
                existingProduct.setDescription(updateDto.getDescription());
                updateLog.append("description, ");
                hasUpdates = true;
            }

            if (updateDto.getIsActive() != null &&
                    !updateDto.getIsActive().equals(existingProduct.isActive())) {
                log.debug("Updating active status from '{}' to '{}'",
                        existingProduct.isActive(), updateDto.getIsActive());
                existingProduct.setActive(updateDto.getIsActive());
                updateLog.append("isActive, ");
                hasUpdates = true;
            }

            if (!hasUpdates) {
                log.info("No changes detected for product id: {}, returning existing data", id);
            } else {
                log.debug(updateLog.toString());
            }

            // Save updated product
            InvestmentProduct updatedProduct = investmentProductRepository.save(existingProduct);
            InvestmentProductDto responseDto = modelMapper.map(updatedProduct, InvestmentProductDto.class);

            log.info("Successfully updated product with id: {} ", id);

            return responseDto;

        } catch (ResourceNotFoundException e) {
            log.error("Product not found for update with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating product with id: {}", id, e);
            throw new RuntimeException("Failed to update investment product", e);
        }
    }

    /**
     * Soft deletes an investment product by setting isActive to false.
     *
     * @param id The ID of the product to delete
     * @throws ResourceNotFoundException if product is not found
     */
    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        log.info("Soft deleting investment product with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid product ID provided for deletion: {}", id);
            throw new IllegalArgumentException("Product ID must be a positive integer");
        }

        try {
            InvestmentProduct existingProduct = investmentProductRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Product not found for deletion with id: {}", id);
                        return new ResourceNotFoundException("Investment product not found with id: " + id);
                    });

            if (!existingProduct.isActive()) {
                log.warn("Attempted to delete already inactive product with id: {}", id);
                return; // Product is already soft deleted
            }

            log.debug("Soft deleting product: {} (Type: {})",
                    existingProduct.getName(), existingProduct.getType());

            existingProduct.setActive(false);
            investmentProductRepository.save(existingProduct);

            log.info("Successfully soft deleted product '{}' with id: {}", existingProduct.getName(), id);

        } catch (ResourceNotFoundException e) {
            log.error("Product not found for deletion with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting product with id: {}", id, e);
            throw new RuntimeException("Failed to delete investment product", e);
        }
    }

    /**
     * Filters investment products based on multiple criteria.
     *
     * This method provides flexible filtering capabilities:
     * - Search by name (if searchTerm is provided)
     * - Filter by type, risk level, and maximum investment amount
     * - Supports partial matching for name searches
     * - Returns only active products
     *
     * Search priority:
     * 1. If searchTerm is provided, performs name-based search
     * 2. Otherwise, applies filters for type, risk level, and amount
     *
     * @param filterDto DTO containing filter criteria
     * @return List of InvestmentProductListDto matching the criteria
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvestmentProductListDto> filterProducts(InvestmentProductFilterDto filterDto) {
        log.info("Filtering investment products with criteria: search='{}', type={}, risk={}, maxAmount={}",
                filterDto.getSearchTerm(), filterDto.getType(), filterDto.getRiskLevel(),
                filterDto.getMaximumInvestmentAmount());

        try {
            List<InvestmentProduct> products;

            // Prioritize search term over other filters
            if (filterDto.getSearchTerm() != null && !filterDto.getSearchTerm().trim().isEmpty()) {
                String searchTerm = filterDto.getSearchTerm().trim();
                log.debug("Performing name-based search with term: '{}'", searchTerm);
                products = investmentProductRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(searchTerm);
                log.debug("Name search returned {} products", products.size());
            } else {
                log.debug("Performing criteria-based filtering");
                products = investmentProductRepository.findByFilters(
                        filterDto.getType(),
                        filterDto.getRiskLevel(),
                        filterDto.getMaximumInvestmentAmount()
                );
                log.debug("Criteria filtering returned {} products", products.size());
            }

            List<InvestmentProductListDto> resultDtos = products.stream()
                    .map(product -> {
                        log.trace("Including product in results: {}", product.getName());
                        return modelMapper.map(product, InvestmentProductListDto.class);
                    })
                    .collect(Collectors.toList());

            log.info("Successfully filtered products, returning {} results", resultDtos.size());

            return resultDtos;

        } catch (Exception e) {
            log.error("Error occurred while filtering products", e);
            throw new RuntimeException("Failed to filter investment products", e);
        }
    }

    /**
     * Retrieves all active investment products with a specific risk level.
     *
     * @param riskLevel The risk level to filter by
     * @return List of InvestmentProductListDto with the specified risk level
     * @throws IllegalArgumentException if riskLevel is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvestmentProductListDto> getProductsByRiskLevel(RiskLevel riskLevel) {
        log.info("Fetching investment products with risk level: {}", riskLevel);

        if (riskLevel == null) {
            log.warn("Null risk level provided");
            throw new IllegalArgumentException("Risk level cannot be null");
        }

        try {
            List<InvestmentProduct> products = investmentProductRepository.findByRiskLevelAndIsActiveTrue(riskLevel);

            log.debug("Found {} products with risk level: {}", products.size(), riskLevel);

            List<InvestmentProductListDto> productDtos = products.stream()
                    .map(product -> {
                        log.trace("Including product: {} (Min Investment: {})",
                                product.getName(), product.getMinimumInvestment());
                        return modelMapper.map(product, InvestmentProductListDto.class);
                    })
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} products with risk level {}",
                    productDtos.size(), riskLevel);

            return productDtos;

        } catch (Exception e) {
            log.error("Error occurred while fetching products by risk level: {}", riskLevel, e);
            throw new RuntimeException("Failed to retrieve products by risk level", e);
        }
    }

    /**
     * Retrieves all available investment types as strings.
     *
     * @return List of investment type names
     */
    @Override
    public List<String> getInvestmentTypes() {
        log.debug("Fetching all available investment types");

        try {
            List<String> types = Arrays.stream(InvestmentType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());

            log.debug("Available investment types: {}", types);
            log.info("Successfully retrieved {} investment types", types.size());

            return types;

        } catch (Exception e) {
            log.error("Error occurred while fetching investment types", e);
            throw new RuntimeException("Failed to retrieve investment types", e);
        }
    }

    @Override
    public void updateIsActive(Integer id, Boolean active) {
        InvestmentProduct investmentProduct = investmentProductRepository.findById(id).get();
        investmentProduct.setActive(active);
        investmentProductRepository.save(investmentProduct);
    }

    /**
     * Retrieves all active investment products of a specific type.
     *
     * @param type The investment type to filter by
     * @return List of InvestmentProductListDto of the specified type
     * @throws IllegalArgumentException if type is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvestmentProductListDto> getProductsByType(InvestmentType type) {
        log.info("Fetching investment products with type: {}", type);

        if (type == null) {
            log.warn("Null investment type provided");
            throw new IllegalArgumentException("Investment type cannot be null");
        }

        try {
            List<InvestmentProduct> products = investmentProductRepository.findByTypeAndIsActiveTrue(type);

            log.debug("Found {} products with type: {}", products.size(), type);

            List<InvestmentProductListDto> productDtos = products.stream()
                    .map(product -> {
                        log.trace("Including product: {} (Risk: {}, Min Investment: {})",
                                product.getName(), product.getRiskLevel(), product.getMinimumInvestment());
                        return modelMapper.map(product, InvestmentProductListDto.class);
                    })
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} products with type {} ", productDtos.size(), type);

            return productDtos;

        } catch (Exception e) {
            log.error("Error occurred while fetching products by type: {}", type, e);
            throw new RuntimeException("Failed to retrieve products by type", e);
        }
    }

    /**
     * Private helper method to validate product creation DTO.
     *
     * @param createDto The DTO to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCreateDto(InvestmentProductCreateDto createDto) {
        log.debug("Validating product creation DTO");

        if (createDto == null) {
            log.error("Create DTO is null");
            throw new IllegalArgumentException("Product creation data cannot be null");
        }

        if (createDto.getName() == null || createDto.getName().trim().isEmpty()) {
            log.error("Product name is null or empty");
            throw new IllegalArgumentException("Product name is required");
        }

        if (createDto.getType() == null) {
            log.error("Product type is null");
            throw new IllegalArgumentException("Product type is required");
        }

        if (createDto.getRiskLevel() == null) {
            log.error("Risk level is null");
            throw new IllegalArgumentException("Risk level is required");
        }

        if (createDto.getMinimumInvestment() == null || createDto.getMinimumInvestment().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid minimum investment amount: {}", createDto.getMinimumInvestment());
            throw new IllegalArgumentException("Minimum investment must be greater than zero");
        }

        if (createDto.getExpectedAnnualReturnRate() == null || createDto.getExpectedAnnualReturnRate().compareTo(BigDecimal.ZERO) < 0) {
            log.error("Invalid expected return rate: {}", createDto.getExpectedAnnualReturnRate());
            throw new IllegalArgumentException("Expected annual return rate cannot be negative");
        }

        if (createDto.getCurrentNetAssetValuePerUnit() == null || createDto.getCurrentNetAssetValuePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid NAV per unit: {}", createDto.getCurrentNetAssetValuePerUnit());
            throw new IllegalArgumentException("Current NAV per unit must be greater than zero");
        }

        log.debug("Product creation DTO validation passed");
    }
}
