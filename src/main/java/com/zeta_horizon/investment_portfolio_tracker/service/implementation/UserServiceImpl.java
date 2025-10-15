package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.exception.UserAlreadyExistsException;
import com.zeta_horizon.investment_portfolio_tracker.repository.UserRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTServiceImpl JWTService;

    @Override
    public User saveUser(User user){
        if (userRepository.findByEmail(user.getEmail())!=null) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String verify(User user) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPasswordHash()));

        if(authentication.isAuthenticated())
            return JWTService.generateToken(getUserByEmail(user.getEmail()));

        return "failure";
    }


}
