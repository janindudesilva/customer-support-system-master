package com.customersupport.controller;

import com.customersupport.dto.TicketCreateDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.entity.Ticket;
import com.customersupport.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

  @Autowired private TicketService ticketService;

  // Create a new ticket
  @PostMapping
  public ResponseEntity<?> createTicket(@Valid @RequestBody TicketCreateDTO ticketCreateDTO) {
    try {
      TicketDTO createdTicket = ticketService.createTicket(ticketCreateDTO);
      return ResponseEntity.ok(createdTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get ticket by ID
  @GetMapping("/{id}")
  public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
    return ticketService
        .getTicketById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get ticket by ticket number
  @GetMapping("/number/{ticketNumber}")
  public ResponseEntity<TicketDTO> getTicketByNumber(@PathVariable String ticketNumber) {
    return ticketService
        .getTicketByTicketNumber(ticketNumber)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get tickets by customer
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<TicketDTO>> getTicketsByCustomer(@PathVariable Long customerId) {
    List<TicketDTO> tickets = ticketService.getTicketsByCustomer(customerId);
    return ResponseEntity.ok(tickets);
  }

  // Get tickets by customer and status
  @GetMapping("/customer/{customerId}/status/{status}")
  public ResponseEntity<List<TicketDTO>> getTicketsByCustomerAndStatus(
      @PathVariable Long customerId, @PathVariable Ticket.Status status) {
    List<TicketDTO> tickets = ticketService.getTicketsByCustomerAndStatus(customerId, status);
    return ResponseEntity.ok(tickets);
  }

  // Search tickets by customer
  @GetMapping("/customer/{customerId}/search")
  public ResponseEntity<List<TicketDTO>> searchTicketsByCustomer(
      @PathVariable Long customerId, @RequestParam String keyword) {
    List<TicketDTO> tickets = ticketService.searchTicketsByCustomer(customerId, keyword);
    return ResponseEntity.ok(tickets);
  }

  // Update ticket (customer can only update if status is OPEN and no agent assigned)
  @PutMapping("/{id}/customer/{customerId}")
  public ResponseEntity<?> updateTicket(
      @PathVariable Long id,
      @PathVariable Long customerId,
      @Valid @RequestBody TicketCreateDTO ticketUpdateDTO) {
    try {
      TicketDTO updatedTicket = ticketService.updateTicket(id, ticketUpdateDTO, customerId);
      return ResponseEntity.ok(updatedTicket);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Cancel ticket (customer can only cancel if status is OPEN or IN_PROGRESS)
  @PutMapping("/{id}/cancel/customer/{customerId}")
  public ResponseEntity<?> cancelTicket(@PathVariable Long id, @PathVariable Long customerId) {
    try {
      ticketService.cancelTicket(id, customerId);
      return ResponseEntity.ok().body("Ticket cancelled successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get ticket count by customer and status
  @GetMapping("/customer/{customerId}/count/status/{status}")
  public ResponseEntity<Long> getTicketCountByCustomerAndStatus(
      @PathVariable Long customerId, @PathVariable Ticket.Status status) {
    Long count = ticketService.getTicketCountByCustomerAndStatus(customerId, status);
    return ResponseEntity.ok(count);
  }
}
