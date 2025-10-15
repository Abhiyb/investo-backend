package com.zeta_horizon.investment_portfolio_tracker.repositoryTest;

import com.zeta_horizon.investment_portfolio_tracker.entity.Transaction;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import com.zeta_horizon.investment_portfolio_tracker.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionRepositoryTest {

    @Mock
    private TransactionRepository transactionRepository;

    private User testUser;
    private InvestmentProduct testProduct;
    private Transaction buyTxn;
    private Transaction sellTxn;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testProduct = new InvestmentProduct();
        testProduct.setId(1);
        testProduct.setName("Equity Fund");

        buyTxn = Transaction.builder()
                .id(1)
                .user(testUser)
                .investmentProduct(testProduct)
                .txnType(TransactionType.BUY)
                .units(BigDecimal.valueOf(1000))
                .txnDate(LocalDateTime.now().minusDays(2))
                .build();

        sellTxn = Transaction.builder()
                .id(2)
                .user(testUser)
                .investmentProduct(testProduct)
                .txnType(TransactionType.SELL)
                .units(BigDecimal.valueOf(500))
                .txnDate(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void testFindByUser() {
        when(transactionRepository.findByUser(testUser)).thenReturn(List.of(buyTxn, sellTxn));

        List<Transaction> result = transactionRepository.findByUser(testUser);

        assertEquals(2, result.size());
        assertEquals(testUser, result.get(0).getUser());
        verify(transactionRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testFindByUserOrderByTxnDateDesc() {
        when(transactionRepository.findByUserOrderByTxnDateDesc(testUser)).thenReturn(List.of(sellTxn, buyTxn));

        List<Transaction> result = transactionRepository.findByUserOrderByTxnDateDesc(testUser);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getTxnDate().isAfter(result.get(1).getTxnDate()));
        verify(transactionRepository, times(1)).findByUserOrderByTxnDateDesc(testUser);
    }

    @Test
    void testFindFilteredTransactions() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("txnDate").descending());
        Page<Transaction> expectedPage = new PageImpl<>(List.of(sellTxn));

        when(transactionRepository.findFilteredTransactions(
                eq(testUser.getId()),
                eq("SELL"),
                eq(TransactionType.SELL),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)
        )).thenReturn(expectedPage);

        Page<Transaction> result = transactionRepository.findFilteredTransactions(
                testUser.getId(),
                "SELL",
                TransactionType.SELL,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now(),
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TransactionType.SELL, result.getContent().get(0).getTxnType());
        verify(transactionRepository, times(1)).findFilteredTransactions(
                eq(testUser.getId()),
                eq("SELL"),
                eq(TransactionType.SELL),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)
        );
    }

}
