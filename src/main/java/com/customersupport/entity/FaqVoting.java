package com.customersupport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faq_voting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqVoting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "faq_id", nullable = false)
  private Faq faq;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "session_id")
  private String sessionId; // For anonymous voting

  @Column(name = "is_helpful", nullable = false)
  private Boolean isHelpful;

  @Column(name = "feedback")
  private String feedback;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "ip_address")
  private String ipAddress;
}
