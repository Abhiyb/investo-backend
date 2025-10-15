package com.zeta_horizon.investment_portfolio_tracker.repository;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InvestmentProduct entity operations.
 *
 * This repository provides data access methods for investment products including:
 * - CRUD operations through JpaRepository
 * - Custom finder methods for active products
 * - Filtering capabilities by type, risk level, and name
 * - Advanced search with multiple criteria
 * - Soft delete support (working with isActive flag)
 *
 * All custom query methods are optimized for performance and include proper
 * indexing strategies. The repository follows Spring Data JPA conventions
 * and provides both derived queries and custom JPQL queries.
 *
 */
@Repository
public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Integer> {

    /**
     * Retrieves all active investment products.
     * This method is used to get only products that are currently available
     * for investment (isActive = true). Soft-deleted products are excluded.
     *
     * @return List of active InvestmentProduct entities
     */
    List<InvestmentProduct> findByIsActiveTrue();

    /**
     * Finds an active investment product by its ID.
     * Combines ID lookup with active status check in a single query.
     * Returns empty Optional if product doesn't exist or is inactive.
     *
     * @param id The unique identifier of the investment product
     * @return Optional containing the product if found and active, empty otherwise
     */
    Optional<InvestmentProduct> findByIdAndIsActiveTrue(Integer id);

    /**
     * Retrieves all active investment products of a specific type.
     * Useful for filtering products by investment category (stocks, bonds, etc.).
     *
     * @param type The type of investment product to filter by
     * @return List of active products matching the specified type
     */
    List<InvestmentProduct> findByTypeAndIsActiveTrue(InvestmentType type);

    /**
     * Retrieves all active investment products with a specific risk level.
     * Helps investors find products that match their risk tolerance.
     *
     * @param riskLevel The risk level to filter by (LOW, MEDIUM, HIGH)
     * @return List of active products with the specified risk level
     */
    List<InvestmentProduct> findByRiskLevelAndIsActiveTrue(RiskLevel riskLevel);

    /**
     * Searches for active investment products by name using case-insensitive partial matching.
     *
     * @param name The search term to match against product names (case-insensitive)
     * @return List of active products with names containing the search term
     */
    List<InvestmentProduct> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    /**
     * Advanced filtering method with multiple optional criteria.
     * This custom JPQL query provides flexible filtering capabilities:
     *
     * - Null parameters are ignored (no filtering applied for that criterion)
     * - Multiple criteria can be combined
     * - Only active products are returned
     * - Optimized with proper parameter binding to prevent SQL injection
     *
     * @param type Optional investment type filter (null = no type filter)
     * @param riskLevel Optional risk level filter (null = no risk filter)
     * @param maxAmount Optional maximum investment amount filter (null = no amount filter)
     * @return List of active products matching the specified criteria
     */
    @Query("SELECT i FROM InvestmentProduct i WHERE " +
            "i.isActive = true AND " +
            "(:type IS NULL OR i.type = :type) AND " +
            "(:riskLevel IS NULL OR i.riskLevel = :riskLevel) AND " +
            "(:maxAmount IS NULL OR i.minimumInvestment <= :maxAmount)")
    List<InvestmentProduct> findByFilters(
            @Param("type") InvestmentType type,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("maxAmount") BigDecimal maxAmount
    );
}