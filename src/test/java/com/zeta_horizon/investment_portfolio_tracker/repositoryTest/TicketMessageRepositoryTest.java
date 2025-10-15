package com.zeta_horizon.investment_portfolio_tracker.repositoryTest;


import com.zeta_horizon.investment_portfolio_tracker.entity.TicketMessage;
import com.zeta_horizon.investment_portfolio_tracker.entity.SupportTicket;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.enums.UserRole;
import com.zeta_horizon.investment_portfolio_tracker.repository.TicketMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketMessageRepositoryTest {

    @Mock
    private TicketMessageRepository ticketMessageRepository;

    private TicketMessage message1;
    private TicketMessage message2;
    private TicketMessage message3;
    private UUID testTicketId;
    private User testUser;
    private SupportTicket testSupportTicket;

    @BeforeEach
    void setUp() {
        testTicketId = UUID.randomUUID();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .build();

        testSupportTicket = SupportTicket.builder()
                .id(testTicketId)
                .subject("Test Ticket")
                .build();

        message1 = TicketMessage.builder()
                .id(UUID.randomUUID())
                .supportTicket(testSupportTicket)
                .sender(testUser)
                .message("First message")
                .timestamp(LocalDateTime.now().minusHours(2))
                .senderType(UserRole.USER)
                .build();

        message2 = TicketMessage.builder()
                .id(UUID.randomUUID())
                .supportTicket(testSupportTicket)
                .sender(testUser)
                .message("Second message")
                .timestamp(LocalDateTime.now().minusHours(1))
                .senderType(UserRole.ADMIN)
                .build();

        message3 = TicketMessage.builder()
                .id(UUID.randomUUID())
                .supportTicket(testSupportTicket)
                .sender(testUser)
                .message("Third message")
                .timestamp(LocalDateTime.now())
                .senderType(UserRole.USER)
                .build();
    }

    @Test
    void findBySupportTicketIdOrderByTimestampAsc_shouldReturnMessagesInOrder() {
        List<TicketMessage> expectedMessages = Arrays.asList(message1, message2, message3);
        when(ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(testTicketId))
                .thenReturn(expectedMessages);
        List<TicketMessage> result = ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(testTicketId);
        assertEquals(3, result.size());
        assertTrue(result.get(0).getTimestamp().isBefore(result.get(1).getTimestamp()));
        assertTrue(result.get(1).getTimestamp().isBefore(result.get(2).getTimestamp()));

        assertEquals("First message", result.get(0).getMessage());
        assertEquals("Second message", result.get(1).getMessage());
        assertEquals("Third message", result.get(2).getMessage());
        assertEquals(testTicketId, result.get(0).getSupportTicket().getId());
        assertEquals(testUser.getId(), result.get(0).getSender().getId());

        verify(ticketMessageRepository, times(1)).findBySupportTicketIdOrderByTimestampAsc(testTicketId);
    }

    @Test
    void findBySupportTicketIdOrderByTimestampAsc_shouldReturnEmptyListForNoMessages() {
        when(ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(testTicketId))
                .thenReturn(List.of());
        List<TicketMessage> result = ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(testTicketId);
        assertTrue(result.isEmpty());
        verify(ticketMessageRepository, times(1)).findBySupportTicketIdOrderByTimestampAsc(testTicketId);
    }

    @Test
    void findBySupportTicketIdOrderByTimestampAsc_shouldVerifyMessageProperties() {
        List<TicketMessage> expectedMessages = List.of(message2);
        when(ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(testTicketId))
                .thenReturn(expectedMessages);
        List<TicketMessage> result = ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(testTicketId);
        assertEquals(1, result.size());
        TicketMessage message = result.get(0);

        assertEquals("Second message", message.getMessage());
        assertEquals(UserRole.ADMIN, message.getSenderType());
        assertEquals(testTicketId, message.getSupportTicket().getId());
        assertEquals(testUser.getId(), message.getSender().getId());

        verify(ticketMessageRepository, times(1)).findBySupportTicketIdOrderByTimestampAsc(testTicketId);
    }
}
