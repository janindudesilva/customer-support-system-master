package com.customersupport.service;

import com.customersupport.dto.AgentDTO;
import com.customersupport.dto.AgentPerformanceDTO;
import com.customersupport.dto.AgentRegistrationDTO;
import com.customersupport.dto.ReviewDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.entity.Agent;
import com.customersupport.entity.Company;
import com.customersupport.entity.Role;
import com.customersupport.entity.Ticket;
import com.customersupport.entity.User;
import com.customersupport.repository.AgentRepository;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.ReviewRepository;
import com.customersupport.repository.RoleRepository;
import com.customersupport.repository.TicketRepository;
import com.customersupport.repository.UserRepository;
import com.customersupport.util.PasswordEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AgentService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired private AgentRepository agentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EmailService emailService;

  @Autowired private TicketRepository ticketRepository;

  @Autowired private ReviewRepository reviewRepository;

  // Register a new agent
  public AgentDTO registerAgent(AgentRegistrationDTO registrationDTO) {
    // Check if email already exists
    if (userRepository.existsByEmail(registrationDTO.getEmail())) {
      throw new RuntimeException("Email already in use");
    }

    // Find company
    Company company =
        companyRepository
            .findById(registrationDTO.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company not found"));

    // Check if company has reached max agents
    Integer currentAgentCount = agentRepository.countByCompanyId(company.getId());
    if (currentAgentCount >= company.getMaxAgents()) {
      throw new RuntimeException("Company has reached maximum agent limit");
    }

    // Find agent role
    Role agentRole =
        roleRepository
            .findByName("SUPPORT_AGENT")
            .orElseThrow(() -> new RuntimeException("Agent role not found"));

    // Create user
    User user = new User();
    user.setCompany(company);
    user.setRole(agentRole);
    user.setEmail(registrationDTO.getEmail());
    user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
    user.setFirstname(registrationDTO.getFirstName());
    user.setLastname(registrationDTO.getLastName());
    user.setPhone(registrationDTO.getPhone());
    user.setStatus(User.UserStatus.ACTIVE);
    user.setEmailVerified(true);

    User savedUser = userRepository.save(user);

    // Send email with login credentials to the agent
    emailService.sendAgentRegistrationEmail(
        registrationDTO.getFirstName(),
        registrationDTO.getLastName(),
        registrationDTO.getEmail(),
        registrationDTO.getPassword(),
        company,
        registrationDTO.getDepartment(),
        registrationDTO.getSpecialization());

    // Create agent
    Agent agent = new Agent();
    agent.setUser(savedUser);
    agent.setCompany(company);
    agent.setDepartment(registrationDTO.getDepartment());
    agent.setSpecialization(registrationDTO.getSpecialization());
    agent.setMaxConcurrentTickets(
        registrationDTO.getMaxConcurrentTickets() != null
                && registrationDTO.getMaxConcurrentTickets() > 0
            ? registrationDTO.getMaxConcurrentTickets()
            : 10);
    agent.setShiftStart(registrationDTO.getShiftStart());
    agent.setShiftEnd(registrationDTO.getShiftEnd());
    agent.setIsAvailable(
        registrationDTO.getIsAvailable() == null ? Boolean.TRUE : registrationDTO.getIsAvailable());
    agent.setWorkingDays(normalizeWorkingDays(registrationDTO.getWorkingDays()));

    Agent savedAgent = agentRepository.save(agent);
    return convertToDTO(savedAgent);
  }

  // Get all agents by company
  public List<AgentDTO> getAgentsByCompany(Long companyId) {
    return agentRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Get agent by ID
  public Optional<AgentDTO> getAgentById(Long id) {
    return agentRepository.findById(id).map(this::convertToDTO);
  }

  // Get agent by user ID
  public Optional<AgentDTO> getAgentByUserId(Long userId) {
    return agentRepository.findByUserId(userId).map(this::convertToDTO);
  }

  // Search agents by company
  public List<AgentDTO> searchAgentsByCompany(Long companyId, String keyword) {
    return agentRepository.searchAgentsByCompany(companyId, keyword).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Update agent
  public AgentDTO updateAgent(Long id, AgentDTO agentDTO) {
    Agent agent =
        agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));

    // Update agent fields
    agent.setDepartment(agentDTO.getDepartment());
    agent.setSpecialization(agentDTO.getSpecialization());
    agent.setMaxConcurrentTickets(
        agentDTO.getMaxConcurrentTickets() != null && agentDTO.getMaxConcurrentTickets() > 0
            ? agentDTO.getMaxConcurrentTickets()
            : agent.getMaxConcurrentTickets() != null ? agent.getMaxConcurrentTickets() : 10);
    if (agentDTO.getIsAvailable() != null) {
      agent.setIsAvailable(agentDTO.getIsAvailable());
    }
    agent.setShiftStart(agentDTO.getShiftStart());
    agent.setShiftEnd(agentDTO.getShiftEnd());
    if (agentDTO.getWorkingDays() != null) {
      agent.setWorkingDays(normalizeWorkingDays(agentDTO.getWorkingDays()));
    }

    // Update user fields
    User user = agent.getUser();
    user.setFirstname(agentDTO.getFirstName());
    user.setLastname(agentDTO.getLastName());
    user.setPhone(agentDTO.getPhone());
    userRepository.save(user);

    Agent updatedAgent = agentRepository.save(agent);
    return convertToDTO(updatedAgent);
  }

  // Set agent availability
  public AgentDTO setAgentAvailability(Long id, Boolean isAvailable) {
    Agent agent =
        agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));

    agent.setIsAvailable(isAvailable);
    Agent updatedAgent = agentRepository.save(agent);
    return convertToDTO(updatedAgent);
  }

  // Delete agent
  public void deleteAgent(Long id) {
    Agent agent =
        agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));

    // Delete the user (should cascade to agent due to @OneToOne relationship)
    userRepository.delete(agent.getUser());
  }

  // Helper method to convert entity to DTO
  private AgentDTO convertToDTO(Agent agent) {
    return new AgentDTO().fromEntity(agent);
  }

  private String normalizeWorkingDays(String rawValue) {
    if (rawValue == null) {
      return "[]";
    }

    String trimmed = rawValue.trim();
    if (trimmed.isEmpty()) {
      return "[]";
    }

    try {
      JsonNode parsedNode = OBJECT_MAPPER.readTree(trimmed);
      if (parsedNode.isArray()) {
        return parsedNode.toString();
      }
      if (parsedNode.isTextual()) {
        return OBJECT_MAPPER.writeValueAsString(List.of(parsedNode.asText()));
      }
    } catch (JsonProcessingException ignored) {
      // fall through to manual normalization when the incoming value isn't valid JSON yet
    }

    String[] tokens = trimmed.split("[;,\\n]+|\\s{2,}");
    List<String> values =
        Arrays.stream(tokens)
            .map(String::trim)
            .filter(token -> !token.isEmpty())
            .collect(Collectors.toList());

    if (values.isEmpty()) {
      values = List.of(trimmed);
    }

    try {
      return OBJECT_MAPPER.writeValueAsString(values);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable to process working days", e);
    }
  }

  // Get agent performance metrics
  public AgentPerformanceDTO getAgentPerformance(Long agentId) {
    // Find agent or throw exception
    Agent agent =
        agentRepository
            .findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));

    // Get all tickets assigned to the agent
    List<Ticket> assignedTickets = ticketRepository.findByAgentId(agentId);

    // Get tickets by status
    List<Ticket> ongoingTickets =
        ticketRepository.findByAgentIdAndStatus(agentId, Ticket.Status.IN_PROGRESS);

    // Get closed tickets
    List<Ticket> closedTickets =
        ticketRepository.findByAgentIdAndStatus(agentId, Ticket.Status.CLOSED);

    // Calculate performance metrics
    double averageRating =
        reviewRepository.getAverageRatingByAgent(agentId) != null
            ? reviewRepository.getAverageRatingByAgent(agentId)
            : 0.0;

    // Calculate average response time (in minutes)
    double averageResponseTime =
        assignedTickets.stream()
            .filter(t -> t.getFirstResponseTime() != null)
            .mapToDouble(
                t -> {
                  LocalDateTime created = t.getCreatedAt();
                  LocalDateTime responded = t.getFirstResponseTime();
                  return (double) java.time.Duration.between(created, responded).toMinutes();
                })
            .average()
            .orElse(0.0);

    // Calculate average resolution time (in minutes)
    double averageResolutionTime =
        closedTickets.stream()
            .filter(t -> t.getClosedAt() != null)
            .mapToDouble(
                t -> {
                  LocalDateTime created = t.getCreatedAt();
                  LocalDateTime closed = t.getClosedAt();
                  return (double) java.time.Duration.between(created, closed).toMinutes();
                })
            .average()
            .orElse(0.0);

    // Convert entities to DTOs
    List<TicketDTO> assignedTicketDTOs =
        assignedTickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());

    List<TicketDTO> ongoingTicketDTOs =
        ongoingTickets.stream().map(TicketDTO::fromEntity).collect(Collectors.toList());

    // Create list of tickets with reviews
    List<AgentPerformanceDTO.TicketWithReviewDTO> ticketsWithReviews =
        closedTickets.stream()
            .filter(ticket -> ticket.getReview() != null)
            .map(
                ticket -> {
                  TicketDTO ticketDTO = TicketDTO.fromEntity(ticket);
                  ReviewDTO reviewDTO = null;
                  if (ticket.getReview() != null) {
                    reviewDTO = ReviewDTO.fromEntity(ticket.getReview());
                  }
                  return new AgentPerformanceDTO.TicketWithReviewDTO(ticketDTO, reviewDTO);
                })
            .collect(Collectors.toList());

    // Build and return the DTO
    return AgentPerformanceDTO.builder()
        .agentId(agentId)
        .agentName(agent.getUser().getFirstname() + " " + agent.getUser().getLastname())
        .totalAssignedTickets(assignedTickets.size())
        .ongoingTicketsCount(ongoingTickets.size())
        .closedTicketsCount(closedTickets.size())
        .averageResponseTime(averageResponseTime)
        .averageResolutionTime(averageResolutionTime)
        .averageRating(averageRating)
        .assignedTickets(assignedTicketDTOs)
        .ongoingTickets(ongoingTicketDTOs)
        .reviewedTickets(ticketsWithReviews)
        .build();
  }
}
