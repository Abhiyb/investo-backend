package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.dto.PaginatedTransactionResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionFilterDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TransactionHistoryResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.Transaction;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.repository.TransactionRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.UserRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Retrieves paginated and filtered transaction history for the given user.
     */
    @Override
    public PaginatedTransactionResponseDto getFilteredTransactions(
            User user,
            TransactionFilterDto filterDto) {

        log.info("Fetching filtered transactions for user: {}", user.getEmail());

        // Default pagination and sorting
        int page = filterDto.getPage() != null ? filterDto.getPage() : 0;
        int size = filterDto.getSize() != null ? filterDto.getSize() : 10;
        String sortBy = filterDto.getSortBy() != null ? filterDto.getSortBy() : "txnDate";
        String sortOrder = filterDto.getSortOrder() != null ? filterDto.getSortOrder() : "desc";

        // Determine sort direction
        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch filtered transactions
        Page<Transaction> transactionPage = transactionRepository.findFilteredTransactions(
                user.getId(),
                filterDto.getSearchQuery(),
                filterDto.getTxnType(),
                filterDto.getStartDate(),
                filterDto.getEndDate(),
                pageable
        );

        // Convert entity list to DTO list
        List<TransactionDto> transactionDtos = transactionPage.getContent().stream()
                .map(this::mapToTransactionDto)
                .collect(Collectors.toList());

        log.debug("Found {} transactions for user: {}", transactionDtos.size(), user.getEmail());

        // Build paginated response
        return PaginatedTransactionResponseDto.builder()
                .transactions(transactionDtos)
                .currentPage(transactionPage.getNumber())
                .totalPages(transactionPage.getTotalPages())
                .totalElements(transactionPage.getTotalElements())
                .pageSize(transactionPage.getSize())
                .hasNext(transactionPage.hasNext())
                .hasPrevious(transactionPage.hasPrevious())
                .build();
    }

    /**
     * Returns full transaction history (non-paginated) for the user, sorted by date descending.
     */
    @Override
    public TransactionHistoryResponseDto getTransactionHistory(User user) {
        log.info("Fetching full transaction history for user: {}", user.getEmail());

        // Fetch all transactions
        List<Transaction> transactions = transactionRepository.findByUserOrderByTxnDateDesc(user);

        // Map to DTOs
        List<TransactionDto> transactionDTOs = transactions.stream()
                .map(txn -> TransactionDto.builder()
                        .id(txn.getId())
                        .investmentProductName(txn.getInvestmentProduct().getName())
                        .txnType(txn.getTxnType().toString())
                        .units(txn.getUnits())
                        .navAtTxn(txn.getNavAtTxn())
                        .amount(txn.getUnits().multiply(txn.getNavAtTxn()))
                        .txnDate(txn.getTxnDate().format(DATE_TIME_FORMATTER))
                        .build())
                .toList();

        log.debug("Total {} transactions found for user: {}", transactionDTOs.size(), user.getEmail());

        return TransactionHistoryResponseDto.builder()
                .transactions(transactionDTOs)
                .build();
    }

    /**
     * Converts a Transaction entity into a TransactionDto.
     */
    private TransactionDto mapToTransactionDto(Transaction transaction) {
        BigDecimal amount = transaction.getUnits().multiply(transaction.getNavAtTxn());

        return TransactionDto.builder()
                .id(transaction.getId())
                .investmentProductName(transaction.getInvestmentProduct().getName())
                .txnType(transaction.getTxnType().name())
                .units(transaction.getUnits())
                .navAtTxn(transaction.getNavAtTxn())
                .amount(amount)
                .txnDate(transaction.getTxnDate().format(DATE_TIME_FORMATTER))
                .build();
    }
}
