package com.zeta_horizon.investment_portfolio_tracker.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta_horizon.investment_portfolio_tracker.controller.PortfolioController;
import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.UserRole;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.PortfolioService;
import com.zeta_horizon.investment_portfolio_tracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PortfolioControllerTest.class);


    @Mock
    private PortfolioService portfolioService;

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    private PortfolioController portfolioController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String USERNAME = "user@example.com";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

    @BeforeEach
    void setUp() {
        log.debug("Setting up PortfolioControllerTest");
        portfolioController = new PortfolioController(portfolioService, userService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetUserPortfolioSuccess() throws Exception {
        log.debug("Running testGetUserPortfolioSuccess");
        User mockUser = createMockUser();
        PortfolioResponseDto mockPortfolio = createMockPortfolioResponse();

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(portfolioService.getUserPortfolio(mockUser)).thenReturn(mockPortfolio);

        mockMvc.perform(get("/portfolio")
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalInvestedValue").value(60000.00))
                .andExpect(jsonPath("$.totalCurrentValue").value(30000.00))
                .andExpect(jsonPath("$.holdings").isArray())
                .andExpect(jsonPath("$.holdings[0].investmentProductName").value("Gold mine"))
                .andExpect(jsonPath("$.holdings[0].type").value("STOCK"))
                .andExpect(jsonPath("$.holdings[0].riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.holdings[0].unitsOwned").value(200.00))
                .andExpect(jsonPath("$.holdings[0].avgPurchasePrice").value(300.00))
                .andExpect(jsonPath("$.holdings[0].investedValue").value(60000.00))
                .andExpect(jsonPath("$.holdings[0].currentValue").value(30000.00))
                .andExpect(jsonPath("$.holdings[0].absoluteReturn").value(-30000.00))
                .andExpect(jsonPath("$.holdings[0].percentageReturn").value(-50.00));

        verify(jwtService, times(1)).extractUsername(JWT_TOKEN);
        verify(userService, times(1)).getUserByEmail(USERNAME);
        verify(portfolioService, times(1)).getUserPortfolio(mockUser);
        log.debug("testGetUserPortfolioSuccess passed");
    }

    @Test
    void testBuyInvestmentSuccess() throws Exception {
        log.debug("Running testBuyInvestmentSuccess");
        User mockUser = createMockUser();
        BuyInvestmentRequestDto buyRequest = BuyInvestmentRequestDto.builder()
                .investmentProductId(3)
                .units(BigDecimal.valueOf(5))
                .build();


        PortfolioItemDto mockPortfolioItem = createMockPortfolioItem();

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(portfolioService.buyInvestment(eq(mockUser), any(BuyInvestmentRequestDto.class)))
                .thenReturn(mockPortfolioItem);

        mockMvc.perform(post("/portfolio/buy")
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.investmentProductName").value("Gold mine"))
                .andExpect(jsonPath("$.unitsOwned").value(200.00))
                .andExpect(jsonPath("$.currentValue").value(30000.00));

        verify(jwtService, times(1)).extractUsername(JWT_TOKEN);
        verify(userService, times(1)).getUserByEmail(USERNAME);
        verify(portfolioService, times(1)).buyInvestment(eq(mockUser), any(BuyInvestmentRequestDto.class));
        log.debug("testBuyInvestmentSuccess passed");
    }

    @Test
    void testSellInvestmentSuccess() throws Exception {
        log.debug("Running testSellInvestmentSuccess");
        User mockUser = createMockUser();

        SellInvestmentRequestDto sellRequest = SellInvestmentRequestDto.builder()
                .investmentProductId(3)
                .units(BigDecimal.valueOf(3))
                .build();

        PortfolioItemDto mockPortfolioItem = createMockPortfolioItem();
        mockPortfolioItem.setUnitsOwned(BigDecimal.valueOf(197)); // 200 - 3 units

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(portfolioService.sellInvestment(eq(mockUser), any(SellInvestmentRequestDto.class)))
                .thenReturn(mockPortfolioItem);

        mockMvc.perform(post("/portfolio/sell")
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.investmentProductName").value("Gold mine"))
                .andExpect(jsonPath("$.unitsOwned").value(197.00));

        verify(jwtService, times(1)).extractUsername(JWT_TOKEN);
        verify(userService, times(1)).getUserByEmail(USERNAME);
        verify(portfolioService, times(1)).sellInvestment(eq(mockUser), any(SellInvestmentRequestDto.class));
        log.debug("testSellInvestmentSuccess passed");
    }

    @Test
    void testGetPortfolioByIdSuccess() throws Exception {
        log.debug("Running testGetPortfolioByIdSuccess");
        User mockUser = createMockUser();
        Integer portfolioId = 1;
        PortfolioItemDto mockPortfolioItem = createMockPortfolioItem();

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(portfolioService.getInvestmentById(mockUser, portfolioId)).thenReturn(mockPortfolioItem);

        mockMvc.perform(get("/portfolio/{id}", portfolioId)
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.investmentProductName").value("Gold mine"))
                .andExpect(jsonPath("$.unitsOwned").value(200.00))
                .andExpect(jsonPath("$.currentValue").value(30000.00))
                .andExpect(jsonPath("$.absoluteReturn").value(-30000.00));

        verify(jwtService, times(1)).extractUsername(JWT_TOKEN);
        verify(userService, times(1)).getUserByEmail(USERNAME);
        verify(portfolioService, times(1)).getInvestmentById(mockUser, portfolioId);
        log.debug("testGetPortfolioByIdSuccess passed");
    }

    @Test
    void testGetUserPortfolioWithoutAuthorizationHeader() throws Exception {
        log.debug("Running testGetUserPortfolioWithoutAuthorizationHeader");
        mockMvc.perform(get("/portfolio"))
                .andExpect(status().isBadRequest());
        log.debug("testGetUserPortfolioWithoutAuthorizationHeader passed");
    }

    @Test
    void testBuyInvestmentWithInvalidRequest() throws Exception {
        log.debug("Running testBuyInvestmentWithInvalidRequest");
        BuyInvestmentRequestDto invalidRequest = BuyInvestmentRequestDto.builder().build();
        // Missing required fields

        mockMvc.perform(post("/portfolio/buy")
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        log.debug("testBuyInvestmentWithInvalidRequest passed");
    }

    @Test
    void testPortfolioValueCalculation() throws Exception {
        log.debug("Running testPortfolioValueCalculation");
        User mockUser = createMockUser();
        PortfolioResponseDto portfolioWithMultipleItems = createPortfolioWithMultipleItems();

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(portfolioService.getUserPortfolio(mockUser)).thenReturn(portfolioWithMultipleItems);

        mockMvc.perform(get("/portfolio")
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvestedValue").value(15500.00))
                .andExpect(jsonPath("$.totalCurrentValue").value(22550.00))
                .andExpect(jsonPath("$.holdings").isArray())
                .andExpect(jsonPath("$.holdings").isNotEmpty());

        verify(jwtService, times(1)).extractUsername(JWT_TOKEN);
        verify(userService, times(1)).getUserByEmail(USERNAME);
        verify(portfolioService, times(1)).getUserPortfolio(mockUser);
        log.debug("testPortfolioValueCalculation passed");
    }

    @Test
    void testPortfolioCalculationNegativeGainLoss() throws Exception {
        log.debug("Running testPortfolioCalculationNegativeGainLoss");
        // Arrange
        User mockUser = createMockUser();
        PortfolioResponseDto portfolioWithLoss = createPortfolioWithLoss();

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USERNAME);
        when(userService.getUserByEmail(USERNAME)).thenReturn(mockUser);
        when(portfolioService.getUserPortfolio(mockUser)).thenReturn(portfolioWithLoss);

        // Act & Assert
        mockMvc.perform(get("/portfolio")
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvestedValue").value(3800.00))
                .andExpect(jsonPath("$.totalCurrentValue").value(1800.00))
                .andExpect(jsonPath("$.holdings[0].absoluteReturn").value(-2000.00));

        log.debug("testPortfolioCalculationNegativeGainLoss passed");
    }



    // Helper methods for creating mock objects
    private User createMockUser() {
        User user = new User();
        user.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
        user.setEmail(USERNAME);
        user.setPasswordHash("John@123");
        user.setRole(UserRole.USER);
        return user;
    }

    private PortfolioItemDto createMockPortfolioItem() {
        return PortfolioItemDto.builder()
                .id(1)
                .investmentProductId(3)
                .investmentProductName("Gold mine")
                .type("STOCK")
                .riskLevel("HIGH")
                .unitsOwned(BigDecimal.valueOf(200.00))
                .avgPurchasePrice(BigDecimal.valueOf(300.00))
                .investedValue(BigDecimal.valueOf(60000.00))
                .currentValue(BigDecimal.valueOf(30000.00))
                .absoluteReturn(BigDecimal.valueOf(-30000.00))
                .percentageReturn(BigDecimal.valueOf(-50.00))
                .build();
    }

    private PortfolioResponseDto createMockPortfolioResponse() {
        return PortfolioResponseDto.builder()
                .totalInvestedValue(BigDecimal.valueOf(60000.00))
                .totalCurrentValue(BigDecimal.valueOf(30000.00))
                .holdings(Arrays.asList(createMockPortfolioItem()))
                .build();
    }

    private PortfolioResponseDto createPortfolioWithMultipleItems() {
        PortfolioItemDto item1 = PortfolioItemDto.builder()
                .id(1)
                .investmentProductId(101)
                .investmentProductName("AAPL")
                .type("STOCK")
                .riskLevel("MEDIUM")
                .unitsOwned(BigDecimal.valueOf(10))
                .avgPurchasePrice(BigDecimal.valueOf(150.00))
                .currentNAV(BigDecimal.valueOf(155.00))
                .investedValue(BigDecimal.valueOf(1500.00)) // 10 * 150
                .currentValue(BigDecimal.valueOf(1550.00))  // 10 * 155
                .absoluteReturn(BigDecimal.valueOf(50.00))  // 1550 - 1500
                .percentageReturn(BigDecimal.valueOf(3.33)) // (50/1500)*100 approx
                .build();

        PortfolioItemDto item2 = PortfolioItemDto.builder()
                .id(2)
                .investmentProductId(102)
                .investmentProductName("GOOGL")
                .type("STOCK")
                .riskLevel("HIGH")
                .unitsOwned(BigDecimal.valueOf(5))
                .avgPurchasePrice(BigDecimal.valueOf(2000.00))
                .currentNAV(BigDecimal.valueOf(3000.00))
                .investedValue(BigDecimal.valueOf(10000.00))  // 5 * 2000
                .currentValue(BigDecimal.valueOf(15000.00))  // 5 * 3000
                .absoluteReturn(BigDecimal.valueOf(5000.00)) // 15000 - 10000
                .percentageReturn(BigDecimal.valueOf(50.00)) // (5000/10000)*100
                .build();

        PortfolioItemDto item3 = PortfolioItemDto.builder()
                .id(3)
                .investmentProductId(103)
                .investmentProductName("MSFT")
                .type("STOCK")
                .riskLevel("MEDIUM")
                .unitsOwned(BigDecimal.valueOf(20))
                .avgPurchasePrice(BigDecimal.valueOf(200.00))
                .currentNAV(BigDecimal.valueOf(300.00))
                .investedValue(BigDecimal.valueOf(4000.00))   // 20 * 200
                .currentValue(BigDecimal.valueOf(6000.00))   // 20 * 300
                .absoluteReturn(BigDecimal.valueOf(2000.00)) // 6000 - 4000
                .percentageReturn(BigDecimal.valueOf(50.00)) // (2000/4000)*100
                .build();

        BigDecimal totalInvested = item1.getInvestedValue()
                .add(item2.getInvestedValue())
                .add(item3.getInvestedValue());

        BigDecimal totalCurrent = item1.getCurrentValue()
                .add(item2.getCurrentValue())
                .add(item3.getCurrentValue());

        return PortfolioResponseDto.builder()
                .totalInvestedValue(totalInvested)
                .totalCurrentValue(totalCurrent)
                .holdings(Arrays.asList(item1, item2, item3))
                .build();
    }

    private PortfolioResponseDto createPortfolioWithLoss() {
        PortfolioItemDto item = PortfolioItemDto.builder()
                .investmentProductName("AAPL")
                .unitsOwned(BigDecimal.valueOf(10))
                .avgPurchasePrice(BigDecimal.valueOf(200.00))
                .currentNAV(BigDecimal.valueOf(180.00))
                .currentValue(BigDecimal.valueOf(1800.00))
                .absoluteReturn(BigDecimal.valueOf(-2000.00))
                .build();

        return PortfolioResponseDto.builder()
                .totalInvestedValue(BigDecimal.valueOf(3800.00))
                .totalCurrentValue(BigDecimal.valueOf(1800.00))
                .holdings(List.of(item))
                .build();
    }

}
