package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.exception.ResourceNotFoundException;
import com.zeta_horizon.investment_portfolio_tracker.service.InvestmentProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for managing Investment Product operations in the portfolio tracking system.
 */
@Slf4j
@RestController
public class InvestmentProductController {

    private final InvestmentProductService investmentProductService;

    /**
     * Constructor for dependency injection of InvestmentProductService.
     *
     * @param investmentProductService Service layer component for business logic
     */
    @Autowired
    public InvestmentProductController(InvestmentProductService investmentProductService) {
        this.investmentProductService = investmentProductService;
        log.info("InvestmentProductController initialized successfully");
    }

    // ================================================================================================
    // PUBLIC ENDPOINTS - accessible to all users
    // ================================================================================================

    /**
     * Retrieves all active investment products available for investment(excludes inactive).
     *
     * @return ResponseEntity containing SuccessResponse with list of active products
     * @throws RuntimeException if database operation fails
     */
    @GetMapping("investments")
    public ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> getAllActiveProducts() {
        log.info("GET /api/investments - Fetching all active investment products");

        try {
            // Fetch active products from service layer
            List<InvestmentProductListDto> products = investmentProductService.getAllActiveProducts();

            log.debug("Retrieved {} active products", products.size());

            // Create success response
            SuccessResponse<List<InvestmentProductListDto>> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    products,
                    String.format("Successfully fetched %d investment products from database", products.size()),
                    LocalDateTime.now()
            );

            log.info("GET /api/investments completed successfully - returned {} products", products.size());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while fetching all active products", e);
            throw new RuntimeException("Failed to retrieve active investment products. Please try again later.", e);
        }
    }

    /**
     * Retrieves detailed information about a specific investment product by ID.
     *
     * @param id The unique identifier of the investment product (must be positive integer)
     * @return ResponseEntity containing SuccessResponse with detailed product information
     * @throws ResourceNotFoundException if product is not found or inactive
     * @throws IllegalArgumentException if ID is invalid (null or non-positive)
     */
    @GetMapping("/investments/{id}")
    public ResponseEntity<SuccessResponse<InvestmentProductDto>> getProductById(@PathVariable Integer id) {
        log.info("GET /api/investments/{} - Fetching investment product details", id);

        // Input validation
        if (id == null || id <= 0) {
            log.warn("Invalid product ID received: {}", id);
            throw new IllegalArgumentException("Product ID must be a positive integer");
        }

        try {

            // Fetch product details from service
            InvestmentProductDto product = investmentProductService.getProductById(id);

            log.debug("Retrieved product details for ID {} - Product: '{}'", id, product.getName());

            // Create success response
            SuccessResponse<InvestmentProductDto> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    product,
                    String.format("Successfully fetched investment product with id %d", id),
                    LocalDateTime.now()
            );

            log.info("GET /api/investments/{} completed successfully - Product: '{}'", id, product.getName());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            log.warn("Product not found with ID: {}", id);
            throw e; // Re-throw to be handled by global exception handler
        } catch (Exception e) {
            log.error("Error occurred while fetching product with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve investment product details. Please try again later.", e);
        }
    }

    /**
     * Retrieves all available investment types in the system.
     *
     * @return ResponseEntity containing SuccessResponse with list of investment type names
     * @throws RuntimeException if operation fails
     */
    @GetMapping("/investmentTypes")
    public ResponseEntity<SuccessResponse<List<String>>> getInvestmentTypes() {
        log.info("GET /api/investment-types - Fetching all available investment types");

        try {

            // Get investment types from service
            List<String> investmentTypes = investmentProductService.getInvestmentTypes();

            log.debug("Retrieved {} investment types", investmentTypes.size());

            // Create success response
            SuccessResponse<List<String>> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    investmentTypes,
                    "Successfully fetched all investment types",
                    LocalDateTime.now()
            );

            log.info("GET /api/investment-types completed successfully - returned {} types", investmentTypes.size());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while fetching investment types", e);
            throw new RuntimeException("Failed to retrieve investment types. Please try again later.", e);
        }
    }

    /**
     * Retrieves all active investment products of a specific type.
     *
     * @param type The investment type to filter by (must be valid enum value)
     * @return ResponseEntity containing SuccessResponse with filtered products
     * @throws IllegalArgumentException if investment type is invalid
     * @throws RuntimeException if database operation fails
     */
    @GetMapping("/investments/type/{type}")
    public ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> getProductsByType(
            @PathVariable InvestmentType type) {
        log.info("GET /api/investments/type/{} - Fetching products by investment type", type);

        if (type == null) {
            log.warn("Null investment type received in path parameter");
            throw new IllegalArgumentException("Investment type cannot be null");
        }

        try {

            // Fetch products by type from service
            List<InvestmentProductListDto> products = investmentProductService.getProductsByType(type);

            log.debug("Retrieved {} products of type {}", products.size(), type);

            // Create success response
            SuccessResponse<List<InvestmentProductListDto>> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    products,
                    String.format("Successfully fetched %s type investment products", type),
                    LocalDateTime.now()
            );

            log.info("GET /api/investments/type/{} completed successfully - returned {} products",
                    type, products.size());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while fetching products by type: {}", type, e);
            throw new RuntimeException("Failed to retrieve products by type. Please try again later.", e);
        }
    }

    /**
     * Retrieves all active investment products with a specific risk level.
     *
     * @param riskLevel The risk level to filter by (LOW, MEDIUM, HIGH)
     * @return ResponseEntity containing SuccessResponse with filtered products
     * @throws IllegalArgumentException if risk level is invalid
     * @throws RuntimeException if database operation fails
     */
    @GetMapping("/investments/risk/{riskLevel}")
    public ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> getProductsByRiskLevel(
            @PathVariable RiskLevel riskLevel) {
        log.info("GET /api/investments/risk/{} - Fetching products by risk level", riskLevel);

        if (riskLevel == null) {
            log.warn("Null risk level received in path parameter");
            throw new IllegalArgumentException("Risk level cannot be null");
        }

        try {

            // Fetch products by risk level from service
            List<InvestmentProductListDto> products = investmentProductService.getProductsByRiskLevel(riskLevel);

            log.debug("Retrieved {} products with risk level {}",
                    products.size(), riskLevel);

            // Create success response
            SuccessResponse<List<InvestmentProductListDto>> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    products,
                    String.format("Successfully fetched investment products with %s risk level", riskLevel),
                    LocalDateTime.now()
            );

            log.info("GET /api/investments/risk/{} completed successfully - returned {} products",
                    riskLevel, products.size());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while fetching products by risk level: {}", riskLevel, e);
            throw new RuntimeException("Failed to retrieve products by risk level. Please try again later.", e);
        }
    }

    /**
     * Filters investment products based on multiple criteria.
     *
     * This endpoint provides advanced filtering capabilities allowing users to
     * search and filter products based on various criteria:
     *
     * - Search by name (partial, case-insensitive matching)
     * - Filter by investment type
     * - Filter by risk level
     * - Filter by maximum investment amount
     *
     * @param filterDto DTO containing filter criteria (all fields are optional)
     * @return ResponseEntity containing SuccessResponse with filtered products
     * @throws RuntimeException if filtering operation fails
     */
    @PostMapping("/investments/filter")
    public ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> filterProducts(
            @RequestBody InvestmentProductFilterDto filterDto) {
        log.info("POST /api/investments/filter - Filtering products with criteria: {}", filterDto);

        if (filterDto == null) {
            log.warn("Null filter DTO received");
            throw new IllegalArgumentException("Filter criteria cannot be null");
        }

        try {

            // Apply filters using service layer
            List<InvestmentProductListDto> products = investmentProductService.filterProducts(filterDto);

            log.debug("Filtering completed - found {} matching products", products.size());

            // Create success response
            SuccessResponse<List<InvestmentProductListDto>> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    products,
                    String.format("Successfully fetched investment products with filter criteria"),
                    LocalDateTime.now()
            );

            log.info("POST /api/investments/filter completed successfully - returned {} products", products.size());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while filtering products with criteria: {}", filterDto, e);
            throw new RuntimeException("Failed to filter investment products. Please try again later.", e);
        }
    }

    // ================================================================================================
    // ADMIN ENDPOINTS - Require ADMIN role authentication
    // ================================================================================================

    /**
     * Creates a new investment product in the system.
     *
     * This endpoint allows administrators to add new investment products to the
     * system. The created product will be automatically set as active and available
     * for investment. All required fields must be provided and will be validated.
     *
     * @param createDto DTO containing product creation data
     * @return ResponseEntity with 201 status and created product details
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if creation operation fails
     */
    @PostMapping("admin/investments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<InvestmentProductDto>> createProduct(
            @Valid @RequestBody InvestmentProductCreateDto createDto) {
        log.info("Admin request: Create new investment product {}", createDto.getName());
        InvestmentProductDto createdProduct = investmentProductService.createProduct(createDto);
        log.debug("Investment product created with ID {}", createdProduct.getId());
        return new ResponseEntity<>(new SuccessResponse<>(HttpStatus.OK.value(),
                createdProduct, "Successfully created a investment product",
                LocalDateTime.now()), HttpStatus.OK);
    }

    /**
     * Retrieves all investment products including inactive ones.
     *
     * @return ResponseEntity containing SuccessResponse with all products
     * @throws RuntimeException if database operation fails
     */
    @GetMapping("/admin/investments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<List<InvestmentProductListDto>>> getAllProducts() {
        log.info("GET /admin/investments - Admin fetching all investment products (including inactive)");

        try {

            // Fetch all products including inactive ones
            List<InvestmentProductListDto> products = investmentProductService.getAllProducts();

            log.debug("Retrieved {} total products (including inactive)", products.size());

            // Create success response
            SuccessResponse<List<InvestmentProductListDto>> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    products,
                    String.format("Successfully fetched %d investment products from database", products.size()),
                    LocalDateTime.now()
            );

            log.info("GET /admin/investments completed successfully - returned {} total products", products.size());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while fetching all products for admin", e);
            throw new RuntimeException("Failed to retrieve all investment products. Please try again later.", e);
        }
    }

    /**
     * Updates an existing investment product.
     *
     * @param id The ID of the product to update (must be positive integer)
     * @param updateDto DTO containing update data (fields to update)
     * @return ResponseEntity containing SuccessResponse with updated product details
     * @throws ResourceNotFoundException if product is not found
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if update operation fails
     */
    @PutMapping("/admin/investments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<InvestmentProductDto>> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody InvestmentProductUpdateDto updateDto) {
        log.info("PUT /admin/investments/{} - Updating investment product", id);

        // Input validation
        if (id == null || id <= 0) {
            log.warn("Invalid product ID for update: {}", id);
            throw new IllegalArgumentException("Product ID must be a positive integer");
        }


        if (updateDto == null) {
            log.warn("Null update DTO received for product ID: {}", id);
            throw new IllegalArgumentException("Product update data cannot be null");
        }

        try {

            // Update product using service layer
            InvestmentProductDto updatedProduct = investmentProductService.updateProduct(id, updateDto);

            log.debug("Product update completed for ID: {} - Name: '{}'", id, updatedProduct.getName());

            // Create success response
            SuccessResponse<InvestmentProductDto> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    updatedProduct,
                    String.format("Successfully updated investment product with id %d", id),
                    LocalDateTime.now()
            );

            log.info("PUT /admin/investments/{} completed successfully - Updated product: '{}'",
                    id, updatedProduct.getName());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            log.warn("Product not found for update with ID: {}", id);
            throw e; // Re-throw to be handled by global exception handler
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data provided for product update ID {}: {}", id, e.getMessage());
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            log.error("Error occurred while updating product with ID: {}", id, e);
            throw new RuntimeException("Failed to update investment product. Please try again later.", e);
        }
    }

    @PutMapping("/admin/investments/activeHandle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<String>> updateProductStatus(
            @PathVariable Integer id,
            @RequestBody ActiveStatusRequest activeStatusRequest) {
        System.out.println(id + " " + activeStatusRequest.getActive());
        investmentProductService.updateIsActive(id, activeStatusRequest.getActive());
        SuccessResponse<String> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                "updated",
                String.format("Successfully updated investment product with id %d", id),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Soft deletes an investment product by marking it as inactive.
     *
     * @param id The ID of the product to delete (must be positive integer)
     * @return ResponseEntity containing SuccessResponse with deletion confirmation
     * @throws ResourceNotFoundException if product is not found
     * @throws IllegalArgumentException if ID is invalid
     * @throws RuntimeException if deletion operation fails
     */
    @DeleteMapping("/admin/investments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<String>> deleteProduct(@PathVariable Integer id) {
        log.info("DELETE /admin/investments/{} - Soft deleting investment product", id);

        // Input validation
        if (id == null || id <= 0) {
            log.warn("Invalid product ID for deletion: {}", id);
            throw new IllegalArgumentException("Product ID must be a positive integer");
        }

        try {

            // Perform soft delete using service layer
            investmentProductService.deleteProduct(id);

            log.debug("Product soft deletion completed for ID: {}", id);

            // Create success response
            SuccessResponse<String> response = new SuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Deleted",
                    String.format("Successfully deleted investment product with id %d", id),
                    LocalDateTime.now()
            );

            log.info("DELETE /admin/investments/{} completed successfully - Product soft deleted", id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            log.warn("Product not found for deletion with ID: {}", id);
            throw e; // Re-throw to be handled by global exception handler
        } catch (Exception e) {
            log.error("Error occurred while deleting product with ID: {}", id, e);
            throw new RuntimeException("Failed to delete investment product. Please try again later.", e);
        }
    }
}
