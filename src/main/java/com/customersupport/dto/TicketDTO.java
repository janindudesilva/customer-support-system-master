package com.customersupport.dto;

import com.customersupport.entity.Ticket;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
  private Long id;
  private String ticketNumber;
  private Long companyId;
  private String companyName;
  private Long customerId;
  private String customerName;
  private Long agentId;
  private String agentName;
  private Long categoryId;
  private String categoryName;
  private String title;
  private String description;
  private Ticket.Priority priority;
  private Ticket.Status status;
  private Ticket.Source source;
  private String resolution;
  private String tags;
  private String attachments;
  private LocalDateTime estimatedResolutionTime;
  private LocalDateTime actualResolutionTime;
  private LocalDateTime firstResponseTime;
  private LocalDateTime lastActivity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime closedAt;
  private boolean canEdit;
  private boolean canCancel;
  private boolean hasReview;
  private ReviewDTO review;

  public static TicketDTO fromEntity(Ticket ticket) {
    TicketDTO dto = new TicketDTO();
    dto.setId(ticket.getId());
    dto.setTicketNumber(ticket.getTicketNumber());
    dto.setCompanyId(ticket.getCompany().getId());
    dto.setCompanyName(ticket.getCompany().getName());
    dto.setCustomerId(ticket.getCustomer().getId());
    dto.setCustomerName(
        ticket.getCustomer().getUser().getFirstname()
            + " "
            + ticket.getCustomer().getUser().getLastname());

    if (ticket.getAgent() != null) {
      dto.setAgentId(ticket.getAgent().getId());
      dto.setAgentName(
          ticket.getAgent().getUser().getFirstname()
              + " "
              + ticket.getAgent().getUser().getLastname());
    }

    if (ticket.getCategory() != null) {
      dto.setCategoryId(ticket.getCategory().getId());
      dto.setCategoryName(ticket.getCategory().getName());
    }

    dto.setTitle(ticket.getTitle());
    dto.setDescription(ticket.getDescription());
    dto.setPriority(ticket.getPriority());
    dto.setStatus(ticket.getStatus());
    dto.setSource(ticket.getSource());
    dto.setResolution(ticket.getResolution());
    dto.setTags(ticket.getTags());
    dto.setAttachments(ticket.getAttachment());
    dto.setEstimatedResolutionTime(ticket.getEstimatedResolutionTime());
    dto.setActualResolutionTime(ticket.getActualResolutionTime());
    dto.setFirstResponseTime(ticket.getFirstResponseTime());
    dto.setLastActivity(ticket.getLastActivity());
    dto.setCreatedAt(ticket.getCreatedAt());
    dto.setUpdatedAt(ticket.getUpdatedAt());
    dto.setClosedAt(ticket.getClosedAt());

    dto.setCanEdit(ticket.canBeEditedByCustomer());
    dto.setCanCancel(ticket.canBeCancelledByCustomer());
    dto.setHasReview(ticket.getReview() != null);

    if (ticket.getReview() != null) {
      dto.setReview(ReviewDTO.fromEntity(ticket.getReview()));
    }

    return dto;
  }
}
