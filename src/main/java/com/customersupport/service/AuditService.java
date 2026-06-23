package com.customersupport.service;

import com.customersupport.entity.AuditLog;
import com.customersupport.entity.Company;
import com.customersupport.entity.User;
import com.customersupport.repository.AuditLogRepository;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuditService {

  @Autowired private AuditLogRepository auditLogRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private ObjectMapper objectMapper;

  // Log an audit event
  public void logEvent(
      Long companyId,
      Long userId,
      String action,
      String resourceType,
      Long resourceId,
      Object oldValues,
      Object newValues,
      String ipAddress,
      String userAgent,
      AuditLog.Severity severity,
      String details) {
    try {
      AuditLog auditLog = new AuditLog();

      // Set company if provided
      if (companyId != null) {
        Company company = companyRepository.findById(companyId).orElse(null);
        auditLog.setCompany(company);
      }

      // Set user if provided
      if (userId != null) {
        User user = userRepository.findById(userId).orElse(null);
        auditLog.setUser(user);
      }

      auditLog.setAction(action);
      auditLog.setResourceType(details);
      auditLog.setResourceId(resourceId);

      // Convert old and new values to JSON strings
      if (oldValues != null) {
        auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
      }

      if (newValues != null) {
        auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
      }

      auditLog.setIpAddress(ipAddress);
      auditLog.setUserAgent(userAgent);
      auditLog.setSeverity(severity);
      auditLog.setDetails(details);
      auditLog.setCreatedAt(LocalDateTime.now());

      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      System.err.println("Error login audit event " + e.getMessage());
    }
  }

  // Get recent audit logs
  public List<AuditLog> getRecentAuditLogs(int limit) {
    return auditLogRepository.findRecentAuditLogs(limit);
  }

  // Get recent audit log by company
  public List<AuditLog> findRecentAuditLogsByCompany(Long companyId, int limit) {
    return auditLogRepository.findRecentAuditLogsByCompany(companyId, limit);
  }

  // Get recent audit log by user
  public List<AuditLog> findRecentAuditLogsByUser(Long userId, int limit) {
    return auditLogRepository.findRecentAuditLogsByUser(userId, limit);
  }

  // Get audit logs by company
  public List<AuditLog> getAuditLogsByCompany(Long companyId) {
    return auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
  }

  // get audit logs by user
  public List<AuditLog> getAuditLogsByUser(Long userId) {
    return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  // Get audit logs by date range
  public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return auditLogRepository.findByDateRangeOrderByCreatedAtDesc(
        startDate.toLocalDate(), endDate.toLocalDate());
  }

  // Get audit logs by company and date range
  public List<AuditLog> getAuditLogsByCompanyAndDateRange(
      Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
    return auditLogRepository.findByCompanyIdAndDateRangeOrderByCreatedAtDesc(
        companyId, startDate.toLocalDate(), endDate.toLocalDate());
  }

  // Get audit logs by action
  public List<AuditLog> getAuditLogsByAction(String action) {
    return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
  }

  // Get audit logs by severity
  public List<AuditLog> getAuditLogsBySeverity(AuditLog.Severity severity) {
    return auditLogRepository.findBySeverityOrderByCreatedAtDesc(severity);
  }

  // Get audit logs by resource type and id
  public List<AuditLog> getAuditLogsByResource(String resourceType, Long resourceId) {
    return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
  }

  // Additional methods to find by userId and companyId
  public List<AuditLog> findByUserId(Long userId) {
    return auditLogRepository.findByUserId(userId);
  }

  public List<AuditLog> findByCompanyId(Long companyId) {
    return auditLogRepository.findByCompanyId(companyId);
  }
}
