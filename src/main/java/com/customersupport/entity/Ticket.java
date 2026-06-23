package com.customersupport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
  private String ticketNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agent_id")
  private Agent agent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM'")
  private Priority priority = Priority.MEDIUM;

  @Enumerated(EnumType.STRING)
  @Column(
      columnDefinition =
          "ENUM('OPEN', 'IN_PROGRESS', 'PENDING_CUSTOMER', 'RESOLVED', 'CLOSED', 'CANCELLED')"
              + " DEFAULT 'OPEN'")
  private Status status = Status.OPEN;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('WEB', 'EMAIL', 'PHONE', 'CHAT') DEFAULT 'WEB'")
  private Source source = Source.WEB;

  @Column(columnDefinition = "TEXT")
  private String resolution;

  @Column(columnDefinition = "JSON")
  private String tags;

  @Column(columnDefinition = "JSON")
  private String attachment;

  @Column(name = "estimated_resolution_time")
  private LocalDateTime estimatedResolutionTime;

  @Column(name = "actual_resolution_time")
  private LocalDateTime actualResolutionTime;

  @Column(name = "first_response_time")
  private LocalDateTime firstResponseTime;

  @Column(name = "last_activity")
  private LocalDateTime lastActivity = LocalDateTime.now();

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  //    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  //    @JsonIgnore
  //    private List<TicketResponse> responses;

  @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private Review review;

  public enum Source {
    WEB,
    EMAIL,
    PHONE,
    CHAT
  }

  public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
    this.lastActivity = LocalDateTime.now();
  }

  @PrePersist
  public void prePersist() {
    if (this.ticketNumber == null) {
      this.ticketNumber = generateTicketNumber();
    }
  }

  private String generateTicketNumber() {
    LocalDateTime now = LocalDateTime.now();
    String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss").format(now);
    int random = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 1000);
    return String.format("T%s%03d", timestamp, random);
  }

  public boolean canBeEditedByCustomer() {
    return this.status == Status.OPEN && this.agent == null;
  }

  public boolean canBeCancelledByCustomer() {
    return this.status == Status.OPEN || this.status == Status.IN_PROGRESS;
  }

  public enum Status {
    OPEN,
    IN_PROGRESS,
    PENDING_CUSTOMER,
    RESOLVED,
    CLOSED,
    CANCELLED
  }
}
