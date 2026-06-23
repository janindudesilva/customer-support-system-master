package com.customersupport.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentRegistrationDTO {

  @NotNull(message = "Company ID is required")
  private Long companyId;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
  private String lastName;

  @NotBlank(message = "email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "password must be at least 8 characters")
  private String password;

  @Pattern(regexp = "^$|^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
  private String phone;

  private String department;
  private String specialization;
  private Integer maxConcurrentTickets = 10;
  private LocalDateTime shiftStart;
  private LocalDateTime shiftEnd;
  private String workingDays;
  private Boolean isAvailable = true;
}
