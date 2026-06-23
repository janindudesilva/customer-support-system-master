package com.customersupport.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "customer_type",
      columnDefinition = "ENUM('INDIVIDUAL', 'BUSINESS') DEFAULT 'INDIVIDUAL'")
  private CustomerType customerType = CustomerType.INDIVIDUAL;

  @Column(name = "date_of_birth")
  private LocalDateTime dateOfBirth;

  @Column(columnDefinition = "TEXT")
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "preferred_contact_method",
      columnDefinition = "ENUM('EMAIL', 'PHONE', 'SMS') DEFAULT 'EMAIL'")
  private ContactMethod preferredContactMethod = ContactMethod.EMAIL;

  @Column(columnDefinition = "VARCHAR(50) DEFAULT 'UTC'")
  private String timezone = "UTC";

  @Column(name = "language_preference", columnDefinition = "VARCHAR(10) DEFAULT 'en'")
  private String languagePreference = "en";

  @Column(name = "satisfaction_score", precision = 3, scale = 2)
  private BigDecimal satisfactionScore = BigDecimal.ZERO;

  @Column(name = "total_tickets")
  private int totalTickets = 0;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Ticket> tickets;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Review> reviews;

  public enum CustomerType {
    INDIVIDUAL,
    BUSINESS
  }

  public enum ContactMethod {
    EMAIL,
    PHONE,
    SMS
  }
}
