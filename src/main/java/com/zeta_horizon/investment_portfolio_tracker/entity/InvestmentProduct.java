package com.zeta_horizon.investment_portfolio_tracker.entity;

import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing an Investment Product in the portfolio tracking system.
 * This class maps to the investment_product table in the database and contains
 * all the necessary information about various investment instruments.
 *
 * Features:
 * - Supports different investment types (STOCKS, BONDS, MUTUAL_FUNDS, etc.)
 * - Risk level categorization for better investment decisions
 * - Minimum investment amount tracking
 * - Expected return rate calculations
 * - Current NAV (Net Asset Value) per unit
 * - Soft delete functionality with isActive flag
 * - Automatic timestamp tracking for audit purposes
 *
 */
@Slf4j
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProduct {

    /**
     * Primary key for the investment product.
     * Auto-generated using IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Name of the investment product.
     * Cannot be null and has a maximum length of 100 characters.
     * Examples: "Apple Inc. Stock", "Government Bond 2024", "Tech Mutual Fund"
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of investment product (enum).
     * Stored as string in database for better readability.
     * Possible values: FIXED_DEPOSIT, GOVERNMENT_BOND, PUBLIC_PROVIDENT_FUND,  MUTUAL_FUND,
     *     CORPORATE_BOND, REAL_ESTATE_INVESTMENT_TRUST, STOCK, CRYPTOCURRENCY, OPTIONS
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentType type;

    /**
     * Risk level associated with this investment product.
     * Helps investors make informed decisions based on their risk tolerance.
     * Possible values: LOW, MEDIUM, HIGH
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    /**
     * Minimum amount required to invest in this product.
     * Stored with precision of 12 digits and 2 decimal places.
     * Used to filter products based on investor's available capital.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumInvestment;

    /**
     * Expected annual return rate as a percentage (e.g., 8.50 for 8.5%).
     * Precision of 5 digits with 2 decimal places allows for rates like 999.99%.
     * Used for investment projections and comparisons.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal expectedAnnualReturnRate;

    /**
     * Current Net Asset Value (NAV) per unit of the investment.
     * For stocks, this represents the current share price.
     * For mutual funds, this is the NAV per unit.
     * Updated regularly based on market conditions.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentNetAssetValuePerUnit;

    /**
     * Detailed description of the investment product.
     * Optional field that can contain investment strategy, objectives, or other details.
     * Maximum length of 500 characters.
     */
    @Column(length = 500)
    private String description;

    /**
     * Soft delete flag to maintain data integrity.
     * When set to false, the product is considered deleted but data is preserved.
     * Default value is true for new products.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Timestamp when the record was created.
     * Automatically set by Hibernate when the entity is first persisted.
     * Cannot be updated once set.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     * Automatically updated by Hibernate whenever the entity is modified.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Pre-persist callback to log entity creation.
     * Called before the entity is saved to the database for the first time.
     */
    @PrePersist
    protected void onCreate() {
        log.debug("Creating new InvestmentProduct entity with name: {}, type: {}, risk level: {}",
                name, type, riskLevel);
    }

    /**
     * Pre-update callback to log entity updates.
     * Called before the entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating InvestmentProduct entity with id: {}, name: {}", id, name);
    }

    /**
     * Post-load callback to log entity retrieval.
     * Called after the entity is loaded from the database.
     */
    @PostLoad
    protected void onLoad() {
        log.trace("Loaded InvestmentProduct entity with id: {}, name: {}, active: {}",
                id, name, isActive);
    }

    /**
     * Custom toString method for better logging and debugging.
     * Excludes sensitive or large fields to keep logs readable.
     */
    @Override
    public String toString() {
        return "InvestmentProduct{ " +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", riskLevel=" + riskLevel +
                ", minimumInvestment=" + minimumInvestment +
                ", isActive=" + isActive +
                '}';
    }
}
