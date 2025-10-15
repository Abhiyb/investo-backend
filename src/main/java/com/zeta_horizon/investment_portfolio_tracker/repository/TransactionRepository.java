package com.zeta_horizon.investment_portfolio_tracker.repository;

import com.zeta_horizon.investment_portfolio_tracker.entity.Transaction;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    // list all the transactions of a particular user
    List<Transaction> findByUser(User user);

    // list all the transactions of a particular user in a sorted manner of transaction date
    List<Transaction> findByUserOrderByTxnDateDesc(User user);

    // Custom query for filtered and paginated transactions
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:searchQuery IS NULL OR " +
            "     LOWER(t.investmentProduct.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "     LOWER(CAST(t.txnType AS string)) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
            "AND (:txnType IS NULL OR t.txnType = :txnType) " +
            "AND (:startDate IS NULL OR t.txnDate >= :startDate) " +
            "AND (:endDate IS NULL OR t.txnDate <= :endDate)")
    Page<Transaction> findFilteredTransactions(
            @Param("userId") UUID userId,
            @Param("searchQuery") String searchQuery,
            @Param("txnType") TransactionType txnType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
