package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.dto.MessageRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.TicketMessageDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.InvestmentProduct;
import com.zeta_horizon.investment_portfolio_tracker.entity.TicketMessage;
import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.entity.SupportTicket;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import com.zeta_horizon.investment_portfolio_tracker.exception.SupportTicketNotFoundException;
import com.zeta_horizon.investment_portfolio_tracker.exception.TicketClosedException;
import com.zeta_horizon.investment_portfolio_tracker.exception.UserNotFoundException;
import com.zeta_horizon.investment_portfolio_tracker.mapper.SupportTicketMapper;
import com.zeta_horizon.investment_portfolio_tracker.mapper.TicketMessageMapper;
import com.zeta_horizon.investment_portfolio_tracker.repository.SupportTicketRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.TicketMessageRepository;
import com.zeta_horizon.investment_portfolio_tracker.service.SupportTicketService;
import com.zeta_horizon.investment_portfolio_tracker.repository.InvestmentProductRepository;
import com.zeta_horizon.investment_portfolio_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final InvestmentProductRepository investmentProductRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final SupportTicketMapper ticketMapper;
    private final TicketMessageMapper messageMapper;
    private final  ExecutorService customExecutor = new ThreadPoolExecutor(5, 10, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    //ticket creation methord
    @Override
    public SupportTicketResponseDto createTicket(SupportTicketRequestDto requestDto, String emailId) {
        try {
            CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() ->
                    fetchUserByEmail(emailId), customExecutor);
            CompletableFuture<InvestmentProduct> productFuture = CompletableFuture.supplyAsync(() -> {
                if (!StringUtils.isEmpty(requestDto.getInvestmentProductName())) {
                    return fetchInvestmentProductById(requestDto.getInvestmentProductName());
                }
                return null;
            }, customExecutor);

            User user = userFuture.get();
            InvestmentProduct product = productFuture.get();
            SupportTicket ticket = buildSupportTicket(requestDto, user, product);
            SupportTicket saved = ticketRepository.save(ticket);
            log.info("Support ticket created with ID: {}", saved.getId());
            return ticketMapper.toResponseDto(saved);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to create ticket", exception);
        }
    }

    //get the users ticket based on the email
    @Override
    public List<SupportTicketResponseDto> getTicketsForCurrentUser(String emailId) {
        log.info("Fetching support tickets for current user");
        List<SupportTicket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(getUserId(emailId));
        log.info("Found {} tickets for current user", tickets.size());
        return tickets.stream().map(ticketMapper::toResponseDto).collect(Collectors.toList());
    }

    //methord to fetch all the tickets
    @Override
    public List<SupportTicketResponseDto> getAllTickets() {
        log.info("Fetching all support tickets");
        List<SupportTicket> tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        return tickets.stream()
                .map(ticketMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    //methord to update the messages and manage the conversation
    @Override
    @Transactional
    public SupportTicketResponseDto respondToTicket(MessageRequestDto messageRequestDto, String emailId) {
        try {
            User user = fetchUserByEmail(emailId);
            SupportTicket ticket = fetchTicketById(UUID.fromString(messageRequestDto.getTicketId()));
            if(ticket.getStatus().equals(TicketStatus.CLOSED)){
                throw new TicketClosedException("ticket is already closed. pls raise another one");
            }
            saveTicketMessage(ticket, user, messageRequestDto.getResponseMessage());
            updateTicketStatusAndTimestamp(ticket, messageRequestDto.getStatus());
            List<TicketMessageDto> messages = getMessagesForTicket(ticket.getId());
            SupportTicketResponseDto dto = ticketMapper.toResponseDto(ticket);
            dto.setMessages(messages);
            return dto;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to respond to ticket", exception);
        }
    }

    //filtering all the tickets based on priority and status
    @Override
    public List<SupportTicketResponseDto> filterTickets(Priority priority, TicketStatus status) {
        log.info("Filtering tickets with priority={} and status={}", priority, status);
        List<SupportTicket> filteredTickets;
        if (priority != null && status != null) {
            filteredTickets = ticketRepository.findByPriorityAndStatusOrderByCreatedAtDesc(priority, status);
        } else if (priority != null) {
            filteredTickets = ticketRepository.findByPriorityOrderByCreatedAtDesc(priority);
        } else if (status != null) {
            filteredTickets = ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            filteredTickets = ticketRepository.findAll();
        }
        log.info("Found {} filtered tickets", filteredTickets.size());
        return filteredTickets.stream()
                .map(ticketMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    //filtering the user tickets based on priority and status
    @Override
    public List<SupportTicketResponseDto> filterTicketsForUser(String emailId, Priority priority, TicketStatus status) {
        log.info("Filtering tickets for user={} with priority={} and status={}", emailId, priority, status);
        UUID userId = getUserId(emailId);
        List<SupportTicket> filteredTickets;
        if (priority != null && status != null) {
            filteredTickets = ticketRepository.findByUserIdAndPriorityAndStatusOrderByCreatedAtDesc(userId, priority, status);
        } else if (priority != null) {
            filteredTickets = ticketRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(userId, priority);
        } else if (status != null) {
            filteredTickets = ticketRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            filteredTickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        if (filteredTickets == null) {
            log.warn("Filtered ticket list is null, returning empty list");
            return new ArrayList<>();
        }
        log.info("Found {} filtered tickets for user={}", filteredTickets.size(), emailId);
        return filteredTickets.stream()
                .map(ticketMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    //methord to get the ticket based on the ticketId
    @Override
    public SupportTicketResponseDto getTicketViaId(UUID ticketId) {
        Optional<SupportTicket> resultantTicket = ticketRepository.findById(ticketId);
        if(resultantTicket.isPresent()){
            return ticketMapper.toResponseDto(resultantTicket.get());
        }
        throw new SupportTicketNotFoundException("Ticket with id "+ticketId+" is not found");
    }

    public UUID getUserId(String emailId) {
        return fetchUserByEmail(emailId).getId();
    }

    //get the user data based on the emailId
    private User fetchUserByEmail(String emailId) {
        User user = userRepository.findByEmail(emailId);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + emailId);
        }
        return user;
    }

    //fetching the inverstement product data based on productName
    private InvestmentProduct fetchInvestmentProductById(String productName) {
        return investmentProductRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(productName).get(0);
    }

    //fetching the ticket from the db based on the ticketId
    private SupportTicket fetchTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new SupportTicketNotFoundException("Support ticket not found with ID: " + ticketId));
    }

    //builder fucntion for ticket creation
    private SupportTicket buildSupportTicket(SupportTicketRequestDto dto, User user, InvestmentProduct investmentProduct) {
        return SupportTicket.builder()
                .user(user)
                .investmentProduct(investmentProduct)
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM)
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    //function to save the message conversation of the ticket
    private void saveTicketMessage(SupportTicket ticket, User sender, String messageContent) {
        TicketMessage message = TicketMessage.builder()
                .supportTicket(ticket)
                .sender(sender)
                .message(messageContent)
                .senderType(sender.getRole())
                .timestamp(LocalDateTime.now())
                .build();
        ticketMessageRepository.save(message);
    }

    //update the status of the ticket and save in db
    private void updateTicketStatusAndTimestamp(SupportTicket ticket, String status) {
        if (status!=null) {
            ticket.setStatus(TicketStatus.valueOf(status.toUpperCase()));
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    //fetch the message conversation of particular ticket
    private List<TicketMessageDto> getMessagesForTicket(UUID ticketId) {
        List<TicketMessage> messages = ticketMessageRepository.findBySupportTicketIdOrderByTimestampAsc(ticketId);
        return messages.stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

}

