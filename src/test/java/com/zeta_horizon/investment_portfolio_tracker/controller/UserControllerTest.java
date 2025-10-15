package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.UserRole;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String token;
    private String bearerToken;

    @BeforeEach
    void setUp() {
        token = "mock.jwt.token";
        bearerToken = "Bearer " + token;

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");
        user.setPhone("1234567890");
        user.setRole(UserRole.USER);
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Register user successfully")
    void testRegisterUserSuccessfully() throws Exception {
        when(userService.saveUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    @DisplayName("Login user successfully")
    void testLoginUserSuccessfully() throws Exception {
        String mockJwt = "mock.jwt.token";
        when(userService.verify(any(User.class))).thenReturn(mockJwt);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(mockJwt));
    }

    @Test
    @DisplayName("Get all users (admin only)")
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setName("Jane Doe");
        anotherUser.setEmail("jane@example.com");
        anotherUser.setPasswordHash("secure");
        anotherUser.setPhone("9876543210");
        anotherUser.setRole(UserRole.ADMIN);
        anotherUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        when(userService.getAllUsers()).thenReturn(List.of(user, anotherUser));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));
    }

}
