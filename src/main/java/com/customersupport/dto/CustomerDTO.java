package com.customersupport.dto;

import com.customersupport.entity.Customer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
  private Long id;
  private Long userId;
  private String firstName;
  private String lastName;
  private String fullName;
  private String email;
  private String phone;
  private Long companyId;
  private String companyName;
  private Customer.CustomerType customerType;
  private LocalDateTime dateOfBirth;
  private String address;
  private Customer.ContactMethod preferredContactMethod;
  private String timeZone;
  private String languagePreference;
  private BigDecimal satisfactionScore;
  private Integer totalTickets;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static CustomerDTO fromEntity(Customer customer) {
    CustomerDTO dto = new CustomerDTO();
    dto.setId(customer.getId());
    dto.setUserId(customer.getUser().getId());
    dto.setFirstName((customer.getUser().getFirstname()));
    dto.setLastName(customer.getUser().getLastname());
    dto.setEmail(customer.getUser().getEmail());
    dto.setPhone(customer.getUser().getPhone());
    dto.setCompanyId(customer.getCompany().getId());
    dto.setCompanyName(customer.getCompany().getName());
    dto.setCustomerType(customer.getCustomerType());
    dto.setDateOfBirth(customer.getDateOfBirth());
    dto.setAddress(customer.getAddress());
    dto.setPreferredContactMethod(customer.getPreferredContactMethod());
    dto.setTimeZone(customer.getTimezone());
    dto.setLanguagePreference(customer.getLanguagePreference());
    dto.setSatisfactionScore(customer.getSatisfactionScore());
    dto.setTotalTickets(customer.getTotalTickets());
    dto.setCreatedAt(customer.getCreatedAt());
    dto.setUpdatedAt(customer.getUpdatedAt());
    return dto;
  }
}
