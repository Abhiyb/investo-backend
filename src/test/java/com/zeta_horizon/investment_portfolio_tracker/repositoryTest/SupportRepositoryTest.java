package com.zeta_horizon.investment_portfolio_tracker.repositoryTest;

import com.zeta_horizon.investment_portfolio_tracker.entity.SupportTicket;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import com.zeta_horizon.investment_portfolio_tracker.repository.SupportTicketRepository;
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
public class SupportRepositoryTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    private SupportTicket highPriorityTicket;
    private SupportTicket mediumPriorityTicket;
    private SupportTicket lowPriorityTicket;
    private User testUser;
    private InvestmentProduct testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testProduct = new InvestmentProduct();
        testProduct.setId(1);

        highPriorityTicket = SupportTicket.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .investmentProduct(testProduct)
                .subject("Urgent issue")
                .description("System not working")
                .status(TicketStatus.OPEN)
                .priority(Priority.HIGH)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        mediumPriorityTicket = SupportTicket.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .investmentProduct(null)
                .subject("General question")
                .description("How to use feature X")
                .status(TicketStatus.RESPONDED)
                .priority(Priority.MEDIUM)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        lowPriorityTicket = SupportTicket.builder()
                .id(UUID.randomUUID())
                .user(new User())
                .investmentProduct(testProduct)
                .subject("Feature request")
                .description("Please add new chart type")
                .status(TicketStatus.CLOSED)
                .priority(Priority.LOW)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByPriorityOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(highPriorityTicket);
        when(supportTicketRepository.findByPriorityOrderByCreatedAtDesc(Priority.HIGH))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByPriorityOrderByCreatedAtDesc(Priority.HIGH);

        assertEquals(1, result.size());
        assertEquals(Priority.HIGH, result.get(0).getPriority());
        assertEquals("Urgent issue", result.get(0).getSubject());
        verify(supportTicketRepository, times(1)).findByPriorityOrderByCreatedAtDesc(Priority.HIGH);
    }

    @Test
    void findByStatusOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(mediumPriorityTicket);
        when(supportTicketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.RESPONDED))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.RESPONDED);

        assertEquals(1, result.size());
        assertEquals(TicketStatus.RESPONDED, result.get(0).getStatus());
        assertNull(result.get(0).getInvestmentProduct());
        verify(supportTicketRepository, times(1)).findByStatusOrderByCreatedAtDesc(TicketStatus.RESPONDED);
    }

    @Test
    void findByPriorityAndStatusOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(highPriorityTicket);
        when(supportTicketRepository.findByPriorityAndStatusOrderByCreatedAtDesc(Priority.HIGH, TicketStatus.OPEN))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByPriorityAndStatusOrderByCreatedAtDesc(
                Priority.HIGH, TicketStatus.OPEN);

        assertEquals(1, result.size());
        assertEquals(Priority.HIGH, result.get(0).getPriority());
        assertEquals(TicketStatus.OPEN, result.get(0).getStatus());
        assertEquals(testProduct, result.get(0).getInvestmentProduct());
        verify(supportTicketRepository, times(1))
                .findByPriorityAndStatusOrderByCreatedAtDesc(Priority.HIGH, TicketStatus.OPEN);
    }

    @Test
    void findByUserIdAndPriorityAndStatusOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(highPriorityTicket);
        when(supportTicketRepository.findByUserIdAndPriorityAndStatusOrderByCreatedAtDesc(
                testUser.getId(), Priority.HIGH, TicketStatus.OPEN))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByUserIdAndPriorityAndStatusOrderByCreatedAtDesc(
                testUser.getId(), Priority.HIGH, TicketStatus.OPEN);

        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
        assertEquals(Priority.HIGH, result.get(0).getPriority());
        assertEquals(TicketStatus.OPEN, result.get(0).getStatus());
        verify(supportTicketRepository, times(1))
                .findByUserIdAndPriorityAndStatusOrderByCreatedAtDesc(testUser.getId(), Priority.HIGH, TicketStatus.OPEN);
    }

    @Test
    void findByUserIdAndPriorityOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(highPriorityTicket);
        when(supportTicketRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(testUser.getId(), Priority.HIGH))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(
                testUser.getId(), Priority.HIGH);

        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
        assertEquals(Priority.HIGH, result.get(0).getPriority());
        assertEquals("System not working", result.get(0).getDescription());
        verify(supportTicketRepository, times(1))
                .findByUserIdAndPriorityOrderByCreatedAtDesc(testUser.getId(), Priority.HIGH);
    }

    @Test
    void findByUserIdAndStatusOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(mediumPriorityTicket);
        when(supportTicketRepository.findByUserIdAndStatusOrderByCreatedAtDesc(testUser.getId(), TicketStatus.RESPONDED))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                testUser.getId(), TicketStatus.RESPONDED);

        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
        assertEquals(TicketStatus.RESPONDED, result.get(0).getStatus());
        assertEquals("General question", result.get(0).getSubject());
        verify(supportTicketRepository, times(1))
                .findByUserIdAndStatusOrderByCreatedAtDesc(testUser.getId(), TicketStatus.RESPONDED);
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(mediumPriorityTicket, highPriorityTicket);
        when(supportTicketRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertEquals(2, result.size());
        assertEquals(testUser.getId(), result.get(0).getUser().getId());
        assertEquals(testUser.getId(), result.get(1).getUser().getId());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        verify(supportTicketRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void findAllByOrderByCreatedAtDesc() {
        List<SupportTicket> expectedTickets = Arrays.asList(lowPriorityTicket, mediumPriorityTicket, highPriorityTicket);
        when(supportTicketRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(expectedTickets);

        List<SupportTicket> result = supportTicketRepository.findAllByOrderByCreatedAtDesc();

        assertEquals(3, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        assertTrue(result.get(1).getCreatedAt().isAfter(result.get(2).getCreatedAt()));
        verify(supportTicketRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }
}
