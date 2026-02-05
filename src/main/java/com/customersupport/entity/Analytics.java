package com.customersupport.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Analytics {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Enumerated(EnumType.STRING)
  @Column(name = "metric_type", nullable = false)
  private MetricType metricType;

  @Column(name = "metric_name", nullable = false)
  private String metricName;

  @Column(name = "metric_value", precision = 15, scale = 2, nullable = false)
  private BigDecimal metricValue;

  @Column(name = "additional_data", columnDefinition = "JSON")
  private String additionalData;

  @Column(name = "period_start", nullable = false)
  private LocalDate periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDate periodEnd;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  public enum MetricType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
  }
}
