package com.customersupport.controller.web;

import com.customersupport.dto.AgentTicketBoardDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.entity.Agent;
import com.customersupport.repository.AgentRepository;
import com.customersupport.repository.UserRepository;
import com.customersupport.service.TicketService;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agent")
public class AgentWebController {

  @Autowired private TicketService ticketService;

  @Autowired private UserRepository userRepository;

  @Autowired private AgentRepository agentRepository;

  @GetMapping("/ticket-queue")
  public String ticketQueuePage(Model model) {
    try {
      Agent agent = resolveAuthenticatedAgent();
      AgentTicketBoardDTO ticketBoard = ticketService.getAgentTicketBoard(agent.getId());

      // Add ticket data to the model
      model.addAttribute("assignedTickets", ticketBoard.getAssignedTickets());
      model.addAttribute("unassignedTickets", ticketBoard.getAvailableTickets());
      model.addAttribute(
          "allTickets",
          combineTicketLists(
              ticketBoard.getActiveCompanyTickets(), ticketBoard.getClosedCompanyTickets()));

      // Add agent info
      model.addAttribute("agent", ticketBoard.getAgent());

      // Add stats
      model.addAttribute("stats", ticketBoard.getStats());

      return "agent/ticket-queue";
    } catch (Exception e) {
      model.addAttribute("error", "Error loading ticket queue: " + e.getMessage());
      model.addAttribute("assignedTickets", Collections.emptyList());
      model.addAttribute("unassignedTickets", Collections.emptyList());
      model.addAttribute("allTickets", Collections.emptyList());
      return "agent/ticket-queue";
    }
  }

  private List<TicketDTO> combineTicketLists(List<TicketDTO> list1, List<TicketDTO> list2) {
    // Combine two lists and return the result
    List<TicketDTO> combined = list1 != null ? list1 : Collections.emptyList();
    if (list2 != null && !list2.isEmpty()) {
      combined.addAll(list2);
    }
    return combined;
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
