package com.customersupport.controller;

import com.customersupport.dto.CategoryDTO;
import com.customersupport.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

  @Autowired private CategoryService categoryService;

  // Get all categories (for frontend compatibility)
  @GetMapping
  public ResponseEntity<List<CategoryDTO>> getAllCategories() {
    try {
      // This would need to be implemented to get all categories or default company categories
      List<CategoryDTO> categories =
          categoryService.getActiveCategoriesByCompany(1L); // Placeholder
      return ResponseEntity.ok(categories);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // Create category
  @PostMapping
  public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
    try {
      CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
      return ResponseEntity.ok(createdCategory);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get category by ID
  @GetMapping("/{id}")
  public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
    return categoryService
        .getCategoryById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get categories by company
  @GetMapping("/company/{companyId}")
  public ResponseEntity<List<CategoryDTO>> getCategoriesByCompany(@PathVariable Long companyId) {
    List<CategoryDTO> categories = categoryService.getCategoriesByCompany(companyId);
    return ResponseEntity.ok(categories);
  }

  // Get active categories by company
  @GetMapping("/company/{companyId}/active")
  public ResponseEntity<List<CategoryDTO>> getActiveCategoriesByCompany(
      @PathVariable Long companyId) {
    List<CategoryDTO> categories = categoryService.getActiveCategoriesByCompany(companyId);
    return ResponseEntity.ok(categories);
  }

  // Get top level categories by company
  @GetMapping("/company/{companyId}/top-level")
  public ResponseEntity<List<CategoryDTO>> getTopLevelCategoriesByCompany(
      @PathVariable Long companyId) {
    List<CategoryDTO> categories = categoryService.getTopLevelCategoriesByCompany(companyId);
    return ResponseEntity.ok(categories);
  }

  // Get subcategories by parent
  @GetMapping("/parent/{parentId}/subcategories")
  public ResponseEntity<List<CategoryDTO>> getSubcategoriesByParent(@PathVariable Long parentId) {
    List<CategoryDTO> categories = categoryService.getSubcategoriesByParent(parentId);
    return ResponseEntity.ok(categories);
  }

  // Search categories by company
  @GetMapping("/company/{companyId}/search")
  public ResponseEntity<List<CategoryDTO>> searchCategoriesByCompany(
      @PathVariable Long companyId, @RequestParam String keyword) {
    List<CategoryDTO> categories = categoryService.searchCategoriesByCompany(companyId, keyword);
    return ResponseEntity.ok(categories);
  }

  // Update category
  @PutMapping("/{id}")
  public ResponseEntity<?> updateCategory(
      @PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
    try {
      CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
      return ResponseEntity.ok(updatedCategory);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Delete category
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
    try {
      categoryService.deleteCategory(id);
      return ResponseEntity.ok().body("Category deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get FAQ count in category
  @GetMapping("/{id}/faq-count")
  public ResponseEntity<Integer> getFaqCountInCategory(@PathVariable Long id) {
    Integer count = categoryService.getFaqCountInCategory(id);
    return ResponseEntity.ok(count);
  }
}
