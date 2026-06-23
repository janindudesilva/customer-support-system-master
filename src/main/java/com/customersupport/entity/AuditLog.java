package com.customersupport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private String action;

  @Column(name = "resource_type", nullable = false)
  private String resourceType;

  @Column(name = "resource_id")
  private Long resourceId;

  @Column(name = "old_values", columnDefinition = "JSON")
  private String oldValues;

  @Column(name = "new_values", columnDefinition = "JSON")
  private String newValues;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @Column(name = "session_id")
  private String sessionId;

  @Column(name = "request_url")
  private String requestUrl;

  @Column(name = "http_method")
  private String httpMethod;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "severity",
      columnDefinition = "ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'LOW'")
  private Severity severity = Severity.LOW;

  @Column(name = "details", columnDefinition = "TEXT")
  private String details;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }
}
