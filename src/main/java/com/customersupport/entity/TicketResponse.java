package com.customersupport.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_responses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponse {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id", nullable = false)
  private Ticket ticket;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "response_type",
      columnDefinition =
          "ENUM('CUSTOMER_REPLY', 'AGENT_REPLY', 'INTERNAL_NOTE', 'SYSTEM_UPDATE') DEFAULT"
              + " 'AGENT_REPLY'")
  private ResponseType responseType = ResponseType.AGENT_REPLY;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String message;

  @Column(columnDefinition = "JSON")
  private String attachments;

  @Column(name = "is_public")
  private Boolean isPublic = true;

  @Column(name = "response_time", precision = 8, scale = 2)
  private BigDecimal responseTime; // Time taken to respond in hours

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  public enum ResponseType {
    CUSTOMER_REPLY,
    AGENT_REPLY,
    INTERNAL_NOTE,
    SYSTEM_UPDATE
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public boolean isFromAgent() {
    return this.responseType == ResponseType.AGENT_REPLY
        || this.responseType == ResponseType.INTERNAL_NOTE;
  }

  public boolean isFromCustomer() {
    return this.responseType == ResponseType.CUSTOMER_REPLY;
  }
}
