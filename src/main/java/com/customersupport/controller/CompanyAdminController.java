package com.customersupport.controller;

import com.customersupport.dto.AgentDTO;
import com.customersupport.dto.AgentRegistrationDTO;
import com.customersupport.dto.CompanyPerformanceReportDTO;
import com.customersupport.dto.CustomerDTO;
import com.customersupport.dto.CustomerRegistrationDTO;
import com.customersupport.dto.ReviewAnalyticsDTO;
import com.customersupport.dto.ReviewDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.dto.TicketResponseDTO;
import com.customersupport.dto.UserDTO;
import com.customersupport.entity.Ticket;
import com.customersupport.service.AgentService;
import com.customersupport.service.CompanyPerformanceService;
import com.customersupport.service.CustomerService;
import com.customersupport.service.ReviewService;
import com.customersupport.service.TicketResponseService;
import com.customersupport.service.TicketService;
import com.customersupport.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-admin")
@CrossOrigin(origins = "*")
public class CompanyAdminController {

  @Autowired private UserService userService;

  @Autowired private AgentService agentService;

  @Autowired private CustomerService customerService;

  @Autowired private TicketService ticketService;

  @Autowired private CompanyPerformanceService companyPerformanceService;

  @Autowired private ReviewService reviewService;

  @Autowired private TicketResponseService ticketResponseService;

