package com.customersupport.repository;

import com.customersupport.entity.Faq;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

  List<Faq> findByCompanyId(Long companyId);

  List<Faq> findByCompanyIdAndIsPublished(Long companyId, Boolean isPublished);

  List<Faq> findByCompanyIdAndIsFeatured(Long companyId, Boolean isFeatured);

  List<Faq> findByCategoryId(Long categoryId);

  List<Faq> findByCompanyIdAndCategoryId(Long companyId, Long categoryId);

  @Query(
      "SELECT f FROM Faq f WHERE f.company.id = :companyId AND "
          + "(f.question LIKE %:keyword% OR f.answer LIKE %:keyword%)")
  List<Faq> searchFaqsByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query(
      "SELECT f FROM Faq f WHERE f.company.id = :company AND f.isPublished = true AND (f.question"
          + " LIKE %:keyword% OR f.answer LIKE %:keyword% OR f.keywords LIKE %:keyword%)")
  List<Faq> searchPublishedFaqsByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query(
      "SELECT f FROM Faq f WHERE f.company.id = :companyId ORDER BY f.viewCount DESC LIMIT :limit")
  List<Faq> findMostViewedFaqsByCompany(
      @Param("companyId") Long companyId, @Param("limit") int limit);

  @Query(
      "SELECT f FROM Faq f WHERE f.company.id = :companyId ORDER BY f.helpfulCount DESC LIMIT"
          + " :limit")
  List<Faq> findMostHelpfulFaqsByCompany(
      @Param("companyId") Long companyId, @Param("limit") int limit);

  @Query(
      "SELECT f FROM Faq f WHERE f.company.id = :companyId ORDER BY f.createdAt DESC LIMIT :limit")
  List<Faq> findRecentFaqsByCompany(@Param("companyId") Long companyId, @Param("limit") int limit);

  @Query(
      "SELECT f FROM Faq f WHERE f.company.id = :companyId ORDER BY f.sortOrder ASC, f.createdAt"
          + " DESC")
  List<Faq> findByCompanyIdOrderBySortOrderAscCreatedAtDesc(@Param("companyId") Long companyId);
}
