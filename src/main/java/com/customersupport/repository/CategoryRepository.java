package com.customersupport.repository;

import com.customersupport.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByCompanyId(Long companyId);

  List<Category> findByCompanyIdAndIsActive(Long companyId, Boolean isAvailable);

  List<Category> findByParentId(Long parentId);

  List<Category> findByCompanyIdAndParentIdIsNull(Long companyId);

  @Query(
      "SELECT c FROM Category c WHERE c.company.id = :companyId ORDER BY c.sortOrder ASC, c.name"
          + " ASC")
  List<Category> findByCompanyIdOrderBySortOrderAscNameAsc(@Param("companyId") Long companyId);

  @Query(
      "SELECT c FROM Category c WHERE c.company.id = :companyId AND c.parent.id IS NULL ORDER BY"
          + " c.sortOrder ASC, c.name ASC ")
  List<Category> findTopLevelCategoriesByCompanyId(@Param("companyId") Long companyId);

  @Query("SELECT c FROM Category c WHERE c.company.id = :companyId ")
  List<Category> searchCategoriesByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query("SELECT COUNT(f) FROM Faq f WHERE f.category.id = :categoryId")
  Integer countFaqsByCategory(@Param("categoryId") Long categoryId);
}
