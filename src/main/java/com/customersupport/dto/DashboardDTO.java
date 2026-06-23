package com.customersupport.dto;

import com.customersupport.entity.AuditLog;
import com.customersupport.entity.Ticket;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {

  // Company status
  private Integer totalCompanies;
  private Integer activeCompanies;
  private Integer inactiveCompanies;
  private Integer suspendedCompanies;

  // User status
  private Integer totalUsers;
  private Integer totalAgents;
  private Integer totalCustomers;

  // Tickets status
  private Integer totalTickets;
  private Integer openTickets;
  private Integer inProgressTickets;
  private Integer pendingCustomerTickets;
  private Integer resolvedTickets;
  private Integer closedTickets;
  private Integer cancelledTickets;

  // Agent-specific Stats
  private Integer assignedTickets;
  private Integer resolvedTodayTickets;

  // Performance Metrics
  private Double averageResolutionTime; // in hours
  private Double averageResponseTime; // in hours
  private Double averageRating;

  // Trend Data
  private Map<String, Integer> ticketTrend; // date -> count
  private Map<String, Double> resolutionTimeTrend; // date -> avg hours
  private Map<String, Integer> categoryDistribution; // category -> count
  private Map<String, Double> satisfactionTrend; // date -> avg rating

  // Recent Items
  private List<Ticket> recentTickets;
  private List<AuditLog> recentActivities;
}
