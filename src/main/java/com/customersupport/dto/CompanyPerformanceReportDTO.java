package com.customersupport.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyPerformanceReportDTO {

  private Summary summary;
  private List<AgentPerformance> agents;
  private TicketInsights tickets;
  private CustomerInsights customers;
  private ReviewInsights reviews;
  private List<AnalyticsDTO> analytics;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Summary {
    private long totalTickets;
    private long openTickets;
    private long resolvedTickets;
    private long escalatedTickets;
    private double averageResolutionHours;
    private long ticketsLast30Days;
    private double customerSatisfactionScore;
    private long activeAgents;
    private long availableAgents;
    private long customersServed;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AgentPerformance {
    private Long agentId;
    private String agentName;
    private String department;
    private String specialization;
    private boolean available;
    private int maxCapacity;
    private int currentLoad;
    private long assignedTickets;
    private long activeTickets;
    private long resolvedTickets;
    private double averageResolutionHours;
    private double satisfactionScore;
    private Integer totalTicketsHandled;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TicketInsights {
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> priorityBreakdown;
    private List<CategoryInsight> topCategories;
    private List<TicketTrendPoint> monthlyTrends;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryInsight {
    private String name;
    private long count;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TicketTrendPoint {
    private String label;
    private long total;
    private long resolved;
    private long escalated;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CustomerInsights {
    private long totalCustomers;
    private long activeCustomers;
    private long newCustomersThisMonth;
    private double averageSatisfaction;
    private Map<String, Long> contactPreferences;
    private Map<String, Long> customerTypes;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReviewInsights {
    private long totalReviews;
    private double averageRating;
    private double recommendationRate;
    private Map<Integer, Long> ratingDistribution;
    private List<ReviewSummary> recentReviews;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReviewSummary {
    private Long reviewId;
    private Long ticketId;
    private String agentName;
    private String customerName;
    private Integer rating;
    private String feedback;
    private LocalDateTime createdAt;
  }
}
