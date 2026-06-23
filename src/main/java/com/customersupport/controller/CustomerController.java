package com.customersupport.controller;

import com.customersupport.dto.CustomerDTO;
import com.customersupport.dto.CustomerRegistrationDTO;
import com.customersupport.dto.TicketCreateDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.dto.TicketResponseDTO;
import com.customersupport.entity.TicketResponse;
import com.customersupport.entity.User;
import com.customersupport.repository.UserRepository;
import com.customersupport.service.CustomerService;
import com.customersupport.service.TicketResponseService;
import com.customersupport.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

  @Autowired private CustomerService customerService;

  @Autowired private TicketService ticketService;

  @Autowired private UserRepository userRepository;

  @Autowired private TicketResponseService ticketResponseService;

  @PostMapping("/register")
  public ResponseEntity<?> registerCustomer(
      @Valid @RequestBody CustomerRegistrationDTO registrationDTO) {
    try {
      CustomerDTO registeredCustomer = customerService.registerCustomer(registrationDTO);
      return ResponseEntity.ok(registeredCustomer);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
    return customerService
        .getCustomerById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateCustomer(
      @PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
    try {
      CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
      return ResponseEntity.ok(updatedCustomer);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{id}/update-profile")
  public ResponseEntity<?> updateCustomerProfile(
      @PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
    try {
      // This endpoint is specifically for customers to update their own profile
      // Typically has more limited permissions than the admin version
      CustomerDTO updatedCustomer = customerService.updateCustomerProfile(id, customerDTO);
      return ResponseEntity.ok(updatedCustomer);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/by-user/{userId}")
  public ResponseEntity<CustomerDTO> getCustomerByUserId(@PathVariable Long userId) {
    return customerService
        .getCustomerByUserId(userId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/verify-email/{token}")
  public ResponseEntity<?> verifyEmail(@PathVariable String token) {
    try {
      customerService.verifyEmail(token);
      return ResponseEntity.ok().body("Email verified successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Ticket management endpoints for customers
  @PostMapping("/tickets")
  public ResponseEntity<?> createTicket(
      @Valid @RequestBody TicketCreateDTO ticketCreateDTO, BindingResult bindingResult) {
    try {
      if (bindingResult.hasErrors()) {
        List<String> errors =
            bindingResult.getAllErrors().stream()
                .map(
                    error ->
                        error.getDefaultMessage() != null
                            ? error.getDefaultMessage()
                            : error.toString())
                .toList();
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
      }

      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      ticketCreateDTO.setCustomerId(authenticatedCustomer.getId());
      ticketCreateDTO.setCompanyId(authenticatedCustomer.getCompanyId());

      TicketDTO createdTicket = ticketService.createTicket(ticketCreateDTO);
      return ResponseEntity.ok(createdTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/tickets")
  public ResponseEntity<?> getCustomerTickets() {
    try {
      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      List<TicketDTO> tickets = ticketService.getTicketsByCustomer(authenticatedCustomer.getId());
      return ResponseEntity.ok(tickets);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/tickets/{id}")
  public ResponseEntity<?> getTicketById(@PathVariable Long id) {
    try {
      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      TicketDTO ticket = ticketService.getTicketForCustomer(id, authenticatedCustomer.getId());
      return ResponseEntity.ok(ticket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/tickets/{id}/responses")
  public ResponseEntity<?> getTicketResponses(@PathVariable Long id) {
    try {
      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      ticketService.getTicketForCustomer(id, authenticatedCustomer.getId());

      List<TicketResponseDTO> responses =
          ticketResponseService.getResponsesByTicket(id).stream()
              .filter(
                  response ->
                      Boolean.TRUE.equals(response.getIsPublic())
                          || response.getUserId().equals(authenticatedCustomer.getUserId()))
              .toList();

      return ResponseEntity.ok(responses);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/tickets/{id}/responses")
  public ResponseEntity<?> addTicketResponse(
      @PathVariable Long id, @Valid @RequestBody TicketResponseDTO responseDTO) {
    try {
      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      TicketDTO ticket = ticketService.getTicketForCustomer(id, authenticatedCustomer.getId());
      if (!ticket.getCustomerId().equals(authenticatedCustomer.getId())) {
        throw new RuntimeException("You are not authorized to respond to this ticket");
      }

      responseDTO.setTicketId(id);
      responseDTO.setUserId(authenticatedCustomer.getUserId());
      responseDTO.setResponseType(TicketResponse.ResponseType.CUSTOMER_REPLY);
      responseDTO.setIsPublic(
          responseDTO.getIsPublic() == null ? Boolean.TRUE : responseDTO.getIsPublic());

      TicketResponseDTO createdResponse = ticketResponseService.addResponse(id, responseDTO);
      return ResponseEntity.ok(createdResponse);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/tickets/{id}")
  public ResponseEntity<?> updateTicket(
      @PathVariable Long id,
      @Valid @RequestBody TicketCreateDTO ticketDTO,
      BindingResult bindingResult) {
    try {
      if (bindingResult.hasErrors()) {
        List<String> errors =
            bindingResult.getAllErrors().stream()
                .map(
                    error ->
                        error.getDefaultMessage() != null
                            ? error.getDefaultMessage()
                            : error.toString())
                .toList();
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
      }

      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      TicketDTO updatedTicket =
          ticketService.updateTicket(id, ticketDTO, authenticatedCustomer.getId());
      return ResponseEntity.ok(updatedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/tickets/{id}")
  public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
    try {
      CustomerDTO authenticatedCustomer =
          resolveAuthenticatedCustomer()
              .orElseThrow(() -> new RuntimeException("Authenticated customer profile not found"));

      ticketService.deleteTicket(id, authenticatedCustomer.getId());
      return ResponseEntity.ok(Map.of("message", "Ticket deleted successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  private Optional<CustomerDTO> resolveAuthenticatedCustomer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();
    String email;

    if (principal instanceof String) {
      email = (String) principal;
    } else if (principal
        instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
      email = userDetails.getUsername();
    } else {
      email = authentication.getName();
    }

    if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
      return Optional.empty();
    }

    Optional<User> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
      return Optional.empty();
    }

    return customerService.getCustomerByUserId(userOptional.get().getId());
  }
}
