package com.customersupport.dto;

import com.customersupport.entity.Company;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
  private Long id;
  private String name;
  private String email;
  private String phone;
  private String address;
  private String website;
  private Company.CompanyStatus status;
  private Company.SubscriptionPlan subscriptionPlan;
  private Integer maxAgents;
  private Integer maxCustomers;
  private Integer currentAgents;
  private Integer currentCustomers;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<CompanyAdminInfo> companyAdmins;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CompanyAdminInfo {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
  }

  public static CompanyDTO fromEntity(Company company) {
    CompanyDTO dto = new CompanyDTO();
    dto.setId(company.getId());
    dto.setName(company.getName());
    dto.setEmail(company.getEmail());
    dto.setPhone(company.getPhone());
    dto.setAddress(company.getAddress());
    dto.setWebsite(company.getWebsite());
    dto.setStatus(company.getStatus());
    dto.setSubscriptionPlan(company.getSubscriptionPlan());
    dto.setMaxAgents(company.getMaxAgents());
    dto.setMaxCustomers(company.getMaxCustomers());
    dto.setCreatedAt(company.getCreatedAt());
    dto.setUpdatedAt(company.getUpdatedAt());
    return dto;
  }
}
