package com.customersupport.dto;

import com.customersupport.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

  private Long id;

  @NotNull(message = "Ticket ID is required")
  private Long ticketId;

  private String ticketNumber;

  private Long customerId;

  private String customerName;

  private Long agentId;

  private String agentName;

  private Long companyId;

  private String companyName;

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be between 1 and 5")
  @Max(value = 5, message = "Rating must be between 1 and 5")
  private Integer rating;

  private String feedback;

  @Min(value = 1, message = "Service quality rating must be between 1 and 5")
  @Max(value = 5, message = "Service quality rating must be between 1 and 5")
  private Integer serviceQualityRating;

  @Min(value = 1, message = "Response time rating must be between 1 and 5")
  @Max(value = 5, message = "Response time rating must be between 1 and 5")
  private Integer responseTimeRating;

  @Min(value = 1, message = "Professionalism rating must be between 1 and 5")
  @Max(value = 5, message = "Professionalism rating must be between 1 and 5")
  private Integer professionalismRating;

  private Boolean wouldRecommend = true;

  private String additionalComments;

  private Boolean isPublished = false;

  private Boolean isFeatured = false;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private Double averageRating;

  public static ReviewDTO fromEntity(Review review) {
    ReviewDTO dto = new ReviewDTO();

    dto.setId(review.getId());
    dto.setTicketId(review.getTicket().getId());
    dto.setTicketNumber(review.getTicket().getTicketNumber());
    dto.setCustomerId(review.getCustomer().getId());
    dto.setCustomerName(
        review.getCustomer().getUser().getFirstname()
            + " "
            + review.getCustomer().getUser().getLastname());

    if (review.getAgent() != null) {
      dto.setAgentId((review.getAgent().getId()));
      dto.setAgentName(
          review.getAgent().getUser().getFullName()
              + " "
              + review.getAgent().getUser().getLastname());
    }

    dto.setCompanyId(review.getCompany().getId());
    dto.setCompanyName(review.getCompany().getName());
    dto.setRating(review.getRating());
    dto.setFeedback(review.getFeedback());
    dto.setServiceQualityRating(review.getServiceQualityRating());
    dto.setResponseTimeRating(review.getResponseTimeRating());
    dto.setProfessionalismRating(review.getProfessionalismRating());
    dto.setWouldRecommend(review.getWouldRecommend());
    dto.setAdditionalComments(review.getAdditionalComments());
    dto.setIsPublished(review.getIsPublished());
    dto.setIsFeatured(review.getIsFeatured());
    dto.setCreatedAt(review.getCreatedAt());
    dto.setUpdatedAt(review.getUpdatedAt());
    dto.setAverageRating(review.getAverageRating());

    return dto;
  }
}
