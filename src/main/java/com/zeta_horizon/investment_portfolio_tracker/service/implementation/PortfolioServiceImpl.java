package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.entity.Portfolio;
import com.zeta_horizon.investment_portfolio_tracker.entity.Transaction;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.TransactionType;
import com.zeta_horizon.investment_portfolio_tracker.exception.InsufficientUnitsException;
import com.zeta_horizon.investment_portfolio_tracker.exception.InvalidInvestmentException;
import com.zeta_horizon.investment_portfolio_tracker.exception.MinimumInvestmentException;
import com.zeta_horizon.investment_portfolio_tracker.repository.InvestmentProductRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.PortfolioRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.TransactionRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.PortfolioService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final InvestmentProductRepository investmentProductRepository;
    private final TransactionRepository transactionRepository;

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                InvestmentProductRepository investmentProductRepository,
                                TransactionRepository transactionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.investmentProductRepository = investmentProductRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public PortfolioResponseDto getUserPortfolio(User user) {
        log.info("Fetching portfolio for user: {}", user.getEmail());
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);

        List<PortfolioItemDto> holdings = portfolios.stream()
                .map(this :: mapToPortfolioItemDto)
                .toList();
        BigDecimal totalInvestedValue = holdings.stream()
                .map(PortfolioItemDto::getInvestedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrentValue = holdings.stream()
                .map(PortfolioItemDto::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("User: {} | Total Invested: {}, Total Current: {}", user.getEmail(), totalInvestedValue, totalCurrentValue);

        return PortfolioResponseDto.builder()
                .holdings(holdings)
                .totalInvestedValue(totalInvestedValue)
                .totalCurrentValue(totalCurrentValue)
                .build();
    }

    @Transactional
    @Override
    public PortfolioItemDto buyInvestment(User user, BuyInvestmentRequestDto request) {
        log.info("User {} is attempting to buy product ID: {}", user.getEmail(), request.getInvestmentProductId());
        InvestmentProduct product = investmentProductRepository.findById(request.getInvestmentProductId())
                .orElseThrow(() -> new InvalidInvestmentException("Investment product not found"));
        if (!product.isActive()) {
            log.warn("Product ID {} is inactive", product.getId());
            throw new InvalidInvestmentException("Investment product is not active");
        }

        BigDecimal investmentAmount  = request.getUnits().multiply(product.getCurrentNetAssetValuePerUnit());
        log.debug("Investment amount calculated: {}", investmentAmount);

        if(investmentAmount.compareTo(product.getMinimumInvestment()) < 0){
            log.warn("Investment below minimum. Required: {}, Provided: {}", product.getMinimumInvestment(), investmentAmount);
            throw new MinimumInvestmentException("Minimum investment required: " + product.getMinimumInvestment());
        }

        // Find or create portfolio entry
        Portfolio portfolio = portfolioRepository.findByUserAndInvestmentProduct(user, product)
                .orElse(new Portfolio(null, user, product, BigDecimal.ZERO, BigDecimal.ZERO));

        // Update average purchase price
        if (portfolio.getUnitsOwned().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalOldValue = portfolio.getUnitsOwned().multiply(portfolio.getAvgPurchasePrice());
            BigDecimal totalNewValue = request.getUnits().multiply(product.getCurrentNetAssetValuePerUnit());
            BigDecimal totalUnits = portfolio.getUnitsOwned().add(request.getUnits());

            BigDecimal newAvgPrice = totalOldValue.add(totalNewValue).divide(totalUnits, 2, RoundingMode.HALF_UP);
            portfolio.setAvgPurchasePrice(newAvgPrice);
            log.debug("Updated avg purchase price: {}", newAvgPrice);
        } else {
            portfolio.setAvgPurchasePrice(product.getCurrentNetAssetValuePerUnit());
            log.debug("Initial avg purchase price set: {}", product.getCurrentNetAssetValuePerUnit());
        }

        // set units in portfolio
        portfolio.setUnitsOwned(portfolio.getUnitsOwned().add(request.getUnits()));
        log.debug("Updated units owned: {}", portfolio.getUnitsOwned());

        //save the portfolio
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("Portfolio updated for user: {}, product ID: {}", user.getEmail(), product.getId());

        // reduce units in investmentProduct

        // Record transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .txnType(TransactionType.BUY)
                .investmentProduct(product)
                .units(request.getUnits())
                .navAtTxn(product.getCurrentNetAssetValuePerUnit())
                .txnDate(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("BUY transaction recorded: User={}, ProductID={}, Units={}", user.getEmail(), product.getId(), request.getUnits());

        return mapToPortfolioItemDto(savedPortfolio);
    }

    @Override
    @Transactional
    public PortfolioItemDto sellInvestment(User user, SellInvestmentRequestDto request) {
        log.info("User {} is attempting to sell product ID: {}", user.getEmail(), request.getInvestmentProductId());
        InvestmentProduct product = investmentProductRepository.findById(request.getInvestmentProductId())
                .orElseThrow(() -> new InvalidInvestmentException("Investment product not found"));

        Portfolio portfolio = portfolioRepository.findByUserAndInvestmentProduct(user, product)
                .orElseThrow(() -> new InvalidInvestmentException("You don't have this investment in your portfolio"));

        // Check if user has enough units
        if (portfolio.getUnitsOwned().compareTo(request.getUnits()) < 0) {
            log.warn("Sell failed: User={} | Requested={}, Available={}", user.getEmail(), request.getUnits(), portfolio.getUnitsOwned());
            throw new InsufficientUnitsException("Not enough units to sell. Available: " + portfolio.getUnitsOwned());
        }

        // Subtract units
        portfolio.setUnitsOwned(portfolio.getUnitsOwned().subtract(request.getUnits()));
        log.debug("Units after selling: {}", portfolio.getUnitsOwned());

        // if units is 0 then delete that investment
        if (portfolio.getUnitsOwned().compareTo(BigDecimal.ZERO) == 0) {
            log.info("All units sold. Deleting portfolio item with ID: {}", portfolio.getId());
            portfolioRepository.deleteById(portfolio.getId());
        }
        else {
            // Average purchase price remains the same
            // Save portfolio
            Portfolio savedPortfolio = portfolioRepository.save(portfolio);
            log.info("Portfolio updated after partial sell: {}", savedPortfolio.getId());
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .investmentProduct(product)
                .txnType(TransactionType.SELL)
                .units(request.getUnits())
                .navAtTxn(product.getCurrentNetAssetValuePerUnit())
                .txnDate(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("SELL transaction recorded: User={}, ProductID={}, Units={}", user.getEmail(), product.getId(), request.getUnits());

        return mapToPortfolioItemDto(portfolio);
    }

    @Override
    public PortfolioItemDto getInvestmentById(User user, Integer id) {
        log.info("Fetching investment by ID: {} for user {}", id, user.getEmail());

        Portfolio product = portfolioRepository.findById(id).get();

        return mapToPortfolioItemDto(product);
    }

    private PortfolioItemDto mapToPortfolioItemDto(Portfolio portfolio){
        InvestmentProduct product = portfolio.getInvestmentProduct();
        BigDecimal investedValue = portfolio.getUnitsOwned().multiply(portfolio.getAvgPurchasePrice()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentValue = portfolio.getUnitsOwned().multiply(product.getCurrentNetAssetValuePerUnit()).setScale(2,RoundingMode.HALF_UP);
        BigDecimal absoluteReturn = currentValue.subtract(investedValue);
        BigDecimal percentageReturn = investedValue.compareTo(BigDecimal.ZERO) > 0
                ? absoluteReturn.multiply(new BigDecimal("100")).divide(investedValue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.debug("Mapped portfolio item: ID={}, Invested={}, Current={}, Return={}",
                portfolio.getId(), investedValue, currentValue, percentageReturn);

        return PortfolioItemDto.builder()
                .id(portfolio.getId())
                .investmentProductId(product.getId())
                .investmentProductName(product.getName())
                .type(product.getType().toString())
                .riskLevel(product.getRiskLevel().toString())
                .unitsOwned(portfolio.getUnitsOwned())
                .avgPurchasePrice(portfolio.getAvgPurchasePrice())
                .currentNAV(product.getCurrentNetAssetValuePerUnit())
                .investedValue(investedValue)
                .currentValue(currentValue)
                .absoluteReturn(absoluteReturn)
                .percentageReturn(percentageReturn)
                .build();
    }
}
