package com.customersupport.service;

import com.customersupport.dto.CategoryDTO;
import com.customersupport.entity.Category;
import com.customersupport.entity.Company;
import com.customersupport.repository.CategoryRepository;
import com.customersupport.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CategoryService {

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private CompanyRepository companyRepository;

  // Create category
  public CategoryDTO createCategory(CategoryDTO categoryDTO) {
    Company company =
        companyRepository
            .findById(categoryDTO.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company not found"));

    Category parent = null;
    if (categoryDTO.getParentId() != null) {
      parent =
          categoryRepository
              .findById(categoryDTO.getParentId())
              .orElseThrow(() -> new RuntimeException("Parent category not found"));

      // Verify parent belongs to company
      if (!parent.getCompany().getId().equals(company.getId())) {
        throw new RuntimeException("Parent category does not belong to the specified company");
      }
    }

    Category category = new Category();
    category.setCompany(company);
    category.setName(categoryDTO.getName());
    category.setDescription(categoryDTO.getDescription());
    category.setParent(parent);
    category.setColorCode(categoryDTO.getColorCode());
    category.setIsActive(categoryDTO.getIsActive());
    category.setSortOrder(categoryDTO.getSortOrder());
    category.setCreatedBy(categoryDTO.getCreatedBy());

    Category savedCategory = categoryRepository.save(category);
    return CategoryDTO.fromEntity(savedCategory);
  }

  // Get category by ID
  public Optional<CategoryDTO> getCategoryById(Long id) {
    return categoryRepository.findById(id).map(category -> CategoryDTO.fromEntity(category, true));
  }

  // Get categories by company
  public List<CategoryDTO> getCategoriesByCompany(Long companyId) {
    return categoryRepository.findByCompanyIdOrderBySortOrderAscNameAsc(companyId).stream()
        .map(category -> CategoryDTO.fromEntity(category, false))
        .collect(Collectors.toList());
  }

  // Get active categories by company
  public List<CategoryDTO> getActiveCategoriesByCompany(Long companyId) {
    return categoryRepository.findByCompanyIdAndIsActive(companyId, true).stream()
        .map(category -> CategoryDTO.fromEntity(category, false))
        .collect(Collectors.toList());
  }

  // Get top level categories by company
  public List<CategoryDTO> getTopLevelCategoriesByCompany(Long companyId) {
    return categoryRepository.findTopLevelCategoriesByCompanyId(companyId).stream()
        .map(category -> CategoryDTO.fromEntity(category, true))
        .collect(Collectors.toList());
  }

  // Get subcategories by parent
  public List<CategoryDTO> getSubcategoriesByParent(Long parentId) {
    return categoryRepository.findByParentId(parentId).stream()
        .map(category -> CategoryDTO.fromEntity(category, false))
        .collect(Collectors.toList());
  }

  // Search categories by company
  public List<CategoryDTO> searchCategoriesByCompany(Long companyId, String keyword) {
    return categoryRepository.searchCategoriesByCompany(companyId, keyword).stream()
        .map(category -> CategoryDTO.fromEntity(category, false))
        .collect(Collectors.toList());
  }

  // Update category
  public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    // Update parent if provided
    if (categoryDTO.getParentId() != null) {
      // Verify parent is not the category itself
      if (categoryDTO.getParentId().equals(id)) {
        throw new RuntimeException("A category cannot be its own parent");
      }

      Category parent =
          categoryRepository
              .findById(categoryDTO.getParentId())
              .orElseThrow(() -> new RuntimeException("Parent category not found"));

      // Verify parent belongs to company
      if (!parent.getCompany().getId().equals(category.getCompany().getId())) {
        throw new RuntimeException("Parent category does not belong to the specified company");
      }

      category.setParent(parent);
    } else {
      category.setParent(null);
    }

    // Update fields
    category.setName(categoryDTO.getName());
    category.setDescription(categoryDTO.getDescription());
    category.setColorCode(categoryDTO.getColorCode());
    category.setIsActive(categoryDTO.getIsActive());
    category.setSortOrder(categoryDTO.getSortOrder());

    Category updatedCategory = categoryRepository.save(category);
    return CategoryDTO.fromEntity(updatedCategory, true);
  }

  // Delete category
  public void deleteCategory(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    // Check if category has FAQs
    Integer faqCount = categoryRepository.countFaqsByCategory(id);
    if (faqCount > 0) {
      throw new RuntimeException(
          "Cannot delete category with FAQs. Please reassign or delete the FAQs first.");
    }

    // Check if category has subcategories
    List<Category> subcategories = categoryRepository.findByParentId(id);
    if (!subcategories.isEmpty()) {
      throw new RuntimeException(
          "Cannot delete category with subcategories. Please reassign or delete the subcategories"
              + " first.");
    }

    categoryRepository.delete(category);
  }

  // Get FAQ count in category
  public Integer getFaqCountInCategory(Long categoryId) {
    return categoryRepository.countFaqsByCategory(categoryId);
  }
}
