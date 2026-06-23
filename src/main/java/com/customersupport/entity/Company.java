package com.customersupport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(length = 12)
  private String phone;

  @Column(columnDefinition = "TEXT")
  private String address;

  @Column(length = 255)
  private String website;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE'")
  private CompanyStatus status = CompanyStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "subscription_plans",
      columnDefinition = "ENUM('BASIC', 'PREMIUM', 'ENTERPRISE') DEFAULT 'BASIC'")
  private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

  @Column(name = "max_agent")
  private Integer maxAgents = 10;

  @Column(name = "max_customers")
  private Integer maxCustomers = 1000;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "created_date")
  private Long createdBy;

  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<User> users;

  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<Customer> customers;

  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<Agent> agents;

  public enum CompanyStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
  }

  public enum SubscriptionPlan {
    BASIC,
    PREMIUM,
    ENTERPRISE
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
