package com.zeta_horizon.investment_portfolio_tracker.serviceTest;


import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.InvestmentType;
import com.zeta_horizon.investment_portfolio_tracker.enums.RiskLevel;
import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import com.zeta_horizon.investment_portfolio_tracker.repository.TransactionRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.UserRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.implementation.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private InvestmentProduct investmentProduct;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        investmentProduct = InvestmentProduct.builder()
                .id(1)
                .name("Test Product")
                .minimumInvestment(new BigDecimal("1000.00"))
                .expectedAnnualReturnRate(new BigDecimal("7.50"))
                .currentNetAssetValuePerUnit(new BigDecimal("105.00"))
                .type(InvestmentType.MUTUAL_FUND)
                .riskLevel(RiskLevel.MEDIUM)
                .isActive(true)
                .build();

        transaction = Transaction.builder()
                .id(1)
                .user(user)
                .investmentProduct(investmentProduct)
                .txnType(TransactionType.BUY)
                .units(new BigDecimal("10"))
                .navAtTxn(new BigDecimal("100.00"))
                .txnDate(LocalDateTime.of(2024, 5, 1, 10, 30))
                .build();
    }

    @Test
    void getFilteredTransactionsShouldReturnPaginatedResponse() {
        TransactionFilterDto filterDto = TransactionFilterDto.builder()
                .page(0)
                .size(5)
                .sortBy("txnDate")
                .sortOrder("desc")
                .build();

        Pageable pageable = PageRequest.of(0, 5, Sort.by("txnDate").descending());
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findFilteredTransactions(
                eq(user.getId()),
                any(),
                any(),
                any(),
                any(),
                eq(pageable)))
                .thenReturn(transactionPage);

        PaginatedTransactionResponseDto response = transactionService.getFilteredTransactions(user, filterDto);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
        assertEquals(transaction.getId(), response.getTransactions().get(0).getId());
        assertEquals("Test Product", response.getTransactions().get(0).getInvestmentProductName());
        assertEquals(new BigDecimal("1000.00"), response.getTransactions().get(0).getAmount());
    }

    @Test
    void getTransactionHistoryShouldReturnTransactionList() {
        when(transactionRepository.findByUserOrderByTxnDateDesc(user))
                .thenReturn(List.of(transaction));

        TransactionHistoryResponseDto response = transactionService.getTransactionHistory(user);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());

        TransactionDto dto = response.getTransactions().get(0);
        assertEquals(transaction.getId(), dto.getId());
        assertEquals("Test Product", dto.getInvestmentProductName());
        assertEquals(new BigDecimal("1000.00"), dto.getAmount());
        assertEquals("BUY", dto.getTxnType());
    }
}
