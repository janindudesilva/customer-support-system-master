package com.customersupport.dto;

import com.customersupport.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
  private Long id;
  private Long companyId;
  private String companyName;
  private Long roleId;
  private String roleName;

  @Email(message = "enter correct email type")
  private String email;

  @NotBlank private String firstName;

  @NotBlank private String lastName;
  private String fullName;

  private String phone;
  private User.UserStatus status;
  private Boolean emailVerified;
  private LocalDateTime lastLogin;
  private LocalDateTime created;
  private LocalDateTime updated;

  public static UserDTO fromEntity(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setCompanyId(user.getCompany() != null ? user.getCompany().getId() : null);
    dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : null);
    dto.setRoleId(user.getRole().getId());
    dto.setRoleName(user.getRole().getName());
    dto.setEmail(user.getEmail());
    dto.setFirstName(user.getFirstname());
    dto.setLastName(user.getLastname());
    dto.setPhone(user.getPhone());
    dto.setStatus(user.getStatus());
    dto.setEmailVerified(user.getEmailVerified());
    dto.setLastLogin(user.getLastLogin());
    dto.setCreated(user.getCreatedAt());
    dto.setUpdated(user.getUpdatedAt());
    dto.setFullName(user.getFirstname() + " " + user.getLastname());
    return dto;
  }
}
