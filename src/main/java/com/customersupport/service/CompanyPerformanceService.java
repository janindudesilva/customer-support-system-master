package com.customersupport.service;

import com.customersupport.dto.AgentDTO;
import com.customersupport.dto.AnalyticsDTO;
import com.customersupport.dto.CompanyPerformanceReportDTO;
import com.customersupport.dto.CustomerDTO;
import com.customersupport.dto.ReviewDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.entity.Company;
import com.customersupport.entity.Ticket;
import com.customersupport.repository.CompanyRepository;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class CompanyPerformanceService {

  private final TicketService ticketService;
  private final AgentService agentService;
  private final CustomerService customerService;
  private  ReviewService reviewService;
  private final AnalyticsService analyticsService;
  private final CompanyRepository companyRepository;
  private final SpringTemplateEngine pdfTemplateEngine;

  public CompanyPerformanceService(
      TicketService ticketService,
      AgentService agentService,
      CustomerService customerService,
      ReviewService reviewService,
      AnalyticsService analyticsService,
      CompanyRepository companyRepository,
      @Qualifier("pdfTemplateEngine") SpringTemplateEngine pdfTemplateEngine) {
    this.ticketService = ticketService;
    this.agentService = agentService;
    this.customerService = customerService;
    this.reviewService = reviewService;
    this.analyticsService = analyticsService;
    this.companyRepository = companyRepository;
    this.pdfTemplateEngine = pdfTemplateEngine;
  }

  private static final DateTimeFormatter PDF_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm z");

  public CompanyPerformanceReportDTO buildPerformanceReport(Long companyId) {
    List<TicketDTO> tickets = ticketService.getTicketsByCompany(companyId);
    List<AgentDTO> agents = agentService.getAgentsByCompany(companyId);
    List<CustomerDTO> customers = customerService.getCustomersByCompany(companyId);
    List<ReviewDTO> reviews = reviewService.getReviewsByCompany(companyId);
    List<AnalyticsDTO> analytics =
        analyticsService.getAnalyticsByCompany(companyId).stream()
            .sorted(
                Comparator.comparing(
                    AnalyticsDTO::getPeriodStart, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(12)
            .collect(Collectors.toList());

    CompanyPerformanceReportDTO.Summary summary = buildSummary(tickets, agents, customers, reviews);
    List<CompanyPerformanceReportDTO.AgentPerformance> agentPerformance =
        buildAgentPerformance(agents, tickets);
    CompanyPerformanceReportDTO.TicketInsights ticketInsights = buildTicketInsights(tickets);
    CompanyPerformanceReportDTO.CustomerInsights customerInsights =
        buildCustomerInsights(customers, tickets);
    CompanyPerformanceReportDTO.ReviewInsights reviewInsights = buildReviewInsights(reviews);

    return CompanyPerformanceReportDTO.builder()
        .summary(summary)
        .agents(agentPerformance)
        .tickets(ticketInsights)
        .customers(customerInsights)
        .reviews(reviewInsights)
        .analytics(analytics)
        .build();
  }

  public PerformancePdf generatePerformancePdf(Long companyId) {
    Company company =
        companyRepository
            .findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

    CompanyPerformanceReportDTO report = buildPerformanceReport(companyId);

    CompanyPerformanceReportDTO.TicketInsights ticketInsights =
        Optional.ofNullable(report.getTickets())
            .orElse(CompanyPerformanceReportDTO.TicketInsights.builder().build());
    CompanyPerformanceReportDTO.CustomerInsights customerInsights =
        Optional.ofNullable(report.getCustomers())
            .orElse(CompanyPerformanceReportDTO.CustomerInsights.builder().build());
    CompanyPerformanceReportDTO.ReviewInsights reviewInsights =
        Optional.ofNullable(report.getReviews())
            .orElse(CompanyPerformanceReportDTO.ReviewInsights.builder().build());

    Context context = new Context();
    context.setVariable("company", company);
    context.setVariable("companyName", company.getName());
    context.setVariable("report", report);
    context.setVariable("summary", report.getSummary());
    context.setVariable("agents", Optional.ofNullable(report.getAgents()).orElse(List.of()));
    context.setVariable("tickets", ticketInsights);
    context.setVariable("customers", customerInsights);
    context.setVariable("reviews", reviewInsights);
    context.setVariable("analytics", Optional.ofNullable(report.getAnalytics()).orElse(List.of()));
    ZonedDateTime generatedAt = ZonedDateTime.now();
    context.setVariable("generatedAt", generatedAt);
    context.setVariable("generatedAtFormatted", PDF_DATE_FORMATTER.format(generatedAt));

    String html = pdfTemplateEngine.process("company-admin/company-performance-report", context);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.withHtmlContent(html, null);

      // Set text direction for better layout
      builder.defaultTextDirection(TextDirection.LTR);

      // Set output stream
      builder.toStream(outputStream);
      builder.run();

      return new PerformancePdf(outputStream.toByteArray(), buildFileName(company));
    } catch (Exception ex) {
      throw new IllegalStateException(
          "Failed to generate performance report PDF: " + ex.getMessage(), ex);
    }
  }

  private String buildFileName(Company company) {
    String name = Optional.ofNullable(company.getName()).orElse("company");
    String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    String date = LocalDate.now().toString();
    return (slug.isEmpty() ? "company" : slug) + "-performance-" + date + ".pdf";
  }

  public record PerformancePdf(byte[] content, String filename) {}

  private CompanyPerformanceReportDTO.Summary buildSummary(
      List<TicketDTO> tickets,
      List<AgentDTO> agents,
      List<CustomerDTO> customers,
      List<ReviewDTO> reviews) {
    long totalTickets = tickets.size();
    long openTickets =
        tickets.stream()
            .filter(ticket -> ticket.getStatus() != null && isActiveStatus(ticket.getStatus()))
            .count();
    long resolvedTickets =
        tickets.stream()
            .filter(ticket -> ticket.getStatus() != null && isClosedStatus(ticket.getStatus()))
            .count();
    long escalatedTickets =
        tickets.stream()
            .filter(ticket -> ticket.getStatus() == Ticket.Status.PENDING_CUSTOMER)
            .count();

    double averageResolutionHours =
        tickets.stream()
            .filter(ticket -> ticket.getCreatedAt() != null && ticket.getClosedAt() != null)
            .mapToDouble(ticket -> durationInHours(ticket.getCreatedAt(), ticket.getClosedAt()))
            .average()
            .orElse(0.0);

    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
    long ticketsLast30Days =
        tickets.stream()
            .filter(
                ticket ->
                    ticket.getCreatedAt() != null && ticket.getCreatedAt().isAfter(thirtyDaysAgo))
            .count();

    double customerSatisfaction =
        reviews.stream()
            .filter(review -> review.getRating() != null)
            .mapToInt(ReviewDTO::getRating)
            .average()
            .orElse(0.0);

    long activeAgents = agents.size();
    long availableAgents =
        agents.stream().filter(agent -> Boolean.TRUE.equals(agent.getIsAvailable())).count();

    long customersServed =
        tickets.stream().map(TicketDTO::getCustomerId).filter(Objects::nonNull).distinct().count();

    return CompanyPerformanceReportDTO.Summary.builder()
        .totalTickets(totalTickets)
        .openTickets(openTickets)
        .resolvedTickets(resolvedTickets)
        .escalatedTickets(escalatedTickets)
        .averageResolutionHours(round(averageResolutionHours))
        .ticketsLast30Days(ticketsLast30Days)
        .customerSatisfactionScore(round(customerSatisfaction))
        .activeAgents(activeAgents)
        .availableAgents(availableAgents)
        .customersServed(customersServed)
        .build();
  }

  private List<CompanyPerformanceReportDTO.AgentPerformance> buildAgentPerformance(
      List<AgentDTO> agents, List<TicketDTO> tickets) {
    Map<Long, List<TicketDTO>> ticketsByAgent =
        tickets.stream()
            .filter(ticket -> ticket.getAgentId() != null)
            .collect(Collectors.groupingBy(TicketDTO::getAgentId));

    return agents.stream()
        .map(
            agent -> {
              List<TicketDTO> agentTickets = ticketsByAgent.getOrDefault(agent.getId(), List.of());
              long assigned = agentTickets.size();
              long active =
                  agentTickets.stream()
                      .filter(
                          ticket ->
                              ticket.getStatus() != null && isActiveStatus(ticket.getStatus()))
                      .count();
              long resolved =
                  agentTickets.stream()
                      .filter(
                          ticket ->
                              ticket.getStatus() != null && isClosedStatus(ticket.getStatus()))
                      .count();
              double avgResolution =
                  agentTickets.stream()
                      .filter(
                          ticket -> ticket.getCreatedAt() != null && ticket.getClosedAt() != null)
                      .mapToDouble(
                          ticket -> durationInHours(ticket.getCreatedAt(), ticket.getClosedAt()))
                      .average()
                      .orElse(0.0);

              double satisfaction =
                  Optional.ofNullable(agent.getCustomerSatisfactionRating())
                      .map(BigDecimal::doubleValue)
                      .orElse(0.0);

              return CompanyPerformanceReportDTO.AgentPerformance.builder()
                  .agentId(agent.getId())
                  .agentName(buildAgentName(agent))
                  .department(agent.getDepartment())
                  .specialization(agent.getSpecialization())
                  .available(Boolean.TRUE.equals(agent.getIsAvailable()))
                  .maxCapacity(Optional.ofNullable(agent.getMaxConcurrentTickets()).orElse(0))
                  .currentLoad(Optional.ofNullable(agent.getCurrentTicketCount()).orElse(0))
                  .assignedTickets(assigned)
                  .activeTickets(active)
                  .resolvedTickets(resolved)
                  .averageResolutionHours(round(avgResolution))
                  .satisfactionScore(round(satisfaction))
                  .totalTicketsHandled(
                      Optional.ofNullable(agent.getTotalTicketsHandled()).orElse(0))
                  .build();
            })
        .sorted(
            Comparator.comparingLong(
                    CompanyPerformanceReportDTO.AgentPerformance::getResolvedTickets)
                .reversed())
        .collect(Collectors.toList());
  }

  private CompanyPerformanceReportDTO.TicketInsights buildTicketInsights(List<TicketDTO> tickets) {
    Map<String, Long> statusBreakdown =
        tickets.stream()
            .filter(ticket -> ticket.getStatus() != null)
            .collect(
                Collectors.groupingBy(ticket -> ticket.getStatus().name(), Collectors.counting()));

    Map<String, Long> priorityBreakdown =
        tickets.stream()
            .filter(ticket -> ticket.getPriority() != null)
            .collect(
                Collectors.groupingBy(
                    ticket -> ticket.getPriority().name(), Collectors.counting()));

    Map<String, Long> categoryCounts =
        tickets.stream()
            .filter(ticket -> ticket.getCategoryName() != null)
            .collect(Collectors.groupingBy(TicketDTO::getCategoryName, Collectors.counting()));

    List<CompanyPerformanceReportDTO.CategoryInsight> topCategories =
        categoryCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(
                entry ->
                    CompanyPerformanceReportDTO.CategoryInsight.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
            .collect(Collectors.toList());

    Map<YearMonth, List<TicketDTO>> ticketsByMonth =
        tickets.stream()
            .filter(ticket -> ticket.getCreatedAt() != null)
            .collect(Collectors.groupingBy(ticket -> YearMonth.from(ticket.getCreatedAt())));

    List<CompanyPerformanceReportDTO.TicketTrendPoint> monthlyTrends =
        ticketsByMonth.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(
                entry -> {
                  YearMonth month = entry.getKey();
                  List<TicketDTO> monthTickets = entry.getValue();
                  long total = monthTickets.size();
                  long resolved =
                      monthTickets.stream()
                          .filter(
                              ticket ->
                                  ticket.getStatus() != null && isClosedStatus(ticket.getStatus()))
                          .count();
                  long escalated =
                      monthTickets.stream()
                          .filter(ticket -> ticket.getStatus() == Ticket.Status.PENDING_CUSTOMER)
                          .count();
                  return CompanyPerformanceReportDTO.TicketTrendPoint.builder()
                      .label(month.toString())
                      .total(total)
                      .resolved(resolved)
                      .escalated(escalated)
                      .build();
                })
            .collect(Collectors.toList());

    return CompanyPerformanceReportDTO.TicketInsights.builder()
        .statusBreakdown(orderByValueDesc(statusBreakdown))
        .priorityBreakdown(orderByValueDesc(priorityBreakdown))
        .topCategories(topCategories)
        .monthlyTrends(monthlyTrends)
        .build();
  }

  private CompanyPerformanceReportDTO.CustomerInsights buildCustomerInsights(
      List<CustomerDTO> customers, List<TicketDTO> tickets) {
    long totalCustomers = customers.size();
    long activeCustomers =
        customers.stream()
            .filter(customer -> Optional.ofNullable(customer.getTotalTickets()).orElse(0) > 0)
            .count();

    LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
    long newCustomersThisMonth =
        customers.stream()
            .filter(
                customer ->
                    customer.getCreatedAt() != null
                        && !customer.getCreatedAt().toLocalDate().isBefore(firstOfMonth))
            .count();

    double averageSatisfaction =
        customers.stream()
            .map(CustomerDTO::getSatisfactionScore)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .average()
            .orElse(0.0);

    Map<String, Long> contactPreferences =
        customers.stream()
            .filter(customer -> customer.getPreferredContactMethod() != null)
            .collect(
                Collectors.groupingBy(
                    customer -> customer.getPreferredContactMethod().name(),
                    Collectors.counting()));

    Map<String, Long> customerTypes =
        customers.stream()
            .filter(customer -> customer.getCustomerType() != null)
            .collect(
                Collectors.groupingBy(
                    customer -> customer.getCustomerType().name(), Collectors.counting()));

    return CompanyPerformanceReportDTO.CustomerInsights.builder()
        .totalCustomers(totalCustomers)
        .activeCustomers(activeCustomers)
        .newCustomersThisMonth(newCustomersThisMonth)
        .averageSatisfaction(round(averageSatisfaction))
        .contactPreferences(orderByValueDesc(contactPreferences))
        .customerTypes(orderByValueDesc(customerTypes))
        .build();
  }

  private CompanyPerformanceReportDTO.ReviewInsights buildReviewInsights(List<ReviewDTO> reviews) {
    long totalReviews = reviews.size();
    double averageRating =
        reviews.stream()
            .filter(review -> review.getRating() != null)
            .mapToInt(ReviewDTO::getRating)
            .average()
            .orElse(0.0);

    double recommendationRate =
        reviews.stream()
            .filter(review -> review.getWouldRecommend() != null)
            .filter(review -> review.getWouldRecommend())
            .count();
    recommendationRate = totalReviews > 0 ? (recommendationRate * 100.0) / totalReviews : 0.0;

    Map<Integer, Long> distribution =
        reviews.stream()
            .filter(review -> review.getRating() != null)
            .collect(Collectors.groupingBy(ReviewDTO::getRating, Collectors.counting()));

    // Ensure all rating buckets exist
    for (int rating = 1; rating <= 5; rating++) {
      distribution.putIfAbsent(rating, 0L);
    }

    List<CompanyPerformanceReportDTO.ReviewSummary> recentReviews =
        reviews.stream()
            .sorted(
                Comparator.comparing(
                    ReviewDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(6)
            .map(
                review ->
                    CompanyPerformanceReportDTO.ReviewSummary.builder()
                        .reviewId(review.getId())
                        .ticketId(review.getTicketId())
                        .agentName(review.getAgentName())
                        .customerName(review.getCustomerName())
                        .rating(review.getRating())
                        .feedback(review.getFeedback())
                        .createdAt(review.getCreatedAt())
                        .build())
            .collect(Collectors.toList());

    return CompanyPerformanceReportDTO.ReviewInsights.builder()
        .totalReviews(totalReviews)
        .averageRating(round(averageRating))
        .recommendationRate(round(recommendationRate))
        .ratingDistribution(
            distribution.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new)))
        .recentReviews(recentReviews)
        .build();
  }

  private boolean isActiveStatus(Ticket.Status status) {
    return status == Ticket.Status.OPEN
        || status == Ticket.Status.IN_PROGRESS
        || status == Ticket.Status.PENDING_CUSTOMER;
  }

  private boolean isClosedStatus(Ticket.Status status) {
    return status == Ticket.Status.RESOLVED
        || status == Ticket.Status.CLOSED
        || status == Ticket.Status.CANCELLED;
  }

  private double durationInHours(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return 0.0;
    }
    return Duration.between(start, end).toMinutes() / 60.0;
  }

  private double round(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  private String buildAgentName(AgentDTO agent) {
    if (agent.getFullName() != null) {
      return agent.getFullName();
    }
    String first = Optional.ofNullable(agent.getFirstName()).orElse("");
    String last = Optional.ofNullable(agent.getLastName()).orElse("");
    String name = (first + " " + last).trim();
    if (!name.isEmpty()) {
      return name;
    }
    return Optional.ofNullable(agent.getEmail()).orElse("Agent " + agent.getId());
  }

  private <K> Map<K, Long> orderByValueDesc(Map<K, Long> source) {
    return source.entrySet().stream()
        .sorted(Map.Entry.<K, Long>comparingByValue().reversed())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
  }
}
