package com.zeta_horizon.investment_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {

   @Id
   @GeneratedValue
   private Integer id;

   @ManyToOne
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

   @ManyToOne
   @JoinColumn(name = "investment_product_id", nullable = false)
   private InvestmentProduct investmentProduct;

   @Column(precision = 10, scale = 2)
   private BigDecimal unitsOwned;

   @Column(precision = 10, scale = 4)
   private BigDecimal avgPurchasePrice;

}
