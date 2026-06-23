package com.customersupport.dto;

import com.customersupport.entity.Faq;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqDTO {

  private Long id;

  @NotNull(message = "Company ID is required")
  private Long companyId;

  private String companyName;

  private Long categoryId;

  private String categoryName;

  @NotBlank(message = "Question is required")
  private String question;

  @NotBlank(message = "Answer is required")
  private String answer;

  @NotBlank(message = "Keywords are required")
  private String keywords;

  private Integer viewCount = 0;

  private Integer helpfulCount = 0;

  private Integer notHelpfulCount = 0;

  private Boolean isFeatured = false;

  private Boolean isPublished = true;

  private Integer sortOrder = 0;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private Long createdById;

  private String createdByName;

  private Long updatedById;

  private String updatedByName;

  private Double helpfulnessScore;

  private Integer totalVotes;

  public static FaqDTO fromEntity(Faq faq) {
    FaqDTO dto = new FaqDTO();
    dto.setId(faq.getId());
    dto.setCompanyId(faq.getCompany().getId());
    dto.setCompanyName(faq.getCompany().getName());

    if (faq.getCategory() != null) {
      dto.setCategoryId(faq.getCategory().getId());
      dto.setCategoryName(faq.getCategory().getName());
    }

    dto.setQuestion(faq.getQuestion());
    dto.setAnswer(faq.getAnswer());
    dto.setKeywords(faq.getKeywords());
    dto.setViewCount(faq.getViewCount());
    dto.setHelpfulCount(faq.getHelpfulCount());
    dto.setNotHelpfulCount(faq.getNotHelpfulCount());
    dto.setIsFeatured(faq.getIsFeatured());
    dto.setIsPublished(faq.getIsPublished());
    dto.setSortOrder(faq.getSortOrder());
    dto.setCreatedAt(faq.getCreatedAt());
    dto.setUpdatedAt(faq.getUpdatedAt());

    if (faq.getCreatedBy() != null) {
      dto.setCreatedById(faq.getCreatedBy().getId());
      dto.setCreatedByName(
          faq.getCreatedBy().getFirstname() + " " + faq.getCreatedBy().getLastname());
    }

    if (faq.getUpdatedBy() != null) {
      dto.setUpdatedById(faq.getUpdatedBy().getId());
      dto.setUpdatedByName(
          faq.getUpdatedBy().getFirstname() + " " + faq.getUpdatedBy().getLastname());
    }

    dto.setHelpfulnessScore(faq.getHelpfulnessScore());
    dto.setTotalVotes(faq.getTotalVotes());

    return dto;
  }
}
