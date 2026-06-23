package com.customersupport.service;

import com.customersupport.dto.AnalyticsDTO;
import com.customersupport.entity.Analytics;
import com.customersupport.entity.Company;
import com.customersupport.repository.AnalyticsRepository;
import com.customersupport.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AnalyticsService {

  @Autowired private AnalyticsRepository analyticsRepository;

  @Autowired private CompanyRepository companyRepository;

  // Save analytics data
  public AnalyticsDTO saveAnalytics(AnalyticsDTO analyticsDTO) {
    Company company =
        companyRepository
            .findById(analyticsDTO.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company Not Found"));

    // Check if analytics for this metric and period already exists
    Optional<Analytics> existingAnalytics =
        analyticsRepository.findByCompanyIdAndMetricTypeAndMetricNameAndPeriodStart(
            company.getId(),
            analyticsDTO.getMetricType(),
            analyticsDTO.getMetricName(),
            analyticsDTO.getPeriodStart());

    Analytics analytics;

    if (existingAnalytics.isPresent()) {
      // update exiting analytics
      analytics = existingAnalytics.get();
      analytics.setMetricValue(analyticsDTO.getMetricValue());
      analytics.setAdditionalData(analyticsDTO.getAdditionalData());
      analytics.setPeriodEnd(analyticsDTO.getPeriodEnd());
    } else {
      // Create new analytics
      analytics = new Analytics();
      analytics.setCompany(company);
      analytics.setMetricType(analyticsDTO.getMetricType());
      analytics.setMetricName(analyticsDTO.getMetricName());
      analytics.setMetricValue(analyticsDTO.getMetricValue());
      analytics.setAdditionalData(analyticsDTO.getAdditionalData());
      analytics.setPeriodStart(analyticsDTO.getPeriodStart());
      analytics.setPeriodEnd(analyticsDTO.getPeriodEnd());
    }

    Analytics savedAnalytics = analyticsRepository.save(analytics);
    return AnalyticsDTO.fromEntity(savedAnalytics);
  }

  // Get Analytics by ID
  public Optional<AnalyticsDTO> getAnalyticsById(Long id) {
    return analyticsRepository.findById(id).map(AnalyticsDTO::fromEntity);
  }

  // Get Analytics by Company
  public List<AnalyticsDTO> getAnalyticsByCompany(Long companyId) {
    return analyticsRepository.findByCompanyId(companyId).stream()
        .map(AnalyticsDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get analytics by company and metric type
  public List<AnalyticsDTO> getAnalyticsByCompanyAndMetricType(
      Long companyId, Analytics.MetricType metricType) {
    return analyticsRepository.findByCompanyIdAndMetricType(companyId, metricType).stream()
        .map(AnalyticsDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get analytics by company, metric type and metric name, order by period start
  public List<AnalyticsDTO> getAnalyticsByCompanyAndMetricTypeAndMetricName(
      Long companyId, Analytics.MetricType metricType, String metricName) {
    return analyticsRepository
        .findByCompanyIdAndMetricTypeAndMetricNameOrderByPeriodStartDesc(
            companyId, metricType, metricName)
        .stream()
        .map(AnalyticsDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get analytics by company, metric type and data range
  public List<AnalyticsDTO> getAnalyticsByCompanyAndMetricTypeAndDateRange(
      Long companyId, Analytics.MetricType metricType, LocalDate startDate, LocalDate endDate) {
    return analyticsRepository
        .findByCompanyIdAndMetricTypeAndDateRange(companyId, metricType, startDate, endDate)
        .stream()
        .map(AnalyticsDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Delete analytics
  public void deleteAnalytics(Long id) {
    analyticsRepository.deleteById(id);
  }

  // Generate daily ticket analytics for a company
  public void generateDailyTicketAnalytics(
      Long companyId,
      LocalDate date,
      int openTickets,
      int resolvedTickets,
      int averageResolutionTime,
      BigDecimal customerSatisfaction) {
    // Save open tickets metric
    saveMetric(
        companyId,
        Analytics.MetricType.DAILY,
        "OPEN_TICKETS",
        BigDecimal.valueOf(openTickets),
        date);

    // Save resolved tickets metric
    saveMetric(
        companyId,
        Analytics.MetricType.DAILY,
        "RESOLVED_TICKETS",
        BigDecimal.valueOf(resolvedTickets),
        date);

    // Save average resolution time metric
    saveMetric(
        companyId,
        Analytics.MetricType.DAILY,
        "AVG_RESOLUTION_TIME",
        BigDecimal.valueOf(averageResolutionTime),
        date);

    // Save customer satisfaction metric
    saveMetric(
        companyId, Analytics.MetricType.DAILY, "CUSTOMER_SATISFACTION", customerSatisfaction, date);
  }

  // Generate monthly ticket analytics for a company
  public void generateMonthlyTicketAnalytics(
      Long companyId,
      LocalDate date,
      int totalTickets,
      int resolvedTickets,
      int averageResolutionTime,
      BigDecimal customerSatisfaction) {
    // Get first and last day of the month
    LocalDate firstDayOfMonth = date.withDayOfMonth(1);
    LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());

    // Save total tickets metric
    saveMetric(
        companyId,
        Analytics.MetricType.MONTHLY,
        "TOTAL_TICKETS",
        BigDecimal.valueOf(totalTickets),
        firstDayOfMonth,
        lastDayOfMonth);

    // Save resolved tickets metric
    saveMetric(
        companyId,
        Analytics.MetricType.MONTHLY,
        "RESOLVED_TICKETS",
        BigDecimal.valueOf(resolvedTickets),
        firstDayOfMonth,
        lastDayOfMonth);

    // Save average resolution time metric
    saveMetric(
        companyId,
        Analytics.MetricType.MONTHLY,
        "AVG_RESOLUTION_TIME",
        BigDecimal.valueOf(averageResolutionTime),
        firstDayOfMonth,
        lastDayOfMonth);

    // Save customer satisfaction metric
    saveMetric(
        companyId,
        Analytics.MetricType.MONTHLY,
        "CUSTOMER_SATISFACTION",
        customerSatisfaction,
        firstDayOfMonth,
        lastDayOfMonth);
  }

  // Helper method to save a metric
  private void saveMetric(
      Long companyId,
      Analytics.MetricType metricType,
      String metricName,
      BigDecimal metricValue,
      LocalDate periodStart) {
    saveMetric(companyId, metricType, metricName, metricValue, periodStart, periodStart);
  }

  // Helper method to save a metric with start and end dates
  private void saveMetric(
      Long companyId,
      Analytics.MetricType metricType,
      String metricName,
      BigDecimal metricValue,
      LocalDate periodStart,
      LocalDate periodEnd) {
    Company company =
        companyRepository
            .findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

    // Check if analytics for this metric and period already exists
    Optional<Analytics> existingAnalytics =
        analyticsRepository.findByCompanyIdAndMetricTypeAndMetricNameAndPeriodStart(
            companyId, metricType, metricName, periodStart);

    Analytics analytics;

    if (existingAnalytics.isPresent()) {
      // Update existing analytics
      analytics = existingAnalytics.get();
      analytics.setMetricValue(metricValue);
      analytics.setPeriodEnd(periodEnd);
    } else {
      // Create new analytics
      analytics = new Analytics();
      analytics.setCompany(company);
      analytics.setMetricType(metricType);
      analytics.setMetricName(metricName);
      analytics.setMetricValue(metricValue);
      analytics.setPeriodStart(periodStart);
      analytics.setPeriodEnd(periodEnd);
    }

    analyticsRepository.save(analytics);
  }
}
