package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.PortfolioService;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;
    private final JWTService jwtService;

    public PortfolioController(PortfolioService portfolioService,
                               UserService userService,
                               JWTService jwtService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<PortfolioResponseDto> getUserPortfolio(@RequestHeader("Authorization") String bearer) {
        String username = getUserName(bearer);
        log.info("Fetching portfolio for user: {}", username);
        User user = userService.getUserByEmail(username);
        PortfolioResponseDto response = portfolioService.getUserPortfolio(user);
        log.debug("Portfolio response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/buy")
    public ResponseEntity<PortfolioItemDto> buyInvestment(
            @RequestHeader("Authorization") String bearer,
            @Valid @RequestBody BuyInvestmentRequestDto request) {
        String username = getUserName(bearer);
        log.info("User {} is buying investment: {}", username, request);
        User user = userService.getUserByEmail(username);
        PortfolioItemDto result = portfolioService.buyInvestment(user, request);
        log.debug("Buy result: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sell")
    public ResponseEntity<PortfolioItemDto> sellInvestment(
            @RequestHeader("Authorization") String bearer,
            @Valid @RequestBody SellInvestmentRequestDto request) {
        String username = getUserName(bearer);
        log.info("User {} is selling investment: {}", username, request);
        User user = userService.getUserByEmail(username);
        PortfolioItemDto result = portfolioService.sellInvestment(user, request);
        log.debug("Sell result: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioItemDto> getPortfolioById(@RequestHeader("Authorization") String bearer, @PathVariable Integer id){
        String username = getUserName(bearer);
        log.info("User {} is fetching investment with ID: {}", username, id);
        User user = userService.getUserByEmail(username);
        PortfolioItemDto item = portfolioService.getInvestmentById(user, id);
        log.debug("Investment item: {}", item);
        return ResponseEntity.ok(item);
    }

    public String getUserName(String bearerToken) {
        String token = bearerToken.substring(7);
        String username = jwtService.extractUsername(token);
        log.debug("Extracted username from token: {}", username);
        return username;
    }
}
