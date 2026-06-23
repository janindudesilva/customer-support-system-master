package com.customersupport.service;

import com.customersupport.dto.ReviewAnalyticsDTO;
import com.customersupport.dto.ReviewDTO;
import com.customersupport.entity.*;
import com.customersupport.repository.*;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ReviewService {

  private static final long REVIEW_EDIT_WINDOW_HOURS = 24L;

  @Autowired private ReviewRepository reviewRepository;

  @Autowired private TicketRepository ticketRepository;

  @Autowired private CustomerRepository customerRepository;

  @Autowired private AgentRepository agentRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EmailService emailService;

  // Create review
  public ReviewDTO createReview(ReviewDTO reviewDTO) {
    // Check if a review already exists fo this ticket
    if (reviewRepository.findByTicketId(reviewDTO.getTicketId()).isPresent()) {
      throw new RuntimeException("A review already exists for this ticket");
    }

    Ticket ticket =
        ticketRepository
            .findById(reviewDTO.getTicketId())
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Only resolved tickets can be reviewed
    if (ticket.getStatus() != Ticket.Status.RESOLVED
        && ticket.getStatus() != Ticket.Status.CLOSED) {
      throw new RuntimeException("Only resolved or closed tickets can be reviewed");
    }

    Customer customer =
        customerRepository
            .findById(reviewDTO.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    // Verify this ticket belongs to the customer
    if (!ticket.getCustomer().getId().equals(customer.getId())) {
      throw new RuntimeException("This ticket does not belong to the customer");
    }

    Agent agent = null;
    if (ticket.getAgent() != null) {
      agent =
          agentRepository
              .findById(ticket.getAgent().getId())
              .orElseThrow(() -> new RuntimeException("Agent not found"));
    }

    Company company =
        companyRepository
            .findById(ticket.getCompany().getId())
            .orElseThrow(() -> new RuntimeException("Company not found"));

    Review review = new Review();
    review.setTicket(ticket);
    review.setCustomer(customer);
    review.setAgent(agent);
    review.setCompany(company);
    review.setRating(reviewDTO.getRating());
    review.setFeedback(reviewDTO.getFeedback());
    review.setServiceQualityRating(reviewDTO.getServiceQualityRating());
    review.setResponseTimeRating(reviewDTO.getResponseTimeRating());
    review.setProfessionalismRating(reviewDTO.getProfessionalismRating());
    review.setWouldRecommend(reviewDTO.getWouldRecommend());
    review.setAdditionalComments(reviewDTO.getAdditionalComments());

    // New reviews are not published or featured by default
    review.setIsPublished(false);
    review.setIsFeatured(false);

    Review savedReview = reviewRepository.save(review);

    // Find company admins for this company
    List<User> companyAdmins =
        userRepository.findByCompanyIdAndRoleName(company.getId(), "COMPANY_ADMIN");

    // Send email notification to all company admins
    if (companyAdmins != null && !companyAdmins.isEmpty()) {
      for (User admin : companyAdmins) {
        emailService.sendNewReviewNotification(savedReview, admin);
      }
    }

    recalculateAgentSatisfaction(agent);
    return ReviewDTO.fromEntity(savedReview);
  }

  // get review by ID
  public Optional<ReviewDTO> getReviewById(Long id) {
    return reviewRepository.findById(id).map(ReviewDTO::fromEntity);
  }

  // get review by ticket ID
  public Optional<ReviewDTO> getReviewByTicketId(Long ticketId) {
    return reviewRepository.findByTicketId(ticketId).map(ReviewDTO::fromEntity);
  }

  // Get reviews by customer
  public List<ReviewDTO> getReviewsByCustomer(Long customerId) {
    return reviewRepository.findByCustomerId(customerId).stream()
        .map(ReviewDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get reviews by agent
  public List<ReviewDTO> getReviewsByAgent(Long agentId) {
    return reviewRepository.findByAgentIdOrderByCreatedAtDesc(agentId).stream()
        .map(ReviewDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get reviews by company
  public List<ReviewDTO> getReviewsByCompany(Long companyId) {
    return reviewRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
        .map(ReviewDTO::fromEntity)
        .collect(Collectors.toList());
  }

  public List<ReviewDTO> getPublishedReviewsByCompany(Long companyId, Integer limit) {
    List<Review> publishedReviews = reviewRepository.findPublishedReviewsByCompany(companyId);
    Stream<Review> reviewStream = publishedReviews.stream();

    if (limit != null && limit > 0) {
      reviewStream = reviewStream.limit(limit);
    }

    return reviewStream.map(ReviewDTO::fromEntity).collect(Collectors.toList());
  }

  public List<ReviewDTO> getReviewsByCompanyWithFilters(
      Long companyId, Long agentId, LocalDate startDate, LocalDate endDate) {
    LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
    LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

    return reviewRepository.findByCompanyWithFilters(companyId, agentId, start, end).stream()
        .map(ReviewDTO::fromEntity)
        .collect(Collectors.toList());
  }

  public ReviewAnalyticsDTO calculateAnalytics(List<ReviewDTO> reviews) {
    ReviewAnalyticsDTO analytics = new ReviewAnalyticsDTO();

    if (reviews == null || reviews.isEmpty()) {
      Map<Integer, Long> distribution = new HashMap<>();
      for (int rating = 1; rating <= 5; rating++) {
        distribution.put(rating, 0L);
      }
      analytics.setRatingDistribution(distribution);
      analytics.setTotalReviews(0L);
      return analytics;
    }

    long totalReviews = reviews.size();
    analytics.setTotalReviews(totalReviews);

    analytics.setAverageRating(
        reviews.stream()
            .filter(review -> review.getRating() != null)
            .mapToInt(ReviewDTO::getRating)
            .average()
            .orElse(0.0));

    analytics.setServiceQualityAverage(
        reviews.stream()
            .filter(review -> review.getServiceQualityRating() != null)
            .mapToInt(ReviewDTO::getServiceQualityRating)
            .average()
            .orElse(0.0));

    analytics.setResponseTimeAverage(
        reviews.stream()
            .filter(review -> review.getResponseTimeRating() != null)
            .mapToInt(ReviewDTO::getResponseTimeRating)
            .average()
            .orElse(0.0));

    analytics.setProfessionalismAverage(
        reviews.stream()
            .filter(review -> review.getProfessionalismRating() != null)
            .mapToInt(ReviewDTO::getProfessionalismRating)
            .average()
            .orElse(0.0));

    long recommendations =
        reviews.stream().filter(review -> Boolean.TRUE.equals(review.getWouldRecommend())).count();
    analytics.setRecommendationRate(
        totalReviews > 0 ? (recommendations * 100.0) / totalReviews : 0.0);

    Map<Integer, Long> distribution = new HashMap<>();
    for (int rating = 1; rating <= 5; rating++) {
      distribution.put(rating, 0L);
    }
    reviews.stream()
        .filter(review -> review.getRating() != null)
        .forEach(
            review ->
                distribution.compute(
                    review.getRating(), (key, value) -> value == null ? 1L : value + 1L));
    analytics.setRatingDistribution(distribution);

    return analytics;
  }

  // Get featured reviews by company
  public List<ReviewDTO> getFeaturedReviewsByCompany(Long companyId) {
    return reviewRepository.findFeaturedReviewsByCompany(companyId).stream()
        .map(ReviewDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Update review (Customer can update their own review)
  public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO, Long customerId) {
    Review review =
        reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

    // Verify this review belongs to the customer
    if (!review.getCustomer().getId().equals(customerId)) {
      throw new RuntimeException("You do not have permission to update this review");
    }

    LocalDateTime createdAt = review.getCreatedAt();
    if (createdAt != null
        && createdAt.isBefore(LocalDateTime.now().minusHours(REVIEW_EDIT_WINDOW_HOURS))) {
      throw new RuntimeException("Reviews can only be edited within 24 hours of submission");
    }

    // Update review fields
    review.setRating(reviewDTO.getRating());
    review.setFeedback(reviewDTO.getFeedback());
    review.setServiceQualityRating(reviewDTO.getServiceQualityRating());
    review.setResponseTimeRating(reviewDTO.getResponseTimeRating());
    review.setProfessionalismRating(reviewDTO.getProfessionalismRating());
    review.setWouldRecommend(reviewDTO.getWouldRecommend());
    review.setAdditionalComments(reviewDTO.getAdditionalComments());

    Review updatedReview = reviewRepository.save(review);

    recalculateAgentSatisfaction(review.getAgent());
    return ReviewDTO.fromEntity(updatedReview);
  }

  // Update review publication status (admin only)
  public ReviewDTO updateReviewPublicationStatus(Long id, boolean isPublished) {
    Review review =
        reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

    review.setIsPublished(isPublished);
    Review updatedReview = reviewRepository.save(review);

    return ReviewDTO.fromEntity(updatedReview);
  }

  // Update review featured status (admin only)
  public ReviewDTO updateReviewFeaturedStatus(Long id, boolean isFeatured) {
    Review review =
        reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

    review.setIsFeatured(isFeatured);
    Review updatedReview = reviewRepository.save(review);

    return ReviewDTO.fromEntity(updatedReview);
  }

  // Delete review (admin only)
  public void deleteReview(Long id) {
    Review review =
        reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

    reviewRepository.delete(review);

    recalculateAgentSatisfaction(review.getAgent());
  }

  public void deleteReviewByCustomer(Long id, Long customerId) {
    Review review =
        reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));

    if (!review.getCustomer().getId().equals(customerId)) {
      throw new RuntimeException("You do not have permission to delete this review");
    }

    reviewRepository.delete(review);

    recalculateAgentSatisfaction(review.getAgent());
  }

  // Get average rating by company
  public Double getAverageRatingByCompany(Long companyId) {
    Double average = reviewRepository.getAverageRatingByCompany(companyId);
    return average != null ? average : 0.0;
  }

  // Get average rating by agent
  public Double getAverageRatingByAgent(Long agentId) {
    return reviewRepository.getAverageRatingByAgent(agentId);
  }

  // Get average rating by company and rating
  public Integer getAverageRatingByCompanyAndRating(Long companyId, Integer rating) {
    return reviewRepository.countByCompanyIdAndRating(companyId, rating);
  }

  // Get review count by company and date range
  public Integer getReviewCountByCompanyAndDateRange(
      Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
    return reviewRepository.countByCompanyIdAndDateRange(companyId, startDate, endDate);
  }

  private void recalculateAgentSatisfaction(Agent agent) {
    if (agent == null) {
      return;
    }

    Double avgRating = reviewRepository.getAverageRatingByAgent(agent.getId());
    agent.setCustomerSatisfactionRating(
        avgRating != null ? BigDecimal.valueOf(avgRating) : BigDecimal.ZERO);
    agentRepository.save(agent);
  }
}
