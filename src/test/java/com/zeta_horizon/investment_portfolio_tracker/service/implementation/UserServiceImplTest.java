package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUser_encodesPasswordAndSavesUser() {
        User user = new User();
        user.setPasswordHash("rawPassword");

        // Mock password encoding
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        // Mock repository save to return the same user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.saveUser(user);

        // Password should be encoded
        assertEquals("encodedPassword", savedUser.getPasswordHash());
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(savedUser);
    }

    @Test
    void getAllUsers_returnsUserList() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserByEmail_returnsUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User foundUser = userService.getUserByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void verify_authenticatesAndGeneratesToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("password");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("token123");

        String token = userService.verify(user);

        assertEquals("token123", token);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    void verify_authenticationFails_returnsFailure() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("wrongPassword");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        String result = userService.verify(user);

        assertEquals("failure", result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }
}
