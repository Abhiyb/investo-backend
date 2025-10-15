package com.zeta_horizon.investment_portfolio_tracker.repository;

import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.entity.Portfolio;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {

   // list all the portfolios of a particular user
   List<Portfolio> findByUser(User user);

   Optional<Portfolio> findByUserAndInvestmentProduct(User user, InvestmentProduct investmentProduct);


}
