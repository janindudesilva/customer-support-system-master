package com.customersupport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAdminRegistrationDTO {

  @NotNull(message = "Company ID is required")
  private Long companyId;

  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name must not exceed 100 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100, message = "Last name must not exceed 100 characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  private String email;

  @Size(max = 20, message = "Phone must not exceed 20 characters")
  private String phone;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;
}
