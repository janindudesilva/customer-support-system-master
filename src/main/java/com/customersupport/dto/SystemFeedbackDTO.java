package com.customersupport.dto;

import com.customersupport.entity.SystemFeedback;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemFeedbackDTO {

  private Long id;

  @NotNull(message = "Company ID is required")
  private Long companyId;

  private String companyName;

  @NotNull(message = "Admin ID is required")
  private Long adminId;

  private String adminName;

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Feedback is required")
  private String feedback;

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be between 1 and 5")
  @Max(value = 5, message = "Rating must be between 1 and 5")
  private Integer rating;

  private SystemFeedback.Status status = SystemFeedback.Status.PENDING;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public static SystemFeedbackDTO fromEntity(SystemFeedback feedback) {
    if (feedback == null) {
      return null;
    }

    SystemFeedbackDTO dto = new SystemFeedbackDTO();
    dto.setId(feedback.getId());
    dto.setCompanyId(feedback.getCompany() != null ? feedback.getCompany().getId() : null);
    dto.setCompanyName(feedback.getCompany() != null ? feedback.getCompany().getName() : null);
    dto.setAdminId(feedback.getAdmin() != null ? feedback.getAdmin().getId() : null);
    dto.setAdminName(feedback.getAdmin() != null ? feedback.getAdmin().getFullName() : null);
    dto.setTitle(feedback.getTitle());
    dto.setFeedback(feedback.getFeedback());
    dto.setRating(feedback.getRating());
    dto.setStatus(feedback.getStatus());
    dto.setCreatedAt(feedback.getCreatedAt());
    dto.setUpdatedAt(feedback.getUpdatedAt());
    return dto;
  }
}
