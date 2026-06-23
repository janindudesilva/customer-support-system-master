package com.customersupport.service;

import com.customersupport.dto.DashboardDTO;
import com.customersupport.entity.AuditLog;
import com.customersupport.entity.Ticket;
import com.customersupport.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CustomerRepository customerRepository;

  @Autowired private AgentRepository agentRepository;

  @Autowired private TicketRepository ticketRepository;

  @Autowired private ReviewRepository reviewRepository;

  @Autowired private AuditLogRepository auditLogRepository;

  // Get super admin dashboard
  public DashboardDTO getSuperAdminDashboard() {
    DashboardDTO dashboard = new DashboardDTO();

    // Company stats
    dashboard.setTotalCompanies((int) companyRepository.count());
    dashboard.setActiveCompanies(
        companyRepository.countByStatus(com.customersupport.entity.Company.CompanyStatus.ACTIVE));
    dashboard.setInactiveCompanies(
        companyRepository.countByStatus(com.customersupport.entity.Company.CompanyStatus.INACTIVE));
    dashboard.setSuspendedCompanies(
        companyRepository.countByStatus(
            com.customersupport.entity.Company.CompanyStatus.SUSPENDED));

    // User stats
    dashboard.setTotalUsers((int) userRepository.count());

    // Ticket stats across all companies
    dashboard.setTotalTickets((int) ticketRepository.count());
    dashboard.setOpenTickets(ticketRepository.countByStatus(Ticket.Status.OPEN));
    dashboard.setResolvedTickets(ticketRepository.countByStatus(Ticket.Status.RESOLVED));

    // Recent activity
    dashboard.setRecentActivities(auditLogRepository.findRecentAuditLogs(10));

    return dashboard;
  }

  // Get company admin dashboard
  public DashboardDTO getCompanyAdminDashboard(Long companyId) {
    DashboardDTO dashboard = new DashboardDTO();

    // User stats
    dashboard.setTotalUsers(Math.toIntExact(userRepository.countByCompanyId(companyId)));
    dashboard.setTotalAgents(agentRepository.countByCompanyId(companyId));
    dashboard.setTotalCustomers(customerRepository.countByCompanyId(companyId));

    // Ticket stats
    dashboard.setTotalTickets(Math.toIntExact(ticketRepository.countByCompanyId(companyId)));
    dashboard.setOpenTickets(
        Math.toIntExact(ticketRepository.countByCompanyIdAndStatus(companyId, Ticket.Status.OPEN)));
    dashboard.setInProgressTickets(
        Math.toIntExact(
            ticketRepository.countByCompanyIdAndStatus(companyId, Ticket.Status.IN_PROGRESS)));
    dashboard.setResolvedTickets(
        Math.toIntExact(
            ticketRepository.countByCompanyIdAndStatus(companyId, Ticket.Status.RESOLVED)));

    // Customer satisfaction
    Double avgRating = reviewRepository.getAverageRatingByCompany(companyId);
    dashboard.setAverageRating(avgRating != null ? avgRating : 0.0);

    // Recent activity
    dashboard.setRecentActivities(auditLogRepository.findRecentAuditLogsByCompany(companyId, 10));

    return dashboard;
  }

  // Get agent dashboard
  public DashboardDTO getAgentDashboard(Long agentId) {
    DashboardDTO dashboard = new DashboardDTO();

    // Ticket stats
    dashboard.setAssignedTickets(Math.toIntExact(ticketRepository.countByAgentId(agentId)));
    dashboard.setOpenTickets(
        Math.toIntExact(ticketRepository.countByAgentIdAndStatus(agentId, Ticket.Status.OPEN)));
    dashboard.setInProgressTickets(
        Math.toIntExact(
            ticketRepository.countByAgentIdAndStatus(agentId, Ticket.Status.IN_PROGRESS)));
    dashboard.setResolvedTickets(
        Math.toIntExact(ticketRepository.countByAgentIdAndStatus(agentId, Ticket.Status.RESOLVED)));

    // Calculate tickets resolved today
    LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    List<Ticket> resolvedToday =
        ticketRepository.findByAgentIdAndStatusAndClosedAtBetween(
            agentId, Ticket.Status.RESOLVED, startOfDay, endOfDay);
    dashboard.setResolvedTodayTickets(resolvedToday.size());

    // Get customer satisfaction rating
    Double avgRating = reviewRepository.getAverageRatingByAgent(agentId);
    dashboard.setAverageRating(avgRating != null ? avgRating : 0.0);

    // Recent activity
    dashboard.setRecentActivities(auditLogRepository.findRecentAuditLogsByUser(agentId, 10));

    return dashboard;
  }

  // Get customer dashboard
  public DashboardDTO getCustomerDashboard(Long customerId) {
    DashboardDTO dashboard = new DashboardDTO();

    // Ticket stats
    dashboard.setTotalTickets(Math.toIntExact(ticketRepository.countByCustomerId(customerId)));
    dashboard.setOpenTickets(
        Math.toIntExact(
            ticketRepository.countByCustomerIdAndStatus(customerId, Ticket.Status.OPEN)));
    dashboard.setInProgressTickets(
        Math.toIntExact(
            ticketRepository.countByCustomerIdAndStatus(customerId, Ticket.Status.IN_PROGRESS)));
    dashboard.setResolvedTickets(
        Math.toIntExact(
            ticketRepository.countByCustomerIdAndStatus(customerId, Ticket.Status.RESOLVED)));

    // Recent tickets
    dashboard.setRecentTickets(
        ticketRepository
            .findByCustomerIdOrderByCreatedAtDesc(customerId)
            .subList(
                0,
                Math.min(
                    5, ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).size())));

    return dashboard;
  }

  // Get recent activities
  public List<AuditLog> getRecentActivities(Long userId) {
    return auditLogRepository.findRecentAuditLogsByUser(userId, 10);
  }

  // Helper method to calculate date for a period ago
  // Helper method for future use
  @SuppressWarnings("unused")
  private LocalDateTime getDateForPeriod(String period) {
    LocalDateTime now = LocalDateTime.now();
    switch (period) {
      case "today":
        return now.toLocalDate().atStartOfDay();
      case "week":
        return now.minusWeeks(1);
      case "month":
        return now.minusMonths(1);
      case "quarter":
        return now.minusMonths(3);
      case "year":
        return now.minusYears(1);
      default:
        return now.minusMonths(1); // Default to last month
    }
  }
}
