package com.customersupport.repository;

import com.customersupport.entity.AuditLog;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  List<AuditLog> findByUserId(Long userId);

  List<AuditLog> findByCompanyId(Long companyId);

  List<AuditLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId);

  @Query("SELECT a FROM AuditLog a WHERE a.company.id = :companyId ORDER BY a.createdAt DESC")
  List<AuditLog> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") Long companyId);

  @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
  List<AuditLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY"
          + " a.createdAt DESC")
  List<AuditLog> findByDateRangeOrderByCreatedAtDesc(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.company.id = :companyId AND a.createdAt BETWEEN :startDate"
          + " AND :endDate ORDER BY a.createdAt DESC")
  List<AuditLog> findByCompanyIdAndDateRangeOrderByCreatedAtDesc(
      @Param("companyId") Long companyId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.createdAt DESC")
  List<AuditLog> findByActionOrderByCreatedAtDesc(@Param("action") String action);

  @Query("SELECT a FROM AuditLog a WHERE a.severity = :severity ORDER BY a.createdAt DESC")
  List<AuditLog> findBySeverityOrderByCreatedAtDesc(@Param("severity") AuditLog.Severity severity);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.company.id = :companyId AND a.user.id = :userId ORDER BY"
          + " a.createdAt DESC")
  List<AuditLog> findByCompanyIdAndUserIdOrderByCreatedAtDesc(
      @Param("companyId") Long companyId, @Param("userId") Long userId);

  @Query("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC LIMIT :limit")
  List<AuditLog> findRecentAuditLogs(@Param("limit") int limit);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.company.id = :companyId ORDER BY a.createdAt DESC LIMIT"
          + " :limit")
  List<AuditLog> findRecentAuditLogsByCompany(
      @Param("companyId") Long companyId, @Param("limit") int limit);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC LIMIT :limit")
  List<AuditLog> findRecentAuditLogsByUser(@Param("userId") Long userId, @Param("limit") int limit);
}
