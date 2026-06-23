package com.customersupport.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  private String department;

  @Column(columnDefinition = "TEXT")
  private String specialization;

  @Column(name = "max_concurrent_tickets")
  private Integer maxConcurrentTickets = 10;

  @Column(name = "current_ticket_count")
  private Integer currentTicketCount = 0;

  @Column(name = "total_tickets_handled")
  private Integer totalTicketsHandled = 0;

  @Column(name = "average_resolution_time", precision = 8, scale = 2)
  private BigDecimal averageResolutionTime = BigDecimal.ZERO;

  @Column(name = "customer_satisfaction_rating", precision = 3, scale = 2)
  private BigDecimal customerSatisfactionRating = BigDecimal.ZERO;

  @Column(name = "is_available")
  private Boolean isAvailable = true;

  @Column(name = "shift_start")
  private LocalDateTime shiftStart;

  @Column(name = "shift_end")
  private LocalDateTime shiftEnd;

  @Column(name = "working_days", columnDefinition = "JSON")
  private String workingDays;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)
  private List<Ticket> assignedTickets;

  @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)
  private List<Review> reviews;
}