  // User Management
  @GetMapping("/users")
  public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam Long companyId) {
    try {
      List<UserDTO> users = userService.getUsersByCompany(companyId);
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    try {
      return userService
          .getUserById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/users/search")
  public ResponseEntity<List<UserDTO>> searchUsers(
      @RequestParam Long companyId, @RequestParam String keyword) {
    try {
      List<UserDTO> users = userService.searchUsersByCompany(companyId, keyword);
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/users/role/{roleName}")
  public ResponseEntity<List<UserDTO>> getUsersByRole(
      @RequestParam Long companyId, @PathVariable String roleName) {
    try {
      List<UserDTO> users = userService.getUsersByCompanyAndRole(companyId, roleName);
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/users/{id}/activate")
  public ResponseEntity<?> activateUser(@PathVariable Long id) {
    try {
      userService.activateUser(id);
      return ResponseEntity.ok().body("User activated successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/users/{id}/deactivate")
  public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
    try {
      userService.deactivateUser(id);
      return ResponseEntity.ok().body("User deactivated successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    try {
      userService.deleteUser(id);
      return ResponseEntity.ok().body("User deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Agent Management
  @PostMapping("/agents/register")
  public ResponseEntity<?> registerAgent(@Valid @RequestBody AgentRegistrationDTO registrationDTO) {
    try {
      return ResponseEntity.ok(agentService.registerAgent(registrationDTO));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/agents")
  public ResponseEntity<List<AgentDTO>> getAllAgents(@RequestParam Long companyId) {
    try {
      List<AgentDTO> agents = agentService.getAgentsByCompany(companyId);
      return ResponseEntity.ok(agents);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/agents/{id}")
  public ResponseEntity<AgentDTO> getAgentById(@PathVariable Long id) {
    try {
      return agentService
          .getAgentById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/agents/{id}")
  public ResponseEntity<?> updateAgent(@PathVariable Long id, @RequestBody AgentDTO agentDTO) {
    try {
      AgentDTO updated = agentService.updateAgent(id, agentDTO);
      return ResponseEntity.ok(updated);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/agents/{id}")
  public ResponseEntity<?> deleteAgent(@PathVariable Long id) {
    try {
      agentService.deleteAgent(id);
      return ResponseEntity.ok().body("Agent deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Reset Password
  @PostMapping("/users/{id}/reset-password")
  public ResponseEntity<?> resetUserPassword(@PathVariable Long id) {
    try {
      String newPassword = userService.resetPassword(id);
      return ResponseEntity.ok()
          .body("Password reset successfully. Temporary password: " + newPassword);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Customer Management
  @PostMapping("/customers")
  public ResponseEntity<?> registerCustomer(
      @RequestParam Long companyId, @Valid @RequestBody CustomerRegistrationDTO registrationDTO) {
    try {
      registrationDTO.setId(companyId);
      CustomerDTO customer = customerService.registerCustomer(registrationDTO);
      return ResponseEntity.ok(customer);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/customers")
  public ResponseEntity<List<CustomerDTO>> getAllCustomers(@RequestParam Long companyId) {
    try {
      List<CustomerDTO> customers = customerService.getCustomersByCompany(companyId);
      return ResponseEntity.ok(customers);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/customers/{id}")
  public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
    try {
      return customerService
          .getCustomerById(id)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/customers/search")
  public ResponseEntity<List<CustomerDTO>> searchCustomers(
      @RequestParam Long companyId, @RequestParam String keyword) {
    try {
      List<CustomerDTO> customers = customerService.searchCustomersByCompany(companyId, keyword);
      return ResponseEntity.ok(customers);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/customers/{id}")
  public ResponseEntity<?> updateCustomer(
      @PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
    try {
      CustomerDTO updated = customerService.updateCustomer(id, customerDTO);
      return ResponseEntity.ok(updated);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/customers/{id}")
  public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
    try {
      customerService.deleteCustomer(id);
      return ResponseEntity.ok().body("Customer deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Ticket management
  @GetMapping("/tickets")
  public ResponseEntity<List<TicketDTO>> getCompanyTickets(
      @RequestParam Long companyId,
      @RequestParam(required = false) Ticket.Status status,
      @RequestParam(required = false) String search) {
    try {
      List<TicketDTO> tickets = ticketService.getTicketsByCompany(companyId, status, search);
      return ResponseEntity.ok(tickets);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/tickets/{ticketId}/responses")
  public ResponseEntity<List<TicketResponseDTO>> getTicketResponsesForCompanyAdmin(
      @PathVariable Long ticketId, @RequestParam Long companyId) {
    try {
      return ticketService
          .getTicketById(ticketId)
          .filter(ticket -> ticket.getCompanyId().equals(companyId))
          .map(ticket -> ResponseEntity.ok(ticketResponseService.getResponsesByTicket(ticketId)))
          .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/tickets/status-summary")
  public ResponseEntity<Map<String, Object>> getTicketStatusSummary(@RequestParam Long companyId) {
    try {
      Map<String, Object> summary = new HashMap<>();
      summary.put("totals", ticketService.getTicketStatusBreakdown(companyId));
      summary.put("totalTickets", ticketService.countTicketsByCompany(companyId));
      return ResponseEntity.ok(summary);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // Dashboard stats endpoint
  @GetMapping("/dashboard/stats")
  public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestParam Long companyId) {
    try {
      Map<String, Object> stats = new HashMap<>();

      long totalTickets = ticketService.countTicketsByCompany(companyId);
      stats.put("totalTickets", totalTickets);

      long openTickets =
          ticketService.countTicketsByCompanyAndStatus(companyId, Ticket.Status.OPEN)
              + ticketService.countTicketsByCompanyAndStatus(companyId, Ticket.Status.IN_PROGRESS);
      stats.put("openTickets", openTickets);

      stats.put(
          "resolvedTickets",
          ticketService.countTicketsByCompanyAndStatus(companyId, Ticket.Status.RESOLVED)
              + ticketService.countTicketsByCompanyAndStatus(companyId, Ticket.Status.CLOSED));

      List<AgentDTO> agents = agentService.getAgentsByCompany(companyId);
      stats.put("totalAgents", agents.size());

      List<CustomerDTO> customers = customerService.getCustomersByCompany(companyId);
      stats.put("totalCustomers", customers.size());

      stats.put("statusBreakdown", ticketService.getTicketStatusBreakdown(companyId));
      stats.put("averageRating", reviewService.getAverageRatingByCompany(companyId));

      return ResponseEntity.ok(stats);
    } catch (RuntimeException e) {
      Map<String, Object> errorStats = new HashMap<>();
      errorStats.put("totalTickets", 0);
      errorStats.put("openTickets", 0);
      errorStats.put("totalAgents", 0);
      errorStats.put("totalCustomers", 0);
      return ResponseEntity.ok(errorStats);
    }
  }

  // Review management
  @GetMapping("/reviews")
  public ResponseEntity<List<ReviewDTO>> getCompanyReviews(
      @RequestParam Long companyId,
      @RequestParam(required = false) Long agentId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    try {
      List<ReviewDTO> reviews =
          reviewService.getReviewsByCompanyWithFilters(companyId, agentId, startDate, endDate);
      return ResponseEntity.ok(reviews);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/reviews/analytics")
  public ResponseEntity<ReviewAnalyticsDTO> getReviewAnalytics(
      @RequestParam Long companyId,
      @RequestParam(required = false) Long agentId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    try {
      List<ReviewDTO> reviews =
          reviewService.getReviewsByCompanyWithFilters(companyId, agentId, startDate, endDate);
      ReviewAnalyticsDTO analytics = reviewService.calculateAnalytics(reviews);
      analytics.setRecentReviews(
          reviews.stream()
              .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
              .limit(10)
              .collect(Collectors.toList()));
      return ResponseEntity.ok(analytics);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/performance")
  public ResponseEntity<CompanyPerformanceReportDTO> getCompanyPerformance(
      @RequestParam Long companyId) {
    try {
      CompanyPerformanceReportDTO report =
          companyPerformanceService.buildPerformanceReport(companyId);
      return ResponseEntity.ok(report);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/performance/pdf")
  public ResponseEntity<byte[]> downloadCompanyPerformance(@RequestParam Long companyId) {
    try {
      CompanyPerformanceService.PerformancePdf pdf =
          companyPerformanceService.generatePerformancePdf(companyId);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(
          ContentDisposition.attachment().filename(pdf.filename()).build());
      headers.setContentLength(pdf.content().length);

      return ResponseEntity.ok().headers(headers).body(pdf.content());
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
