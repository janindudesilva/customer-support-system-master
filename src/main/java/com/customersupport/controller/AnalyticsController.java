package com.customersupport.controller;

import com.customersupport.dto.AnalyticsDTO;
import com.customersupport.entity.Analytics;
import com.customersupport.service.AnalyticsService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

  @Autowired private AnalyticsService analyticsService;

  // Save analytics data
  @PostMapping
  public ResponseEntity<?> saveAnalytics(@Valid @RequestBody AnalyticsDTO analyticsDTO) {
    try {
      AnalyticsDTO savedAnalytics = analyticsService.saveAnalytics(analyticsDTO);
      return ResponseEntity.ok(savedAnalytics);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get analytics by ID
  @GetMapping("/{id}")
  public ResponseEntity<AnalyticsDTO> getAnalyticsById(@PathVariable Long id) {
    return analyticsService
        .getAnalyticsById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get analytics by company
  @GetMapping("/company/{companyId}")
  public ResponseEntity<List<AnalyticsDTO>> getAnalyticsByCompany(@PathVariable Long companyId) {
    List<AnalyticsDTO> analytics = analyticsService.getAnalyticsByCompany(companyId);
    return ResponseEntity.ok(analytics);
  }

  // Get analytics by company and metric type
  @GetMapping("/company/{companyId}/type/{metricType}")
  public ResponseEntity<List<AnalyticsDTO>> getAnalyticsByCompanyAndMetricType(
      @PathVariable Long companyId, @PathVariable Analytics.MetricType metricType) {
    List<AnalyticsDTO> analytics =
        analyticsService.getAnalyticsByCompanyAndMetricType(companyId, metricType);
    return ResponseEntity.ok(analytics);
  }

  // Get analytics by company, metric type, and metric name
  @GetMapping("/company/{companyId}/type/{metricType}/name/{metricName}")
  public ResponseEntity<List<AnalyticsDTO>> getAnalyticsByCompanyAndMetricTypeAndMetricName(
      @PathVariable Long companyId,
      @PathVariable Analytics.MetricType metricType,
      @PathVariable String metricName) {
    List<AnalyticsDTO> analytics =
        analyticsService.getAnalyticsByCompanyAndMetricTypeAndMetricName(
            companyId, metricType, metricName);
    return ResponseEntity.ok(analytics);
  }

  // Get analytics by company, metric type, and date range
  @GetMapping("/company/{companyId}/type/{metricType}/range")
  public ResponseEntity<List<AnalyticsDTO>> getAnalyticsByCompanyAndMetricTypeAndDateRange(
      @PathVariable Long companyId,
      @PathVariable Analytics.MetricType metricType,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    List<AnalyticsDTO> analytics =
        analyticsService.getAnalyticsByCompanyAndMetricTypeAndDateRange(
            companyId, metricType, startDate, endDate);
    return ResponseEntity.ok(analytics);
  }

  // Delete analytics
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteAnalytics(@PathVariable Long id) {
    try {
      analyticsService.deleteAnalytics(id);
      return ResponseEntity.ok().body("Analytics deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Generate daily ticket analytics for a company
  @PostMapping("/company/{companyId}/generate-daily")
  public ResponseEntity<?> generateDailyTicketAnalytics(
      @PathVariable Long companyId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam int openTickets,
      @RequestParam int resolvedTickets,
      @RequestParam int averageResolutionTime,
      @RequestParam double customerSatisfaction) {
    try {
      analyticsService.generateDailyTicketAnalytics(
          companyId,
          date,
          openTickets,
          resolvedTickets,
          averageResolutionTime,
          java.math.BigDecimal.valueOf(customerSatisfaction));
      return ResponseEntity.ok().body("Daily analytics generated successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Generate monthly ticket analytics for a company
  @PostMapping("/company/{companyId}/generate-monthly")
  public ResponseEntity<?> generateMonthlyTicketAnalytics(
      @PathVariable Long companyId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam int totalTickets,
      @RequestParam int resolvedTickets,
      @RequestParam int averageResolutionTime,
      @RequestParam double customerSatisfaction) {
    try {
      analyticsService.generateMonthlyTicketAnalytics(
          companyId,
          date,
          totalTickets,
          resolvedTickets,
          averageResolutionTime,
          java.math.BigDecimal.valueOf(customerSatisfaction));
      return ResponseEntity.ok().body("Monthly analytics generated successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
