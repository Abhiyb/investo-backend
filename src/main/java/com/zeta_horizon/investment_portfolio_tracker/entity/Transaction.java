package com.zeta_horizon.investment_portfolio_tracker.entity;

import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "investment_product_id", nullable = false)
    private InvestmentProduct investmentProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private TransactionType txnType;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal units;

    @Column(name = "nav_at_txn", nullable = false, precision = 10, scale = 2)
    private BigDecimal navAtTxn;

    @Column(name = "txn_date", nullable = false)
    private LocalDateTime txnDate;


}
