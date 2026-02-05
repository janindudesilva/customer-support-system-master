package com.customersupport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "admin_id", nullable = false)
  private User admin;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String feedback;

  @Column(nullable = false)
  private Integer rating;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PENDING;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @PrePersist
  public void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public enum Status {
    PENDING,
    APPROVED,
    REJECTED
  }
}
