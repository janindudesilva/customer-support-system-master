package com.customersupport.dto;

import com.customersupport.entity.Company;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegistrationDTO {
  @NotBlank(message = "Company name is required")
  @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
  private String phone;

  private String address;
  private String website;
  private Company.SubscriptionPlan subscriptionPlan = Company.SubscriptionPlan.BASIC;
  private Integer maxAgents = 10;
  private Integer maxCustomers = 1000;

  // Company admin details
  @NotBlank(message = "Admin first name is required")
  private String adminFirstName;

  @NotBlank(message = "Admin last name is required")
  private String adminLastName;

  @NotBlank(message = "Admin email is required")
  @Email(message = "Please provide a valid admin email address")
  private String adminEmail;

  @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid admin phone number")
  private String adminPhone;

  @NotBlank(message = "Admin password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  private String adminPassword;
}
