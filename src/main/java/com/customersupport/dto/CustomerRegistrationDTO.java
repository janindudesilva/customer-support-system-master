package com.customersupport.dto;

import com.customersupport.entity.Customer;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationDTO {

  @NotNull(message = "Company ID is required")
  private Long id;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
  private String phone;

  private Customer.CustomerType customerType = Customer.CustomerType.INDIVIDUAL;
  private LocalDateTime dateOfBirth;
  private String address;
  private Customer.ContactMethod preferredContactMethod = Customer.ContactMethod.EMAIL;
  private String timezone = "UTC";
  private String languagePreference = "en";
}
