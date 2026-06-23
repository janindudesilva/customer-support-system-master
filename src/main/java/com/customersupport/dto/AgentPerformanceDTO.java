package com.customersupport.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentPerformanceDTO {
  private Long agentId;
  private String agentName;

  // Ticket counts
  private Integer totalAssignedTickets;
  private Integer ongoingTicketsCount;
  private Integer closedTicketsCount;

  // Performance metrics
  private Double averageResponseTime; // in minutes
  private Double averageResolutionTime; // in minutes
  private Double averageRating;

  // Detailed data for display
  private List<TicketDTO> assignedTickets;
  private List<TicketDTO> ongoingTickets;
  private List<TicketWithReviewDTO> reviewedTickets;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TicketWithReviewDTO {
    private TicketDTO ticket;
    private ReviewDTO review;
  }
}
