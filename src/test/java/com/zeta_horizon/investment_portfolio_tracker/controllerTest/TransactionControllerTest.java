package com.zeta_horizon.investment_portfolio_tracker.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta_horizon.investment_portfolio_tracker.controller.TransactionController;
import com.zeta_horizon.investment_portfolio_tracker.dto.PaginatedTransactionResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionFilterDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionHistoryResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.TransactionService;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    private TransactionController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String BEARER_TOKEN = "Bearer dummy.jwt.token";
    private static final String JWT_TOKEN = "dummy.jwt.token";
    private static final String USERNAME = "testuser@example.com";

    @BeforeEach
    void setup() {
        controller = new TransactionController(jwtService, userService, transactionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        log.info("Test setup complete.");
    }

    @Test
    void testGetTransactionHistorySuccess() throws Exception {
        User mockUser = new User();
        mockUser.setEmail(USERNAME);

        TransactionDto transactionDto = TransactionDto.builder()
                .id(1)
                .investmentProductName("Axis Bluechip Fund")
                .txnType("BUY")
                .units(BigDecimal.valueOf(10.5))
                .navAtTxn(BigDecimal.valueOf(43.21))
                .amount(BigDecimal.valueOf(453.71))
                .txnDate("2025-05-20T10:00:00")
                .build();

        TransactionHistoryResponseDto mockResponse = TransactionHistoryResponseDto.builder()
                .transactions(Collections.singletonList(transactionDto))
                .build();

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(transactionService.getTransactionHistory(mockUser)).thenReturn(mockResponse);

        mockMvc.perform(get("/portfolio/transactions")
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value(1))
                .andExpect(jsonPath("$.transactions[0].txnType").value("BUY"))
                .andExpect(jsonPath("$.transactions[0].amount").value(453.71))
                .andExpect(jsonPath("$.transactions[0].investmentProductName").value("Axis Bluechip Fund"));

        verify(jwtService).extractUsername(JWT_TOKEN);
        verify(userService).getUserByEmail(USERNAME);
        verify(transactionService).getTransactionHistory(mockUser);
    }



    @Test
    void testGetFilteredTransactionsSuccess() throws Exception {
        User mockUser = new User();
        mockUser.setEmail(USERNAME);

        PaginatedTransactionResponseDto mockPaginatedResponse = PaginatedTransactionResponseDto.builder()
                .transactions(Collections.singletonList(
                        TransactionDto.builder()
                                .id(101)
                                .investmentProductName("SBI Equity Fund")
                                .txnType("SELL")
                                .units(new BigDecimal("25.0"))
                                .navAtTxn(new BigDecimal("52.10"))
                                .amount(new BigDecimal("1302.5"))
                                .txnDate("2025-05-15T12:00:00")
                                .build()
                ))
                .currentPage(1)
                .totalPages(2)
                .totalElements(10L)
                .pageSize(5)
                .hasNext(true)
                .hasPrevious(false)
                .build();


        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(transactionService.getFilteredTransactions(any(User.class), any(TransactionFilterDto.class)))
                .thenReturn(mockPaginatedResponse);

        mockMvc.perform(get("/portfolio/transactions/filter")
                        .header("Authorization", BEARER_TOKEN)
                        .param("searchQuery", "investment")
                        .param("txnType", "BUY")
                        .param("startDate", "2025-05-01T00:00:00")
                        .param("endDate", "2025-05-10T23:59:59")
                        .param("sortBy", "txnDate")
                        .param("sortOrder", "asc")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        ArgumentCaptor<TransactionFilterDto> filterCaptor = ArgumentCaptor.forClass(TransactionFilterDto.class);
        verify(transactionService).getFilteredTransactions(eq(mockUser), filterCaptor.capture());

        TransactionFilterDto capturedFilter = filterCaptor.getValue();
        assertEquals("investment", capturedFilter.getSearchQuery());
        assertEquals(TransactionType.BUY, capturedFilter.getTxnType());
        assertEquals(LocalDateTime.parse("2025-05-01T00:00:00"), capturedFilter.getStartDate());
        assertEquals(LocalDateTime.parse("2025-05-10T23:59:59"), capturedFilter.getEndDate());
        assertEquals("txnDate", capturedFilter.getSortBy());
        assertEquals("asc", capturedFilter.getSortOrder());
        assertEquals(1, capturedFilter.getPage());
        assertEquals(5, capturedFilter.getSize());

        verify(jwtService).extractUsername(JWT_TOKEN);
        verify(userService).getUserByEmail(USERNAME);
    }



}
