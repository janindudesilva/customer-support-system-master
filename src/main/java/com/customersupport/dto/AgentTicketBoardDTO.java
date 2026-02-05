package com.customersupport.dto;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentTicketBoardDTO {
  @Builder.Default private List<TicketDTO> assignedTickets = Collections.emptyList();

  @Builder.Default private List<TicketDTO> availableTickets = Collections.emptyList();

  @Builder.Default private List<TicketDTO> activeCompanyTickets = Collections.emptyList();

  @Builder.Default private List<TicketDTO> closedCompanyTickets = Collections.emptyList();

  private AgentStats stats;

  private AgentSummary agent;

  @Data
  @Builder
  public static class AgentStats {
    private long assignedCount;
    private long availableCount;
    private long activeCount;
    private long closedCount;
    private long resolvedToday;
    private Double averageResolutionHours;
    private Double satisfactionRating;
  }

  @Data
  @Builder
  public static class AgentSummary {
    private Long agentId;
    private Long agentUserId;
    private Long companyId;
    private String agentName;
    private String companyName;
    private Integer maxConcurrentTickets;
    private Integer currentTicketCount;
  }
}
