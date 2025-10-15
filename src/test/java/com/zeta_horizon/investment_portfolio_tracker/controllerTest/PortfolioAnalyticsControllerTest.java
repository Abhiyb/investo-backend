package com.zeta_horizon.investment_portfolio_tracker.controllerTest;

import com.zeta_horizon.investment_portfolio_tracker.controller.PortfolioAnalyticsController;
import com.zeta_horizon.investment_portfolio_tracker.dto.AssetAllocationDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.GainLossDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.PortfolioSummaryDto;
import com.zeta_horizon.investment_portfolio_tracker.service.PortfolioAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PortfolioAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortfolioAnalyticsService portfolioAnalyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PortfolioAnalyticsController controller = new PortfolioAnalyticsController(portfolioAnalyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetPortfolioSummary() throws Exception {
        PortfolioSummaryDto summaryDto = PortfolioSummaryDto.builder()
                .totalInvested(new BigDecimal("10000"))
                .currentValue(new BigDecimal("12000"))
                .absoluteReturn(new BigDecimal("2000"))
                .returnPercentage(new BigDecimal("20.0"))
                .build();

        when(portfolioAnalyticsService.getPortfolioSummary(null)).thenReturn(summaryDto);

        mockMvc.perform(get("/portfolio/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalInvested").value(10000))
                .andExpect(jsonPath("$.currentValue").value(12000))
                .andExpect(jsonPath("$.absoluteReturn").value(2000))
                .andExpect(jsonPath("$.returnPercentage").value(20.0));
    }

    @Test
    void testGetAssetAllocation() throws Exception {
        List<AssetAllocationDto> allocations = List.of(
                new AssetAllocationDto("CRYPTOCURRENCY", new BigDecimal("70.0")),
                new AssetAllocationDto("STOCKS", new BigDecimal("30.0"))
        );

        when(portfolioAnalyticsService.getAssetAllocation(null)).thenReturn(allocations);

        mockMvc.perform(get("/portfolio/allocation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].assetType").value("CRYPTOCURRENCY"))
                .andExpect(jsonPath("$[0].percentage").value(70.0))
                .andExpect(jsonPath("$[1].assetType").value("STOCKS"))
                .andExpect(jsonPath("$[1].percentage").value(30.0));
    }

    @Test
    void testGetGainLossAnalysis() throws Exception {
        List<GainLossDto> gainLossList = List.of(
                new GainLossDto("Bitcoin", new BigDecimal("10000"), new BigDecimal("12000"), new BigDecimal("2000"))
        );

        when(portfolioAnalyticsService.getGainLossAnalysis(null)).thenReturn(gainLossList);

        mockMvc.perform(get("/portfolio/gains"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].investmentName").value("Bitcoin"))
                .andExpect(jsonPath("$[0].investedAmount").value(10000))
                .andExpect(jsonPath("$[0].currentValue").value(12000))
                .andExpect(jsonPath("$[0].gainOrLoss").value(2000));
    }
}