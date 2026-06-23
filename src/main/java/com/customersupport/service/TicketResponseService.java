package com.customersupport.service;

import com.customersupport.dto.TicketResponseDTO;
import com.customersupport.entity.Ticket;
import com.customersupport.entity.TicketResponse;
import com.customersupport.entity.User;
import com.customersupport.repository.TicketRepository;
import com.customersupport.repository.TicketResponseRepository;
import com.customersupport.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketResponseService {

  private final TicketResponseRepository ticketResponseRepository;
  private final TicketRepository ticketRepository;
  private final UserRepository userRepository;

  public TicketResponseDTO addResponse(Long ticketId, TicketResponseDTO responseDTO) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    User user =
        userRepository
            .findById(responseDTO.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

    validateResponderPermissions(ticket, user);

    TicketResponse response = new TicketResponse();
    response.setTicket(ticket);
    response.setUser(user);
    response.setMessage(responseDTO.getMessage());
    response.setResponseType(resolveResponseType(user, responseDTO));
    Boolean isPublic = responseDTO.getIsPublic() != null ? responseDTO.getIsPublic() : Boolean.TRUE;
    response.setIsPublic(isPublic);
    response.setCreatedAt(LocalDateTime.now());
    response.setAttachments(responseDTO.getAttachments());

    if (TicketResponse.ResponseType.AGENT_REPLY.equals(response.getResponseType())
        && ticket.getFirstResponseTime() == null) {
      ticket.setFirstResponseTime(LocalDateTime.now());
    }

    if (TicketResponse.ResponseType.AGENT_REPLY.equals(response.getResponseType())
        && (ticket.getStatus() == Ticket.Status.OPEN
            || ticket.getStatus() == Ticket.Status.PENDING_CUSTOMER)) {
      ticket.setStatus(Ticket.Status.IN_PROGRESS);
    }

    ticket.setLastActivity(LocalDateTime.now());

    BigDecimal responseTime = calculateResponseTime(ticket.getCreatedAt(), response.getCreatedAt());
    response.setResponseTime(responseTime);

    ticketRepository.save(ticket);
    TicketResponse savedResponse = ticketResponseRepository.save(response);

    return convertToDTO(savedResponse);
  }

  public List<TicketResponseDTO> getResponsesByTicket(Long ticketId) {
    List<TicketResponse> responses =
        ticketResponseRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    return responses.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  public TicketResponseDTO updateResponse(Long responseId, TicketResponseDTO responseDTO) {
    TicketResponse response =
        ticketResponseRepository
            .findById(responseId)
            .orElseThrow(() -> new RuntimeException("Response not found"));

    response.setMessage(responseDTO.getMessage());
    response.setIsPublic(responseDTO.getIsPublic());
    response.setUpdatedAt(LocalDateTime.now());

    TicketResponse updatedResponse = ticketResponseRepository.save(response);
    return convertToDTO(updatedResponse);
  }

  public void deleteResponse(Long responseId) {
    if (!ticketResponseRepository.existsById(responseId)) {
      throw new RuntimeException("Response not found");
    }
    ticketResponseRepository.deleteById(responseId);
  }

  public List<TicketResponseDTO> getResponsesByUser(Long userId) {
    List<TicketResponse> responses =
        ticketResponseRepository.findByUserIdOrderByCreatedAtDesc(userId);
    return responses.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  private TicketResponseDTO convertToDTO(TicketResponse response) {
    TicketResponseDTO dto = new TicketResponseDTO();
    dto.setId(response.getId());
    dto.setTicketId(response.getTicket().getId());
    dto.setUserId(response.getUser().getId());
    dto.setUserName(response.getUser().getFirstname() + " " + response.getUser().getLastname());
    dto.setMessage(response.getMessage());
    dto.setResponseType(response.getResponseType());
    dto.setIsPublic(response.getIsPublic());
    dto.setAttachments(response.getAttachments());
    dto.setResponseTime(response.getResponseTime());
    dto.setCreatedAt(response.getCreatedAt());
    dto.setUpdatedAt(response.getUpdatedAt());
    return dto;
  }

  private void validateResponderPermissions(Ticket ticket, User user) {
    String roleName = user.getRole() != null ? user.getRole().getName() : null;

    if (roleName == null) {
      return;
    }

    switch (roleName) {
      case "SUPPORT_AGENT" -> {
        if (ticket.getAgent() == null
            || !ticket.getAgent().getUser().getId().equals(user.getId())) {
          throw new RuntimeException("You are not assigned to this ticket");
        }
        if (ticket.getStatus() == Ticket.Status.CLOSED
            || ticket.getStatus() == Ticket.Status.CANCELLED) {
          throw new RuntimeException("This ticket is already closed");
        }
      }
      case "CUSTOMER" -> {
        if (!ticket.getCustomer().getUser().getId().equals(user.getId())) {
          throw new RuntimeException("You cannot respond to this ticket");
        }
        if (ticket.getStatus() == Ticket.Status.CLOSED
            || ticket.getStatus() == Ticket.Status.CANCELLED) {
          throw new RuntimeException("This ticket is already closed");
        }
      }
      default -> {
        // Allow other roles like company admin, super admin by default
      }
    }
  }

  private TicketResponse.ResponseType resolveResponseType(
      User user, TicketResponseDTO responseDTO) {
    TicketResponse.ResponseType providedType = responseDTO.getResponseType();
    String roleName = user.getRole() != null ? user.getRole().getName() : null;

    if (roleName == null) {
      return providedType != null ? providedType : TicketResponse.ResponseType.AGENT_REPLY;
    }

    return switch (roleName) {
      case "CUSTOMER" -> TicketResponse.ResponseType.CUSTOMER_REPLY;
      case "SUPPORT_AGENT" -> TicketResponse.ResponseType.AGENT_REPLY;
      default -> providedType != null ? providedType : TicketResponse.ResponseType.AGENT_REPLY;
    };
  }

  private BigDecimal calculateResponseTime(LocalDateTime createdAt, LocalDateTime respondedAt) {
    if (createdAt == null || respondedAt == null) {
      return null;
    }
    Duration duration = Duration.between(createdAt, respondedAt);
    double hours = duration.toMinutes() / 60.0;
    return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
  }
}
