package com.customersupport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  @JsonIgnore
  private String password;

  @Column(name = "first_name", nullable = false)
  private String firstname;

  @Column(name = "last_name", nullable = false)
  private String lastname;

  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE'")
  private UserStatus status = UserStatus.ACTIVE;

  @Column(name = "email_verified")
  private Boolean emailVerified = false;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts = 0;

  @Column(name = "password_reset_token")
  private String passwordResetToken;

  @Column(name = "password_reset_expires")
  private LocalDateTime passwordResetExpires;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "created_by")
  private Long createdBy;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Customer customer;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Agent agent;

  public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public String getFullName() {
    return firstname + " " + lastname;
  }
}
