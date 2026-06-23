package com.customersupport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id", nullable = false, unique = true)
  private Ticket ticket;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agent_id", nullable = false)
  private Agent agent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  // New added
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(nullable = false)
  private Integer rating;

  @Column(columnDefinition = "TEXT")
  private String feedback;

  @Column(name = "service_quality_rating")
  private Integer serviceQualityRating;

  @Column(name = "response_time_rating")
  private Integer responseTimeRating;

  @Column(name = "professionalism_rating")
  private Integer professionalismRating;

  @Column(name = "would_recommend")
  private Boolean wouldRecommend;

  @Column(name = "additional_comments", columnDefinition = "TEXT")
  private String additionalComments;

  @Column(name = "is_published")
  private Boolean isPublished = false;

  @Column(name = "is_featured")
  private Boolean isFeatured = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Double getAverageRating() {
    int count = 0;
    int total = 0;

    if (rating != null) {
      total += rating;
      count++;
    }

    if (serviceQualityRating != null) {
      total += serviceQualityRating;
      count++;
    }

    if (responseTimeRating != null) {
      total += responseTimeRating;
      count++;
    }

    if (professionalismRating != null) {
      total += professionalismRating;
      count++;
    }

    return count > 0 ? (double) total / count : 0.0;
  }
}
