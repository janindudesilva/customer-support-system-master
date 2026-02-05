package com.customersupport.controller;

import com.customersupport.dto.AgentPerformanceDTO;
import com.customersupport.dto.AgentTicketBoardDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.dto.TicketResponseDTO;
import com.customersupport.entity.Agent;
import com.customersupport.entity.Ticket;
import com.customersupport.entity.TicketResponse;
import com.customersupport.repository.AgentRepository;
import com.customersupport.repository.UserRepository;
import com.customersupport.service.AgentService;
import com.customersupport.service.TicketResponseService;
import com.customersupport.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentController {

  @Autowired private AgentService agentService;

  @Autowired private TicketService ticketService;

  @Autowired private TicketResponseService ticketResponseService;

  @Autowired private UserRepository userRepository;

  @Autowired private AgentRepository agentRepository;

  // Get assigned tickets for the authenticated agent
  @GetMapping("/tickets")
  public ResponseEntity<?> getAgentTickets() {
    try {
      Agent agent = resolveAuthenticatedAgent();
      AgentTicketBoardDTO ticketBoard = ticketService.getAgentTicketBoard(agent.getId());
      return ResponseEntity.ok(ticketBoard);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }

  // Get ticket board data for the agent dashboard
  @GetMapping("/ticket-board")
  public ResponseEntity<?> getAgentTicketBoard() {
    try {
      Agent agent = resolveAuthenticatedAgent();
      AgentTicketBoardDTO ticketBoard = ticketService.getAgentTicketBoard(agent.getId());
      return ResponseEntity.ok(ticketBoard);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }

  // Get assigned tickets for an agent
  @GetMapping("/{agentId}/tickets")
  public ResponseEntity<List<TicketDTO>> getAgentTickets(@PathVariable Long agentId) {
    List<TicketDTO> tickets = ticketService.getTicketsByAgent(agentId);
    return ResponseEntity.ok(tickets);
  }

  // Get tickets by agent and status
  @GetMapping("/{agentId}/tickets/status/{status}")
  public ResponseEntity<List<TicketDTO>> getTicketsByAgentAndStatus(
      @PathVariable Long agentId, @PathVariable Ticket.Status status) {
    List<TicketDTO> tickets = ticketService.getTicketsByAgentAndStatus(agentId, status);
    return ResponseEntity.ok(tickets);
  }

  // Update ticket status (agent)
  @PutMapping("/tickets/{ticketId}/status")
  public ResponseEntity<?> updateTicketStatus(
      @PathVariable Long ticketId, @RequestParam Ticket.Status status, @RequestParam Long agentId) {
    try {
      TicketDTO updatedTicket = ticketService.updateTicketStatus(ticketId, status, agentId);
      return ResponseEntity.ok(updatedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Add response to ticket
  @PostMapping("/tickets/{ticketId}/responses")
  public ResponseEntity<?> addTicketResponse(
      @PathVariable Long ticketId, @Valid @RequestBody TicketResponseDTO responseDTO) {
    try {
      Agent agent = resolveAuthenticatedAgent();

      TicketDTO ticket =
          ticketService
              .getTicketById(ticketId)
              .orElseThrow(() -> new RuntimeException("Ticket not found"));

      if (!ticket.getCompanyId().equals(agent.getCompany().getId())) {
        throw new RuntimeException("You cannot respond to tickets from another company");
      }

      if (!agent.getId().equals(ticket.getAgentId())) {
        throw new RuntimeException("You are not assigned to this ticket");
      }

      responseDTO.setTicketId(ticketId);
      responseDTO.setUserId(agent.getUser().getId());
      responseDTO.setResponseType(TicketResponse.ResponseType.AGENT_REPLY);
      responseDTO.setIsPublic(
          responseDTO.getIsPublic() == null ? Boolean.TRUE : responseDTO.getIsPublic());

      TicketResponseDTO createdResponse = ticketResponseService.addResponse(ticketId, responseDTO);
      return ResponseEntity.ok(createdResponse);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get responses for a ticket
  @GetMapping("/tickets/{ticketId}/responses")
  public ResponseEntity<List<TicketResponseDTO>> getTicketResponses(@PathVariable Long ticketId) {
    Agent agent = resolveAuthenticatedAgent();
    TicketDTO ticket =
        ticketService
            .getTicketById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    if (!ticket.getCompanyId().equals(agent.getCompany().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    List<TicketResponseDTO> responses = ticketResponseService.getResponsesByTicket(ticketId);
    return ResponseEntity.ok(responses);
  }

  // Delete an internal note
  @DeleteMapping("/tickets/{ticketId}/responses/{responseId}")
  public ResponseEntity<?> deleteInternalNote(
      @PathVariable Long ticketId, @PathVariable Long responseId) {
    try {
      Agent agent = resolveAuthenticatedAgent();

      TicketDTO ticket =
          ticketService
              .getTicketById(ticketId)
              .orElseThrow(() -> new RuntimeException("Ticket not found"));

      if (!ticket.getCompanyId().equals(agent.getCompany().getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "You cannot delete responses for tickets from another company"));
      }

      if (!agent.getId().equals(ticket.getAgentId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "You are not assigned to this ticket"));
      }

      ticketResponseService.deleteResponse(responseId);
      return ResponseEntity.ok(Map.of("message", "Internal note deleted successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Edit an internal note
  @PutMapping("/tickets/{ticketId}/responses/{responseId}")
  public ResponseEntity<?> editInternalNote(
      @PathVariable Long ticketId,
      @PathVariable Long responseId,
      @Valid @RequestBody TicketResponseDTO responseDTO) {
    try {
      Agent agent = resolveAuthenticatedAgent();

      TicketDTO ticket =
          ticketService
              .getTicketById(ticketId)
              .orElseThrow(() -> new RuntimeException("Ticket not found"));

      if (!ticket.getCompanyId().equals(agent.getCompany().getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "You cannot edit responses for tickets from another company"));
      }

      if (!agent.getId().equals(ticket.getAgentId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "You are not assigned to this ticket"));
      }

      TicketResponseDTO updatedResponse =
          ticketResponseService.updateResponse(responseId, responseDTO);
      return ResponseEntity.ok(updatedResponse);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Set ticket priority
  @PutMapping("/tickets/{ticketId}/priority")
  public ResponseEntity<?> updateTicketPriority(
      @PathVariable Long ticketId,
      @RequestParam Ticket.Priority priority,
      @RequestParam Long agentId) {
    try {
      TicketDTO updatedTicket = ticketService.updateTicketPriority(ticketId, priority, agentId);
      return ResponseEntity.ok(updatedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Update agent availability
  @PutMapping("/{agentId}/availability")
  public ResponseEntity<?> updateAgentAvailability(
      @PathVariable Long agentId, @RequestParam Boolean isAvailable) {
    try {
      return ResponseEntity.ok(agentService.setAgentAvailability(agentId, isAvailable));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Assign ticket to agent
  @PutMapping("/tickets/{ticketId}/assign")
  public ResponseEntity<?> assignTicket(@PathVariable Long ticketId, @RequestParam Long agentId) {
    try {
      TicketDTO updatedTicket = ticketService.assignTicketToAgent(ticketId, agentId);
      return ResponseEntity.ok(updatedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/tickets/{ticketId}/claim")
  public ResponseEntity<?> claimTicket(@PathVariable Long ticketId) {
    try {
      Agent agent = resolveAuthenticatedAgent();
      TicketDTO updatedTicket = ticketService.claimTicket(ticketId, agent.getId());
      return ResponseEntity.ok(updatedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Get agent performance metrics
  @GetMapping("/performance")
  public ResponseEntity<?> getAgentPerformance() {
    try {
      Agent agent = resolveAuthenticatedAgent();
      AgentPerformanceDTO performance = agentService.getAgentPerformance(agent.getId());
      return ResponseEntity.ok(performance);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Resolve ticket
  @PutMapping("/tickets/{ticketId}/resolve")
  public ResponseEntity<?> resolveTicket(
      @PathVariable Long ticketId,
      @RequestParam Long agentId,
      @RequestParam(required = false) String resolution) {
    try {
      TicketDTO resolvedTicket = ticketService.resolveTicket(ticketId, agentId, resolution);
      return ResponseEntity.ok(resolvedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  private Agent resolveAuthenticatedAgent() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("Authentication required");
    }

    String email;
    Object principal = authentication.getPrincipal();
    if (principal instanceof String) {
      email = (String) principal;
    } else if (principal
        instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
      email = userDetails.getUsername();
    } else {
      email = authentication.getName();
    }

    if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
      throw new RuntimeException("Authenticated agent not found");
    }

    return userRepository
        .findByEmail(email)
        .flatMap(user -> agentRepository.findDetailedByUserId(user.getId()))
        .orElseThrow(() -> new RuntimeException("Authenticated agent profile not found"));
  }
}
