package com.customersupport.repository;

import com.customersupport.entity.Review;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  Optional<Review> findByTicketId(Long ticketId);

  List<Review> findByCustomerId(Long customerId);

  List<Review> findByAgentId(Long agentId);

  List<Review> findByCompanyId(Long companyId);

  @Query("SELECT r FROM Review r WHERE r.company.id = :companyId ORDER BY r.createdAt DESC")
  List<Review> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") Long companyId);

  @Query("SELECT r FROM Review r WHERE r.agent.id = :agentId ORDER BY r.createdAt DESC")
  List<Review> findByAgentIdOrderByCreatedAtDesc(@Param("agentId") Long agentId);

  @Query("SELECT r FROM Review r WHERE r.company.id = :companyId AND r.rating >= :minRating")
  List<Review> findByCompanyIdAndMinimumRating(
      @Param("companyId") Long companyId, @Param("minRating") Integer minRating);

  @Query(
      "SELECT r FROM Review r WHERE r.company.id = :companyId "
          + "AND (:agentId IS NULL OR (r.agent IS NOT NULL AND r.agent.id = :agentId)) "
          + "AND (:startDate IS NULL OR r.createdAt >= :startDate) "
          + "AND (:endDate IS NULL OR r.createdAt <= :endDate) "
          + "ORDER BY r.createdAt DESC")
  List<Review> findByCompanyWithFilters(
      @Param("companyId") Long companyId,
      @Param("agentId") Long agentId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.company.id = :companyId")
  Double getAverageRatingByCompany(@Param("companyId") Long companyId);

  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.agent.id = :agentId")
  Double getAverageRatingByAgent(@Param("agentId") Long agentId);

  @Query(
      "SELECT COUNT(r) FROM Review r WHERE r.company.id = :companyId AND r.createdAt BETWEEN"
          + " :startDate AND :endDate")
  Integer countByCompanyIdAndDateRange(
      @Param("companyId") Long companyId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.company.id = :companyId AND r.rating = :rating")
  Integer countByCompanyIdAndRating(
      @Param("companyId") Long companyId, @Param("rating") Integer rating);

  @Query("SELECT r FROM Review r WHERE r.company.id = :companyId AND r.isFeatured = true")
  List<Review> findFeaturedReviewsByCompany(@Param("companyId") Long companyId);

  @Query(
      "SELECT r FROM Review r WHERE r.company.id = :companyId AND r.isPublished = true ORDER BY"
          + " r.createdAt DESC")
  List<Review> findPublishedReviewsByCompany(@Param("companyId") Long companyId);
}
