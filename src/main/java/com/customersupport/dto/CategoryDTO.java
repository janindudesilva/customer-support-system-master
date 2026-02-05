package com.customersupport.dto;

import com.customersupport.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

  private Long id;

  @NotNull(message = "Company ID is required")
  private Long companyId;

  private String companyName;

  @NotBlank(message = "Category name is required")
  private String name;

  private String description;

  private Long parentId;

  private String parentName;

  private List<CategoryDTO> subcategories;

  private String colorCode = "#007bff";

  private Boolean isActive = true;

  private Integer sortOrder = 0;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private Long createdBy;

  private Integer faqCount;

  public static CategoryDTO fromEntity(Category category) {
    return fromEntity(category, false);
  }

  public static CategoryDTO fromEntity(Category category, boolean includeSubcategories) {
    CategoryDTO dto = new CategoryDTO();
    dto.setId(category.getId());
    dto.setCompanyId(category.getCompany().getId());
    dto.setCompanyName(category.getCompany().getName());
    dto.setName(category.getName());
    dto.setDescription(category.getDescription());

    if (category.getParent() != null) {
      dto.setParentId(category.getParent().getId());
      dto.setParentName(category.getParent().getName());
    }

    if (includeSubcategories
        && category.getSubcategories() != null
        && !category.getSubcategories().isEmpty()) {
      dto.setSubcategories(
          category.getSubcategories().stream()
              .map(subcat -> CategoryDTO.fromEntity(subcat, false))
              .collect(Collectors.toList()));
    } else {
      dto.setSubcategories(new ArrayList<>());
    }

    dto.setColorCode(category.getColorCode());
    dto.setIsActive(category.getIsActive());
    dto.setSortOrder(category.getSortOrder());
    dto.setCreatedAt(category.getCreatedAt());
    dto.setUpdatedAt(category.getUpdatedAt());
    dto.setCreatedBy(category.getCreatedBy());

    // If tickets collection is initialized
    if (category.getTickets() != null) {
      dto.setFaqCount(category.getTickets().size());
    }

    return dto;
  }
}
