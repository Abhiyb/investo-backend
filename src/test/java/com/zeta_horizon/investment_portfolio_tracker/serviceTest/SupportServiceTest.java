package com.zeta_horizon.investment_portfolio_tracker.serviceTest;

import com.zeta_horizon.investment_portfolio_tracker.dto.*;
import com.zeta_horizon.investment_portfolio_tracker.entity.*;
import com.zeta_horizon.investment_portfolio_tracker.enums.*;
import com.zeta_horizon.investment_portfolio_tracker.exception.*;
import com.zeta_horizon.investment_portfolio_tracker.mapper.*;
import com.zeta_horizon.investment_portfolio_tracker.repository.*;
import com.zeta_horizon.investment_portfolio_tracker.service.implementation.SupportTicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportTicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InvestmentProductRepository investmentProductRepository;
    @Mock
    private TicketMessageRepository ticketMessageRepository;
    @Mock
    private SupportTicketMapper ticketMapper;
    @Mock
    private TicketMessageMapper messageMapper;

    @InjectMocks
    private SupportTicketServiceImpl supportTicketService;

    private User user;
    private SupportTicket ticket;
    private SupportTicketResponseDto ticketResponseDto;
    private InvestmentProduct investmentProduct;

    @BeforeEach
    void setup() {
        user = User.builder().id(UUID.randomUUID()).email("test@example.com").role(UserRole.USER).build();
        investmentProduct = InvestmentProduct.builder().id(1).name("Test Investment").build();
        ticket = SupportTicket.builder().id(UUID.randomUUID()).user(user).investmentProduct(investmentProduct)
                .subject("Test Subject").description("Test Description")
                .priority(Priority.HIGH).status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        ticketResponseDto = SupportTicketResponseDto.builder().ticketId(ticket.getId())
                .subject(ticket.getSubject()).description(ticket.getDescription())
                .priority(ticket.getPriority()).status(ticket.getStatus()).build();
    }

    @Test
    void createTicket_shouldCreateTicketSuccessfully() {
        SupportTicketRequestDto requestDto = SupportTicketRequestDto.builder()
                .subject("Test Subject").description("Test Description")
                .investmentProductName("Test Investment").priority(Priority.HIGH).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(investmentProductRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("Test Investment"))
                .thenReturn(Collections.singletonList(investmentProduct));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(ticket);
        when(ticketMapper.toResponseDto(any(SupportTicket.class))).thenReturn(ticketResponseDto);

        SupportTicketResponseDto result = supportTicketService.createTicket(requestDto, user.getEmail());

        assertNotNull(result);
        assertEquals(ticket.getId(), result.getTicketId());
        verify(ticketRepository).save(any(SupportTicket.class));
    }

    @Test
    void getTicketsForCurrentUser_shouldReturnTickets() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        List<SupportTicketResponseDto> result = supportTicketService.getTicketsForCurrentUser(user.getEmail());

        assertEquals(1, result.size());
        assertEquals(ticket.getId(), result.get(0).getTicketId());
    }

    @Test
    void getAllTickets_shouldReturnAllTickets() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        List<SupportTicketResponseDto> result = supportTicketService.getAllTickets();

        assertEquals(1, result.size());
    }

    @Test
    void getTicketViaId_shouldReturnTicket() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        SupportTicketResponseDto result = supportTicketService.getTicketViaId(ticket.getId());

        assertNotNull(result);
    }

    @Test
    void getTicketViaId_shouldThrowExceptionIfNotFound() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(SupportTicketNotFoundException.class, () -> supportTicketService.getTicketViaId(ticketId));
    }

    @Test
    void filterTickets_shouldReturnFilteredTickets() {
        when(ticketRepository.findByPriorityOrderByCreatedAtDesc(Priority.HIGH)).thenReturn(List.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        List<SupportTicketResponseDto> result = supportTicketService.filterTickets(Priority.HIGH, null);

        assertEquals(1, result.size());
    }

    @Test
    void filterTickets_shouldReturnAllTicketsWhenNoFilter() {
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        List<SupportTicketResponseDto> result = supportTicketService.filterTickets(null, null);

        assertEquals(1, result.size());
    }

    @Test
    void filterTicketsForUser_shouldReturnFilteredTickets() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(ticketRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(user.getId(), Priority.HIGH))
                .thenReturn(List.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        List<SupportTicketResponseDto> result = supportTicketService.filterTicketsForUser(user.getEmail(),
                Priority.HIGH, null);

        assertEquals(1, result.size());
    }

    @Test
    void respondToTicket_shouldRespondSuccessfully() {
        UUID ticketId = ticket.getId();
        MessageRequestDto messageRequestDto = MessageRequestDto.builder()
                .ticketId(ticketId.toString()).responseMessage("Message").status("CLOSED").build();

        TicketMessage message = TicketMessage.builder().supportTicket(ticket).sender(user).message("Message").build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(ticketId)).thenReturn(List.of(message));
        when(messageMapper.toDto(message)).thenReturn(new TicketMessageDto());
        when(ticketMapper.toResponseDto(ticket)).thenReturn(ticketResponseDto);

        SupportTicketResponseDto result = supportTicketService.respondToTicket(messageRequestDto, user.getEmail());

        assertNotNull(result);
        assertEquals(ticket.getId(), result.getTicketId());
        verify(ticketMessageRepository).save(any(TicketMessage.class));
    }

    @Test
    void respondToTicket_shouldThrowWrappedTicketClosedException() {
        ticket.setStatus(TicketStatus.CLOSED);

        MessageRequestDto request = MessageRequestDto.builder()
                .ticketId(ticket.getId().toString())
                .responseMessage("Test")
                .build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> supportTicketService.respondToTicket(request, user.getEmail()));
        assertTrue(thrown.getCause() instanceof TicketClosedException);
    }

}
