package com.zeta_horizon.investment_portfolio_tracker.controllerTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta_horizon.investment_portfolio_tracker.controller.SupportTicketController;
import com.zeta_horizon.investment_portfolio_tracker.dto.MessageRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.SupportTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(SupportTicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportTicketService supportTicketService;

    @MockBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String authHeader;
    private SupportTicketRequestDto requestDto;
    private SupportTicketResponseDto responseDto;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer mock_token";

        requestDto = new SupportTicketRequestDto();
        requestDto.setSubject("Login issue");
        requestDto.setDescription("User cannot log in");
        requestDto.setPriority(Priority.HIGH);

        responseDto = new SupportTicketResponseDto();
        responseDto.setTicketId(UUID.randomUUID());
        responseDto.setSubject("Login issue");
        responseDto.setDescription("User cannot log in");
        responseDto.setPriority(Priority.HIGH);
        responseDto.setStatus(TicketStatus.OPEN);
    }

    @Test
    void testCreateTicket() throws Exception {
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(supportTicketService.createTicket(any(SupportTicketRequestDto.class), eq("test@example.com")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/support/createTicket")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Login issue"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void testGetUserTickets() throws Exception {
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(jwtService.extractRoles(anyString())).thenReturn(List.of(new String("ROLE_USER")));
        when(supportTicketService.getTicketsForCurrentUser("test@example.com"))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/support/user")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Login issue"));
    }

    @Test
    void testGetTicketById() throws Exception {
        UUID ticketId = UUID.randomUUID();
        when(supportTicketService.getTicketViaId(ticketId)).thenReturn(responseDto);

        mockMvc.perform(get("/support/ticket/{ticketId}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Login issue"));
    }

    @Test
    void testGetAllTickets() throws Exception {
        when(supportTicketService.getAllTickets()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/support/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Login issue"));
    }

    @Test
    void testRespondToTicket() throws Exception {
        MessageRequestDto messageRequestDto = new MessageRequestDto();
        messageRequestDto.setTicketId(UUID.randomUUID().toString());
        messageRequestDto.setResponseMessage("We are working on it");
        messageRequestDto.setStatus(TicketStatus.OPEN.toString());

        when(jwtService.extractUsername(anyString())).thenReturn("admin@example.com");
        when(supportTicketService.respondToTicket(any(MessageRequestDto.class), eq("admin@example.com")))
                .thenReturn(responseDto);

        mockMvc.perform(put("/support/reply/")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Login issue"));
    }

    @Test
    void testFilterTickets() throws Exception {
        when(supportTicketService.filterTickets(Priority.HIGH, TicketStatus.OPEN))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/support/filter")
                        .param("priority", "HIGH")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Login issue"));
    }

    @Test
    void testAdminCanRespondToTicket() throws Exception {
        // Arrange
        MessageRequestDto requestDto = new MessageRequestDto();
        requestDto.setTicketId(UUID.randomUUID().toString());
        requestDto.setResponseMessage("Response from admin");
        requestDto.setStatus(TicketStatus.RESPONDED.toString());

        SupportTicketResponseDto updatedResponse = new SupportTicketResponseDto();
        updatedResponse.setTicketId(UUID.fromString(requestDto.getTicketId()));
        updatedResponse.setSubject("Login issue");
        updatedResponse.setDescription("User cannot log in");
        updatedResponse.setPriority(Priority.HIGH);
        updatedResponse.setStatus(TicketStatus.RESPONDED);

        when(jwtService.extractUsername(anyString())).thenReturn("admin@example.com");
        when(supportTicketService.respondToTicket(any(MessageRequestDto.class), eq("admin@example.com")))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/support/reply/")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESPONDED"))
                .andExpect(jsonPath("$.subject").value("Login issue"));

        verify(supportTicketService).respondToTicket(any(MessageRequestDto.class), eq("admin@example.com"));
    }

    @Test
    void testSupportTicketLifecycle() throws Exception {
        SupportTicketRequestDto createRequest = new SupportTicketRequestDto();
        createRequest.setSubject("Login issue");
        createRequest.setDescription("User cannot log in");
        createRequest.setPriority(Priority.HIGH);

        SupportTicketResponseDto openTicket = new SupportTicketResponseDto();
        openTicket.setTicketId(UUID.randomUUID());
        openTicket.setSubject("Login issue");
        openTicket.setDescription("User cannot log in");
        openTicket.setPriority(Priority.HIGH);
        openTicket.setStatus(TicketStatus.OPEN);

        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(supportTicketService.createTicket(any(SupportTicketRequestDto.class), eq("test@example.com")))
                .thenReturn(openTicket);

        mockMvc.perform(post("/support/createTicket")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        // Step 2: Admin responds, status -> IN_PROGRESS
        MessageRequestDto inProgressRequest = new MessageRequestDto();
        inProgressRequest.setTicketId(openTicket.getTicketId().toString());
        inProgressRequest.setResponseMessage("We are working on it");
        inProgressRequest.setStatus(TicketStatus.RESPONDED.toString());

        SupportTicketResponseDto inProgressTicket = new SupportTicketResponseDto();
        inProgressTicket.setTicketId(openTicket.getTicketId());
        inProgressTicket.setSubject("Login issue");
        inProgressTicket.setDescription("User cannot log in");
        inProgressTicket.setPriority(Priority.HIGH);
        inProgressTicket.setStatus(TicketStatus.RESPONDED);

        when(jwtService.extractUsername(anyString())).thenReturn("admin@example.com");
        when(supportTicketService.respondToTicket(any(MessageRequestDto.class), eq("admin@example.com")))
                .thenReturn(inProgressTicket);

        mockMvc.perform(put("/support/reply/")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESPONDED"));

        // Step 3: Admin closes the ticket, status -> CLOSED
        MessageRequestDto closeRequest = new MessageRequestDto();
        closeRequest.setTicketId(openTicket.getTicketId().toString());
        closeRequest.setResponseMessage("Issue resolved and closed.");
        closeRequest.setStatus(TicketStatus.CLOSED.toString());

        SupportTicketResponseDto closedTicket = new SupportTicketResponseDto();
        closedTicket.setTicketId(openTicket.getTicketId());
        closedTicket.setSubject("Login issue");
        closedTicket.setDescription("User cannot log in");
        closedTicket.setPriority(Priority.HIGH);
        closedTicket.setStatus(TicketStatus.CLOSED);

        when(supportTicketService.respondToTicket(any(MessageRequestDto.class), eq("admin@example.com")))
                .thenReturn(closedTicket);

        mockMvc.perform(put("/support/reply/")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }


    @Test
    void testFilterUserTickets() throws Exception {
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(supportTicketService.filterTicketsForUser(eq("test@example.com"), eq(Priority.HIGH), eq(TicketStatus.OPEN)))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/support/user/filter")
                        .header("Authorization", authHeader)
                        .param("priority", "HIGH")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Login issue"));
    }
}