package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.exception.UserAlreadyExistsException;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @GetMapping("/user/profile")
    public User getUserProfile(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7);
        return userService.getUserByEmail(jwtService.extractUsername(token));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.verify(user),HttpStatus.OK);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
}
