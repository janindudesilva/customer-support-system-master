package com.customersupport.dto;

import com.customersupport.entity.Analytics;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsDTO {

  private Long id;
  private Long companyId;
  private String companyName;
  private Analytics.MetricType metricType;
  private String metricName;
  private BigDecimal metricValue;
  private String additionalData;
  private LocalDate periodStart;
  private LocalDate periodEnd;
  private LocalDateTime createdAt;

  public static AnalyticsDTO fromEntity(Analytics analytics) {
    AnalyticsDTO dto = new AnalyticsDTO();

    dto.setId((analytics.getId()));
    dto.setCompanyId(analytics.getCompany().getId());
    dto.setCompanyName(analytics.getCompany().getName());
    dto.setMetricType(analytics.getMetricType());
    dto.setMetricName(analytics.getMetricName());
    dto.setMetricValue(analytics.getMetricValue());
    dto.setAdditionalData(analytics.getAdditionalData());
    dto.setPeriodStart(analytics.getPeriodStart());
    dto.setPeriodEnd(analytics.getPeriodEnd());
    dto.setCreatedAt(analytics.getCreatedAt());

    return dto;
  }
}
