package com.customersupport.controller;

import com.customersupport.dto.ReviewDTO;
import com.customersupport.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

  @Autowired private ReviewService reviewService;

  // Create new review
  @PostMapping
  public ResponseEntity<?> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
    try {
      ReviewDTO createdReview = reviewService.createReview(reviewDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get review by  id
  @GetMapping("/{id}")
  public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
    return reviewService
        .getReviewById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get review by ticket id
  @GetMapping("/ticket/{ticketId}")
  public ResponseEntity<ReviewDTO> getReviewByTicketId(@PathVariable Long ticketId) {
    return reviewService
        .getReviewByTicketId(ticketId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get review by customer
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByCustomer(@PathVariable Long customerId) {
    List<ReviewDTO> reviews = reviewService.getReviewsByCustomer(customerId);
    return ResponseEntity.ok(reviews);
  }

  // Get review by agent
  @GetMapping("/agent/{agentId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByAgent(@PathVariable Long agentId) {
    List<ReviewDTO> reviews = reviewService.getReviewsByAgent(agentId);
    return ResponseEntity.ok(reviews);
  }

  // Get review by company
  @GetMapping("/company/{companyId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByCompany(@PathVariable Long companyId) {
    List<ReviewDTO> reviews = reviewService.getReviewsByCompany(companyId);
    return ResponseEntity.ok(reviews);
  }

  // Get published reviews by company (public)
  @GetMapping("/company/{companyId}/published")
  public ResponseEntity<List<ReviewDTO>> getPublishedReviewsByCompany(
      @PathVariable Long companyId, @RequestParam(required = false) Integer limit) {
    List<ReviewDTO> reviews = reviewService.getPublishedReviewsByCompany(companyId, limit);
    return ResponseEntity.ok(reviews);
  }

  // Get featured reviews by company
  @GetMapping("/company/{companyId}/featured")
  public ResponseEntity<List<ReviewDTO>> getFeaturedReviewsByCompany(@PathVariable Long companyId) {
    List<ReviewDTO> reviews = reviewService.getFeaturedReviewsByCompany(companyId);
    return ResponseEntity.ok(reviews);
  }

  // Update review (customer)
  @PutMapping("/{id}")
  public ResponseEntity<?> updateReview(
      @PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO) {
    try {
      if (reviewDTO.getCustomerId() == null) {
        return ResponseEntity.badRequest().body("Customer ID is required to update a review");
      }
      ReviewDTO updatedReview =
          reviewService.updateReview(id, reviewDTO, reviewDTO.getCustomerId());
      return ResponseEntity.ok(updatedReview);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Update review publication status (admin only)
  @PutMapping("/{id}/publish")
  public ResponseEntity<?> publishReview(@PathVariable Long id, @RequestParam boolean publish) {
    try {
      ReviewDTO updateReview = reviewService.updateReviewPublicationStatus(id, publish);
      return ResponseEntity.ok(updateReview);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Update review featured status (admin only)
  @PutMapping("/{id}/feature")
  public ResponseEntity<?> featureReview(@PathVariable Long id, @RequestParam Boolean feature) {
    try {
      ReviewDTO updatedReview = reviewService.updateReviewFeaturedStatus(id, feature);
      return ResponseEntity.ok(updatedReview);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Delete review  (admin only)
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteReview(
      @PathVariable Long id, @RequestParam(name = "customerId", required = false) Long customerId) {
    try {
      if (customerId != null) {
        reviewService.deleteReviewByCustomer(id, customerId);
      } else {
        reviewService.deleteReview(id);
      }
      return ResponseEntity.ok().body("Review deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get average rating by company
  @GetMapping("/company/{companyId}/average-rating")
  public ResponseEntity<Double> getAverageRatingByCompany(@PathVariable Long companyId) {
    Double averageRating = reviewService.getAverageRatingByCompany(companyId);
    return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
  }

  // Get average rating by agent
  @GetMapping("/agent/{agentId}/average-rating")
  public ResponseEntity<Double> getAverageRatingByAgent(@PathVariable Long agentId) {
    Double averageRating = reviewService.getAverageRatingByAgent(agentId);
    return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
  }
}
