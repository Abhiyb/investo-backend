package com.zeta_horizon.investment_portfolio_tracker.service;

import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {

    public User saveUser(User user);

    public List<User> getAllUsers();

    public User getUserByEmail(String email);

    String verify(User user);
}
