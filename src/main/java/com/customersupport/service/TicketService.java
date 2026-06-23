package com.customersupport.service;

import com.customersupport.dto.AgentTicketBoardDTO;
import com.customersupport.dto.TicketCreateDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.entity.Agent;
import com.customersupport.entity.Category;
import com.customersupport.entity.Company;
import com.customersupport.entity.Customer;
import com.customersupport.entity.Ticket;
import com.customersupport.repository.CategoryRepository;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.CustomerRepository;
import com.customersupport.repository.TicketRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TicketService {

  @Autowired private TicketRepository ticketRepository;

  @Autowired private CustomerRepository customerRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private com.customersupport.repository.AgentRepository agentRepository;

  @Autowired private EmailService emailService;

  private static final EnumSet<Ticket.Status> ACTIVE_STATUSES =
      EnumSet.of(Ticket.Status.OPEN, Ticket.Status.IN_PROGRESS, Ticket.Status.PENDING_CUSTOMER);

  private static final EnumSet<Ticket.Status> CLOSED_STATUSES =
      EnumSet.of(Ticket.Status.RESOLVED, Ticket.Status.CLOSED, Ticket.Status.CANCELLED);

  // Get tickets by company with optional filters
  public List<TicketDTO> getTicketsByCompany(Long companyId) {
    return ticketRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
        .map(TicketDTO::fromEntity)
        .collect(Collectors.toList());
  }

  public List<TicketDTO> getTicketsByCompany(Long companyId, Ticket.Status status, String keyword) {
    List<Ticket> tickets;

    String normalizedKeyword = null;
    if (keyword != null) {
      normalizedKeyword = keyword.trim();
    }
    boolean hasKeyword = normalizedKeyword != null && !normalizedKeyword.isEmpty();

    if (status != null && hasKeyword) {
      tickets =
          ticketRepository.searchTicketByCompany(companyId, normalizedKeyword).stream()
              .filter(ticket -> ticket.getStatus() == status)
              .sorted(Comparator.comparing(Ticket::getCreatedAt).reversed())
              .collect(Collectors.toList());
    } else if (status != null) {
      tickets =
          ticketRepository.findByCompanyIdAndStatus(companyId, status).stream()
              .sorted(Comparator.comparing(Ticket::getCreatedAt).reversed())
              .collect(Collectors.toList());
    } else if (hasKeyword) {
      tickets =
          ticketRepository.searchTicketByCompany(companyId, normalizedKeyword).stream()
              .sorted(Comparator.comparing(Ticket::getCreatedAt).reversed())
              .collect(Collectors.toList());
    } else {
      tickets = ticketRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    return tickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());
  }

  public long countTicketsByCompany(Long companyId) {
    Long count = ticketRepository.countByCompanyId(companyId);
    return count != null ? count : 0L;
  }

  public long countTicketsByCompanyAndStatus(Long companyId, Ticket.Status status) {
    Long count = ticketRepository.countByCompanyIdAndStatus(companyId, status);
    return count != null ? count : 0L;
  }

  public Map<Ticket.Status, Long> getTicketStatusBreakdown(Long companyId) {
    Map<Ticket.Status, Long> breakdown = new EnumMap<>(Ticket.Status.class);
    for (Ticket.Status status : Ticket.Status.values()) {
      breakdown.put(status, countTicketsByCompanyAndStatus(companyId, status));
    }
    return breakdown;
  }

  // Create a new ticket
  public TicketDTO createTicket(TicketCreateDTO ticketCreateDTO) {
    if (ticketCreateDTO.getCustomerId() == null) {
      throw new RuntimeException("Customer information is required to create a ticket");
    }

    Customer customer =
        customerRepository
            .findById(ticketCreateDTO.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    Company company;
    if (ticketCreateDTO.getCompanyId() != null) {
      company =
          companyRepository
              .findById(ticketCreateDTO.getCompanyId())
              .orElseThrow(() -> new RuntimeException("Company not found"));
    } else {
      company = customer.getCompany();
    }

    // Verify customer belongs to company
    if (!customer.getCompany().getId().equals(company.getId())) {
      throw new RuntimeException("Customer does not belong to the specified company");
    }

    String title = ticketCreateDTO.getTitle() != null ? ticketCreateDTO.getTitle().trim() : null;
    String description =
        ticketCreateDTO.getDescription() != null ? ticketCreateDTO.getDescription().trim() : null;

    if (title == null || title.isEmpty()) {
      throw new RuntimeException("Ticket title is required");
    }

    if (description == null || description.isEmpty()) {
      throw new RuntimeException("Ticket description is required");
    }

    Ticket ticket = new Ticket();
    ticket.setCustomer(customer);
    ticket.setCompany(company);
    ticket.setTitle(title);
    ticket.setDescription(description);
    ticket.setPriority(
        Optional.ofNullable(ticketCreateDTO.getPriority()).orElse(Ticket.Priority.MEDIUM));
    ticket.setSource(Optional.ofNullable(ticketCreateDTO.getSource()).orElse(Ticket.Source.WEB));
    ticket.setTags(ticketCreateDTO.getTags());
    ticket.setAttachment(ticketCreateDTO.getAttachments());
    ticket.setTicketNumber(generateUniqueTicketNumber(company.getId()));

    // Set category if provided
    if (ticketCreateDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(ticketCreateDTO.getCategoryId())
              .orElseThrow(() -> new RuntimeException("Category not found"));

      if (!category.getCompany().getId().equals(company.getId())) {
        throw new RuntimeException("Selected category does not belong to your company");
      }

      ticket.setCategory(category);
    }

    Ticket savedTicket = ticketRepository.save(ticket);

    // Update customer's ticket count
    customer.setTotalTickets(customer.getTotalTickets() + 1);
    customerRepository.save(customer);

    // Send email notifications to all agents of the company
    TicketDTO ticketDTO = TicketDTO.fromEntity(savedTicket);
    String customerName =
        customer.getUser().getFirstname() + " " + customer.getUser().getLastname();

    // Get all agents for the company
    List<Agent> companyAgents = agentRepository.findByCompanyId(company.getId());

    // Send email notification to each agent
    for (Agent agent : companyAgents) {
      String agentName = agent.getUser().getFirstname() + " " + agent.getUser().getLastname();
      emailService.sendNewTicketNotification(
          agent.getUser().getEmail(), agentName, ticketDTO, customerName);
    }

    return ticketDTO;
  }

  // Get ticket by ID
  public Optional<TicketDTO> getTicketById(Long id) {
    return ticketRepository.findById(id).map(TicketDTO::fromEntity);
  }

  // Get tickets by customer
  public List<TicketDTO> getTicketsByCustomer(Long customerId) {
    return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(TicketDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get tickets by customer and status
  public List<TicketDTO> getTicketsByCustomerAndStatus(Long customerId, Ticket.Status status) {
    return ticketRepository.findByCustomerIdAndStatus(customerId, status).stream()
        .map(TicketDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Search tickets by customer
  public List<TicketDTO> searchTicketsByCustomer(Long customerId, String keyword) {
    return ticketRepository.searchTicketsByCustomer(customerId, keyword).stream()
        .map(TicketDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Update ticket (customer can only update if status is OPEN and no agent assigned)
  public TicketDTO updateTicket(Long id, TicketCreateDTO ticketUpdateDTO, Long customerId) {
    Ticket ticket =
        ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));

    if (!ticket.getCustomer().getId().equals(customerId)) {
      throw new RuntimeException("You do not have permission to update this ticket");
    }

    if (!ticket.canBeEditedByCustomer()) {
      throw new RuntimeException(
          "Tickets can only be edited while they are OPEN and not yet assigned to an agent");
    }

    String title = ticketUpdateDTO.getTitle() != null ? ticketUpdateDTO.getTitle().trim() : null;
    String description =
        ticketUpdateDTO.getDescription() != null ? ticketUpdateDTO.getDescription().trim() : null;

    if (title == null || title.isEmpty()) {
      throw new RuntimeException("Ticket title is required");
    }

    if (description == null || description.isEmpty()) {
      throw new RuntimeException("Ticket description is required");
    }

    ticket.setTitle(title);
    ticket.setDescription(description);
    ticket.setPriority(
        Optional.ofNullable(ticketUpdateDTO.getPriority()).orElse(Ticket.Priority.MEDIUM));
    ticket.setTags(ticketUpdateDTO.getTags());

    if (ticketUpdateDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(ticketUpdateDTO.getCategoryId())
              .orElseThrow(() -> new RuntimeException("Category not found"));

      if (!category.getCompany().getId().equals(ticket.getCompany().getId())) {
        throw new RuntimeException("Selected category does not belong to your company");
      }

      ticket.setCategory(category);
    } else {
      ticket.setCategory(null);
    }

    if (ticketUpdateDTO.getAttachments() != null && !ticketUpdateDTO.getAttachments().isEmpty()) {
      ticket.setAttachment(ticketUpdateDTO.getAttachments());
    }

    Ticket updatedTicket = ticketRepository.save(ticket);
    return TicketDTO.fromEntity(updatedTicket);
  }

  public TicketDTO getTicketForCustomer(Long ticketId, Long customerId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    if (!ticket.getCustomer().getId().equals(customerId)) {
      throw new RuntimeException("You do not have permission to view this ticket");
    }

    return TicketDTO.fromEntity(ticket);
  }

  public void deleteTicket(Long ticketId, Long customerId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    if (!ticket.getCustomer().getId().equals(customerId)) {
      throw new RuntimeException("You do not have permission to delete this ticket");
    }

    if (ticket.getStatus() != Ticket.Status.OPEN) {
      throw new RuntimeException("Only OPEN tickets can be deleted");
    }

    if (ticket.getAgent() != null) {
      throw new RuntimeException(
          "This ticket has already been assigned to an agent and cannot be deleted");
    }

    Customer customer = ticket.getCustomer();

    ticketRepository.delete(ticket);

    if (customer.getTotalTickets() > 0) {
      customer.setTotalTickets(customer.getTotalTickets() - 1);
      customerRepository.save(customer);
    }
  }

  // Cancel ticket (customer can only cancel if status is OPEN or IN_PROGRESS)
  public void cancelTicket(Long id, Long customerId) {
    Ticket ticket =
        ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Verify this ticket belongs to the customer
    if (!ticket.getCustomer().getId().equals(customerId)) {
      throw new RuntimeException("You do not have permission to cancel this ticket");
    }

    // Verify ticket can be cancelled by customer
    if (!ticket.canBeCancelledByCustomer()) {
      throw new RuntimeException(
          "This ticket cannot be cancelled. It is already resolved or closed");
    }

    ticket.setStatus(Ticket.Status.CANCELLED);
    ticket.setClosedAt(LocalDateTime.now());
    ticketRepository.save(ticket);
  }

  // Get ticket by ticket number
  public Optional<TicketDTO> getTicketByTicketNumber(String ticketNumber) {
    return ticketRepository.findByTicketNumber(ticketNumber).map(TicketDTO::fromEntity);
  }

  // Get ticket count by customer and status
  public Long getTicketCountByCustomerAndStatus(Long customerId, Ticket.Status status) {
    return ticketRepository.countByCustomerIdAndStatus(customerId, status);
  }

  // Get tickets by agent
  public List<TicketDTO> getTicketsByAgent(Long agentId) {
    List<Ticket> tickets = ticketRepository.findByAgentId(agentId);
    return tickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());
  }

  public AgentTicketBoardDTO getAgentTicketBoard(Long agentId) {
    Agent agent =
        agentRepository
            .findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));

    Long companyId = agent.getCompany().getId();
    List<TicketDTO> companyTickets =
        ticketRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
            .map(TicketDTO::fromEntity)
            .collect(Collectors.toList());

    List<TicketDTO> assignedTickets =
        companyTickets.stream()
            .filter(
                ticket ->
                    agentId.equals(ticket.getAgentId())
                        && ACTIVE_STATUSES.contains(ticket.getStatus()))
            .collect(Collectors.toList());

    List<TicketDTO> availableTickets =
        companyTickets.stream()
            .filter(
                ticket -> ticket.getAgentId() == null && ticket.getStatus() == Ticket.Status.OPEN)
            .collect(Collectors.toList());

    List<TicketDTO> activeCompanyTickets =
        companyTickets.stream()
            .filter(ticket -> ACTIVE_STATUSES.contains(ticket.getStatus()))
            .collect(Collectors.toList());

    List<TicketDTO> closedCompanyTickets =
        companyTickets.stream()
            .filter(ticket -> CLOSED_STATUSES.contains(ticket.getStatus()))
            .collect(Collectors.toList());

    LocalDate today = LocalDate.now();

    long resolvedToday =
        assignedTickets.stream()
            .filter(
                ticket ->
                    ticket.getStatus() == Ticket.Status.RESOLVED
                        && ticket.getUpdatedAt() != null
                        && ticket.getUpdatedAt().toLocalDate().isEqual(today))
            .count();

    Double averageResolutionHours =
        Optional.ofNullable(agent.getAverageResolutionTime())
            .map(time -> time.doubleValue())
            .orElse(null);

    Double satisfactionRating =
        Optional.ofNullable(agent.getCustomerSatisfactionRating())
            .map(rating -> rating.doubleValue())
            .orElse(null);

    AgentTicketBoardDTO.AgentStats stats =
        AgentTicketBoardDTO.AgentStats.builder()
            .assignedCount(assignedTickets.size())
            .availableCount(availableTickets.size())
            .activeCount(activeCompanyTickets.size())
            .closedCount(closedCompanyTickets.size())
            .resolvedToday(resolvedToday)
            .averageResolutionHours(averageResolutionHours)
            .satisfactionRating(satisfactionRating)
            .build();

    AgentTicketBoardDTO.AgentSummary agentSummary =
        AgentTicketBoardDTO.AgentSummary.builder()
            .agentId(agent.getId())
            .agentUserId(agent.getUser().getId())
            .companyId(companyId)
            .agentName(agent.getUser().getFirstname() + " " + agent.getUser().getLastname())
            .companyName(agent.getCompany().getName())
            .maxConcurrentTickets(agent.getMaxConcurrentTickets())
            .currentTicketCount(Optional.ofNullable(agent.getCurrentTicketCount()).orElse(0))
            .build();

    return AgentTicketBoardDTO.builder()
        .assignedTickets(assignedTickets)
        .availableTickets(availableTickets)
        .activeCompanyTickets(activeCompanyTickets)
        .closedCompanyTickets(closedCompanyTickets)
        .stats(stats)
        .agent(agentSummary)
        .build();
  }

  // Get tickets by agent and status
  public List<TicketDTO> getTicketsByAgentAndStatus(Long agentId, Ticket.Status status) {
    List<Ticket> tickets = ticketRepository.findByAgentIdAndStatus(agentId, status);
    return tickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());
  }

  // Update ticket status by agent
  public TicketDTO updateTicketStatus(Long ticketId, Ticket.Status status, Long agentId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Verify agent is assigned to this ticket
    if (ticket.getAgent() == null || !ticket.getAgent().getId().equals(agentId)) {
      throw new RuntimeException("You are not assigned to this ticket");
    }

    Ticket.Status previousStatus = ticket.getStatus();
    ticket.setStatus(status);
    ticket.setLastActivity(LocalDateTime.now());

    if (status == Ticket.Status.RESOLVED || status == Ticket.Status.CLOSED) {
      ticket.setActualResolutionTime(LocalDateTime.now());
      if (ticket.getClosedAt() == null) {
        ticket.setClosedAt(LocalDateTime.now());
      }
    }

    Ticket updatedTicket = ticketRepository.save(ticket);
    updateAgentTicketCountOnStatusChange(ticket, previousStatus, status);
    return TicketDTO.fromEntity(updatedTicket);
  }

  // Update ticket priority
  public TicketDTO updateTicketPriority(Long ticketId, Ticket.Priority priority, Long agentId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Verify agent is assigned to this ticket
    if (ticket.getAgent() == null || !ticket.getAgent().getId().equals(agentId)) {
      throw new RuntimeException("You are not assigned to this ticket");
    }

    ticket.setPriority(priority);
    ticket.setLastActivity(LocalDateTime.now());

    Ticket updatedTicket = ticketRepository.save(ticket);
    return TicketDTO.fromEntity(updatedTicket);
  }

  // Assign ticket to agent
  public TicketDTO assignTicketToAgent(Long ticketId, Long agentId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    com.customersupport.entity.Agent agent =
        agentRepository
            .findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));

    // Verify agent belongs to the same company as the ticket
    if (!agent.getCompany().getId().equals(ticket.getCompany().getId())) {
      throw new RuntimeException("Agent does not belong to the same company as the ticket");
    }

    Agent previousAgent = ticket.getAgent();
    Ticket.Status previousStatus = ticket.getStatus();

    validateAgentCapacity(agent);

    ticket.setAgent(agent);
    ticket.setStatus(Ticket.Status.IN_PROGRESS);
    ticket.setLastActivity(LocalDateTime.now());

    boolean agentChanged = previousAgent == null || !previousAgent.getId().equals(agent.getId());
    adjustAgentAssignmentCounts(previousAgent, agent);
    if (!agentChanged) {
      updateAgentTicketCountOnStatusChange(ticket, previousStatus, ticket.getStatus());
    }

    Ticket updatedTicket = ticketRepository.save(ticket);
    TicketDTO ticketDTO = TicketDTO.fromEntity(updatedTicket);

    // Send email notification to the customer when agent is assigned by admin
    if (agentChanged) {
      Customer customer = ticket.getCustomer();
      String customerName =
          customer.getUser().getFirstname() + " " + customer.getUser().getLastname();
      String agentName = agent.getUser().getFirstname() + " " + agent.getUser().getLastname();

      emailService.sendTicketClaimedNotification(
          customer.getUser().getEmail(), customerName, ticketDTO, agentName);
    }

    return ticketDTO;
  }

  public TicketDTO claimTicket(Long ticketId, Long agentId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    if (ticket.getAgent() != null) {
      if (ticket.getAgent().getId().equals(agentId)) {
        if (ticket.getStatus() != Ticket.Status.OPEN) {
          throw new RuntimeException("Ticket is already claimed");
        }
      } else {
        throw new RuntimeException("Ticket has already been claimed by another agent");
      }
    }

    if (ticket.getStatus() != Ticket.Status.OPEN) {
      throw new RuntimeException("Only OPEN tickets can be claimed");
    }

    Agent agent =
        agentRepository
            .findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));

    if (!agent.getCompany().getId().equals(ticket.getCompany().getId())) {
      throw new RuntimeException("You cannot claim tickets from another company");
    }

    validateAgentCapacity(agent);

    Agent previousAgent = ticket.getAgent();
    Ticket.Status previousStatus = ticket.getStatus();

    ticket.setAgent(agent);
    ticket.setStatus(Ticket.Status.IN_PROGRESS);
    ticket.setLastActivity(LocalDateTime.now());

    boolean agentChanged = previousAgent == null || !previousAgent.getId().equals(agent.getId());
    adjustAgentAssignmentCounts(previousAgent, agent);
    if (!agentChanged) {
      updateAgentTicketCountOnStatusChange(ticket, previousStatus, ticket.getStatus());
    }

    Ticket updatedTicket = ticketRepository.save(ticket);
    TicketDTO ticketDTO = TicketDTO.fromEntity(updatedTicket);

    // Send email notification to the customer
    Customer customer = ticket.getCustomer();
    String customerName =
        customer.getUser().getFirstname() + " " + customer.getUser().getLastname();
    String agentName = agent.getUser().getFirstname() + " " + agent.getUser().getLastname();

    emailService.sendTicketClaimedNotification(
        customer.getUser().getEmail(), customerName, ticketDTO, agentName);

    return ticketDTO;
  }

  // Resolve ticket
  public TicketDTO resolveTicket(Long ticketId, Long agentId, String resolution) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Verify agent is assigned to this ticket
    if (ticket.getAgent() == null || !ticket.getAgent().getId().equals(agentId)) {
      throw new RuntimeException("You are not assigned to this ticket");
    }

    Ticket.Status previousStatus = ticket.getStatus();

    ticket.setStatus(Ticket.Status.RESOLVED);
    ticket.setResolution(resolution);
    ticket.setActualResolutionTime(LocalDateTime.now());
    ticket.setLastActivity(LocalDateTime.now());

    Ticket updatedTicket = ticketRepository.save(ticket);
    updateAgentTicketCountOnStatusChange(ticket, previousStatus, Ticket.Status.RESOLVED);
    return TicketDTO.fromEntity(updatedTicket);
  }

  private String generateUniqueTicketNumber(Long companyId) {
    int attempts = 0;
    while (attempts < 5) {
      String candidate = buildTicketNumber(companyId);
      if (!ticketRepository.existsByTicketNumber(candidate)) {
        return candidate;
      }
      attempts++;
    }
    throw new RuntimeException("Unable to generate a unique ticket number. Please try again.");
  }

  private String buildTicketNumber(Long companyId) {
    String companySegment =
        companyId != null ? String.format("%02d", Math.abs(companyId % 100)) : "00";
    String timestampSegment =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    int random = ThreadLocalRandom.current().nextInt(0, 1000);
    return String.format("T%s%s%03d", companySegment, timestampSegment, random);
  }

  private void adjustAgentAssignmentCounts(Agent previousAgent, Agent newAgent) {
    if (previousAgent != null
        && (newAgent == null || !previousAgent.getId().equals(newAgent.getId()))) {
      decrementAgentTicketCount(previousAgent);
    }

    if (newAgent != null
        && (previousAgent == null || !previousAgent.getId().equals(newAgent.getId()))) {
      incrementAgentTicketCount(newAgent);
    }
  }

  private void updateAgentTicketCountOnStatusChange(
      Ticket ticket, Ticket.Status previousStatus, Ticket.Status newStatus) {
    Agent agent = ticket.getAgent();
    if (agent == null) {
      return;
    }

    boolean wasActive = ACTIVE_STATUSES.contains(previousStatus);
    boolean isActive = ACTIVE_STATUSES.contains(newStatus);

    if (wasActive && !isActive) {
      decrementAgentTicketCount(agent);
    } else if (!wasActive && isActive) {
      incrementAgentTicketCount(agent);
    }
  }

  private void incrementAgentTicketCount(Agent agent) {
    int currentCount = Optional.ofNullable(agent.getCurrentTicketCount()).orElse(0);
    agent.setCurrentTicketCount(currentCount + 1);
    agentRepository.save(agent);
  }

  private void decrementAgentTicketCount(Agent agent) {
    int currentCount = Optional.ofNullable(agent.getCurrentTicketCount()).orElse(0);
    if (currentCount > 0) {
      agent.setCurrentTicketCount(currentCount - 1);
      agentRepository.save(agent);
    }
  }

  private void validateAgentCapacity(Agent agent) {
    Integer maxConcurrent = agent.getMaxConcurrentTickets();
    if (maxConcurrent == null || maxConcurrent <= 0) {
      return;
    }

    int currentCount = Optional.ofNullable(agent.getCurrentTicketCount()).orElse(0);
    if (currentCount >= maxConcurrent) {
      throw new RuntimeException("You have reached your maximum concurrent ticket limit");
    }
  }
}
