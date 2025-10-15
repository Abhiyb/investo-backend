package com.zeta_horizon.investment_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionDto {
    private Integer id;
    private String investmentProductName;
    private String txnType;
    private BigDecimal units;
    private BigDecimal navAtTxn;
    private BigDecimal amount;
    private String txnDate;

}
