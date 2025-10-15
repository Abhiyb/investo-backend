package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.dto.PaginatedTransactionResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionFilterDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionHistoryResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.TransactionService;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final JWTService jwtService;
    private final UserService userService;
    private final TransactionService transactionService;

    @GetMapping("/transactions")
    public ResponseEntity<TransactionHistoryResponseDto> getTransactionHistory(@RequestHeader("Authorization") String bearer) {
        String username = getUserName(bearer);
        log.info("Fetching transaction history for user: {}", username);
        User user = userService.getUserByEmail(username);
        TransactionHistoryResponseDto response = transactionService.getTransactionHistory(user);
        log.debug("Transaction history response: {}", response);
        return ResponseEntity.ok(response);
    }

    // endpoint - returns filtered and paginated transactions
    @GetMapping("/transactions/filter")
    public ResponseEntity<PaginatedTransactionResponseDto> getFilteredTransactions(
            @RequestHeader("Authorization") String bearer,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) TransactionType txnType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "txnDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        String username = getUserName(bearer);
        log.info("Fetching filtered transactions for user: {}", username);
        log.debug("Filter parameters - searchQuery: {}, txnType: {}, startDate: {}, endDate: {}, sortBy: {}, sortOrder: {}, page: {}, size: {}",
                searchQuery, txnType, startDate, endDate, sortBy, sortOrder, page, size);

        TransactionFilterDto filterDto = TransactionFilterDto.builder()
                .searchQuery(searchQuery)
                .txnType(txnType)
                .startDate(startDate)
                .endDate(endDate)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();

        User user = userService.getUserByEmail(username);
        PaginatedTransactionResponseDto response = transactionService.getFilteredTransactions(user, filterDto);
        log.debug("Filtered transaction response: {}", response);
        return ResponseEntity.ok(response);
    }

    public String getUserName(String bearerToken) {
        String token = bearerToken.substring(7);
        String username = jwtService.extractUsername(token);
        log.debug("Extracted username from token: {}", username);
        return username;
    }
}
