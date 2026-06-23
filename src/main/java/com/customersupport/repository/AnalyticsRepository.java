package com.customersupport.repository;

import com.customersupport.entity.Analytics;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {

  List<Analytics> findByCompanyId(Long companyId);

  List<Analytics> findByCompanyIdAndMetricType(Long companyId, Analytics.MetricType metricType);

  Optional<Analytics> findByCompanyIdAndMetricTypeAndMetricNameAndPeriodStart(
      Long companyId, Analytics.MetricType metricType, String metricName, LocalDate periodStart);

  @Query(
      "SELECT a FROM Analytics a WHERE a.company.id = :companyId AND a.metricType = :metricType AND"
          + " a.metricName = :metricName ORDER BY a.periodStart DESC")
  List<Analytics> findByCompanyIdAndMetricTypeAndMetricNameOrderByPeriodStartDesc(
      @Param("companyId") Long companyId,
      @Param("metricType") Analytics.MetricType metricType,
      @Param("metricName") String metricName);

  @Query(
      "SELECT a FROM Analytics a WHERE a.company.id = :companyId AND a.metricType = :metricType AND"
          + " a.periodStart BETWEEN :startDate AND :endDate ORDER BY a.periodStart ASC")
  List<Analytics> findByCompanyIdAndMetricTypeAndDateRange(
      @Param("companyId") Long companyId,
      @Param("metricType") Analytics.MetricType metricType,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
