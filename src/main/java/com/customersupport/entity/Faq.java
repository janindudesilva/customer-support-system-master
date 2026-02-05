package com.customersupport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faq {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String question;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String answer;

  @Column(columnDefinition = "TEXT")
  private String keywords;

  @Column(name = "view_count")
  private Integer viewCount = 0;

  @Column(name = "helpful_count")
  private Integer helpfulCount = 0;

  @Column(name = "not_helpful_count")
  private Integer notHelpfulCount = 0;

  @Column(name = "is_featured")
  private Boolean isFeatured = false;

  @Column(name = "is_published")
  private Boolean isPublished = true;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;

  @OneToMany(mappedBy = "faq", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<FaqVoting> votings;

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Double getHelpfulnessScore() {
    int totalVotes = helpfulCount + notHelpfulCount;
    if (totalVotes == 0) return 0.0;
    return ((double) helpfulCount / totalVotes) * 100;
  }

  public Integer getTotalVotes() {
    return helpfulCount + notHelpfulCount;
  }

  public void incrementViewCount() {
    this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
  }
}
