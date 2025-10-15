package com.zeta_horizon.investment_portfolio_tracker.controller;

import com.zeta_horizon.investment_portfolio_tracker.dto.MessageRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketRequestDto;
import com.zeta_horizon.investment_portfolio_tracker.dto.SupportTicketResponseDto;
import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import com.zeta_horizon.investment_portfolio_tracker.enums.TicketStatus;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import com.zeta_horizon.investment_portfolio_tracker.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
@Slf4j
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final JWTService jwtService;

    //controller for user to create tickets
    @PostMapping("/createTicket")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SupportTicketResponseDto> createTicket(
            @Valid @RequestBody SupportTicketRequestDto requestDto, @RequestHeader("Authorization") String bearerToken) {
        String email = getUserName(bearerToken);
        log.info("Creating new support ticket by {} for subject={}", email, requestDto.getSubject());
        SupportTicketResponseDto createdTicket = supportTicketService.createTicket(requestDto, email);
        log.info("Created ticket with id={}", createdTicket.getTicketId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    //get all users the tickets created by user
    @GetMapping("/user")
    public ResponseEntity<List<SupportTicketResponseDto>> getUserTickets(@RequestHeader("Authorization") String bearerToken) {
        String email = getUserName(bearerToken);
        String role = (String) getRole(bearerToken).get(0);
        log.info("Fetching support tickets for current user");
        List<SupportTicketResponseDto> tickets ;
        if(role.equalsIgnoreCase("role_admin")){
            tickets = supportTicketService.getAllTickets();
        } else {
            tickets = supportTicketService.getTicketsForCurrentUser(email);
        }
        log.debug("Found {} tickets for current user", tickets.size());
        return ResponseEntity.ok(tickets);
    }

    //get the particular ticket based on the id
    @GetMapping("ticket/{ticketId}")
    public ResponseEntity<SupportTicketResponseDto> getTicketById(@PathVariable UUID ticketId) {
        log.info("ticket id get from the request is {}", ticketId);
        SupportTicketResponseDto reponseTicket = supportTicketService.getTicketViaId(ticketId);
        log.info("response get from the service is {}", reponseTicket);
        return ResponseEntity.ok(reponseTicket);
    }

    //get all the ticket created by the user
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicketResponseDto>> getAllTickets() {
        log.info("Admin is fetching all support tickets");
        List<SupportTicketResponseDto> tickets = supportTicketService.getAllTickets();
        log.debug("Admin fetched {} tickets", tickets.size());
        return ResponseEntity.ok(tickets);
    }

    //manage conversation and related updation of the ticket
    @PutMapping("/reply/")
    public ResponseEntity<SupportTicketResponseDto> respondToTicket(@RequestBody MessageRequestDto messageRequestDto
            , @RequestHeader("Authorization") String bearerToken) {
        String email = getUserName(bearerToken);
        log.info("Responding to ticketId={}, with status={}, message={}, email={}",
                messageRequestDto.getTicketId(), messageRequestDto.getStatus(),
                messageRequestDto.getResponseMessage(), email);
        SupportTicketResponseDto responseDto = supportTicketService.
                respondToTicket(messageRequestDto,email);
        log.debug("Response updated for ticketId={}", messageRequestDto.getTicketId());
        return ResponseEntity.ok(responseDto);
    }

    //filter endpoint for user tickets based on the priority and ticket status
    @GetMapping("/filter")
    public ResponseEntity<List<SupportTicketResponseDto>> filterTickets(
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) TicketStatus status) {
        log.info("Received filter request: priority={}, status={}", priority, status);
        List<SupportTicketResponseDto> resultantTickets = supportTicketService.filterTickets(priority, status);
        log.info("ticket received from service after filtering {}",resultantTickets);
        return ResponseEntity.ok(resultantTickets);
    }

    //filter endpoint for all users tickets based on the priority and ticket status
    @GetMapping("/user/filter")
    public ResponseEntity<List<SupportTicketResponseDto>> filterUserTickets(
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) TicketStatus status,
            @RequestHeader("Authorization") String bearerToken) {
        String emailId = getUserName(bearerToken);
        log.info("Received request to filter tickets for user={} with priority={} and status={}"
                , emailId, priority, status);
        List<SupportTicketResponseDto> filtered = supportTicketService.filterTicketsForUser(emailId, priority, status);
        return ResponseEntity.ok(filtered);
    }

    //fucntion to extract email from the token
    public String getUserName(String bearerToken) {
        String token = bearerToken.substring(7);
        return jwtService.extractUsername(token);
    }

    public List getRole(String bearerToken){
        String token = bearerToken.substring(7);
        return jwtService.extractRoles(token);
    }
}

