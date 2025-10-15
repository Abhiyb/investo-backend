package com.zeta_horizon.investment_portfolio_tracker.repository;

import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.zeta_horizon.investment_portfolio_tracker.entity")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPasswordHash("password123");
        user.setPhone("1234567890");
        user.setRole(UserRole.USER);
        user.setCreatedAt(Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("Test 1: Save User")
    @Order(1)
    public void saveUserTest() {
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Test 3: Find User by ID")
    @Order(3)
    public void findUserByIdTest() {
        User savedUser = userRepository.save(user);
        Optional<User> found = userRepository.findById(savedUser.getId());
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    @DisplayName("Test 6: Update User")
    @Order(6)
    public void updateUserTest() {
        User savedUser = userRepository.save(user);
        savedUser.setName("Jane Smith");
        savedUser.setEmail("jane.smith@example.com");
        savedUser.setRole(UserRole.ADMIN);
        User updated = userRepository.save(savedUser);
        assertEquals("Jane Smith", updated.getName());
        assertEquals(UserRole.ADMIN, updated.getRole());
    }

    @Test
    @DisplayName("Test 7: Delete User")
    @Order(7)
    public void deleteUserTest() {
        User savedUser = userRepository.save(user);
        UUID id = savedUser.getId();
        userRepository.delete(savedUser);
        assertFalse(userRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("Test 8: Delete User by ID")
    @Order(8)
    public void deleteUserByIdTest() {
        User savedUser = userRepository.save(user);
        UUID id = savedUser.getId();
        userRepository.deleteById(id);
        assertTrue(userRepository.findById(id).isEmpty());
    }
}
