package com.customersupport.service;

import com.customersupport.dto.FaqDTO;
import com.customersupport.entity.Category;
import com.customersupport.entity.Company;
import com.customersupport.entity.Faq;
import com.customersupport.entity.User;
import com.customersupport.repository.CategoryRepository;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.FaqRepository;
import com.customersupport.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FaqService {

  @Autowired private FaqRepository faqRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private UserRepository userRepository;

  // Create FAQ
  public FaqDTO createFaq(FaqDTO faqDTO, Long createdById) {
    Company company =
        companyRepository
            .findById(faqDTO.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company not found"));

    User createdBy =
        userRepository
            .findById(createdById)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Category category = null;
    if (faqDTO.getCategoryId() != null) {
      category =
          categoryRepository
              .findById(faqDTO.getCategoryId())
              .orElseThrow(() -> new RuntimeException("Category not found"));

      // Verify category belongs to company
      if (!category.getCompany().getId().equals(company.getId())) {
        throw new RuntimeException("Category does not belong to the specified company");
      }
    }

    Faq faq = new Faq();
    faq.setCompany(company);
    faq.setCategory(category);
    faq.setQuestion(faqDTO.getQuestion());
    faq.setAnswer(faqDTO.getAnswer());
    faq.setKeywords(faqDTO.getKeywords());
    faq.setIsFeatured(faqDTO.getIsFeatured());
    faq.setIsPublished(faqDTO.getIsPublished());
    faq.setSortOrder(faqDTO.getSortOrder());
    faq.setCreatedBy(createdBy);
    faq.setUpdatedBy(createdBy);

    Faq savedFaq = faqRepository.save(faq);
    return FaqDTO.fromEntity(savedFaq);
  }

  // Get FAQ by ID
  public Optional<FaqDTO> getFaqById(Long id) {
    return faqRepository.findById(id).map(FaqDTO::fromEntity);
  }

  // Get FAQs by company
  public List<FaqDTO> getFaqsByCompany(Long companyId) {
    return faqRepository.findByCompanyIdOrderBySortOrderAscCreatedAtDesc(companyId).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get published FAQs by company
  public List<FaqDTO> getPublishedFaqsByCompany(Long companyId) {
    return faqRepository.findByCompanyIdAndIsPublished(companyId, true).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get featured FAQs by company
  public List<FaqDTO> getFeaturedFaqsByCompany(Long companyId) {
    return faqRepository.findByCompanyIdAndIsFeatured(companyId, true).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get FAQs by category
  public List<FaqDTO> getFaqsByCategory(Long categoryId) {
    return faqRepository.findByCategoryId(categoryId).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Search FAQs by company
  public List<FaqDTO> searchFaqsByCompany(Long companyId, String keyword) {
    return faqRepository.searchFaqsByCompany(companyId, keyword).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Search published FAQs by company
  public List<FaqDTO> searchPublishedFaqsByCompany(Long companyId, String keyword) {
    return faqRepository.searchPublishedFaqsByCompany(companyId, keyword).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get most viewed FAQs
  public List<FaqDTO> getMostViewedFaqsByCompany(Long companyId, int limit) {
    return faqRepository.findMostViewedFaqsByCompany(companyId, limit).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get most helpful FAQs
  public List<FaqDTO> getMostHelpfulFaqsByCompany(Long companyId, int limit) {
    return faqRepository.findMostHelpfulFaqsByCompany(companyId, limit).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Get recent FAQs
  public List<FaqDTO> getRecentFaqsByCompany(Long companyId, int limit) {
    return faqRepository.findRecentFaqsByCompany(companyId, limit).stream()
        .map(FaqDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // Update FAQ
  public FaqDTO updateFaq(Long id, FaqDTO faqDTO, Long updatedById) {
    Faq faq = faqRepository.findById(id).orElseThrow(() -> new RuntimeException("FAQ not found"));

    User updatedBy =
        userRepository
            .findById(updatedById)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Update category if provided
    if (faqDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(faqDTO.getCategoryId())
              .orElseThrow(() -> new RuntimeException("Category not found"));

      // Verify category belongs to company
      if (!category.getCompany().getId().equals(faq.getCompany().getId())) {
        throw new RuntimeException("Category does not belong to the specified company");
      }

      faq.setCategory(category);
    } else {
      faq.setCategory(null);
    }

    // Update fields
    faq.setQuestion(faqDTO.getQuestion());
    faq.setAnswer(faqDTO.getAnswer());
    faq.setKeywords(faqDTO.getKeywords());
    faq.setIsFeatured(faqDTO.getIsFeatured());
    faq.setIsPublished(faqDTO.getIsPublished());
    faq.setSortOrder(faqDTO.getSortOrder());
    faq.setUpdatedBy(updatedBy);

    Faq updatedFaq = faqRepository.save(faq);
    return FaqDTO.fromEntity(updatedFaq);
  }

  // Delete FAQ
  public void deleteFaq(Long id) {
    Faq faq = faqRepository.findById(id).orElseThrow(() -> new RuntimeException("FAQ not found"));

    faqRepository.delete(faq);
  }

  // Increment view count
  public void incrementViewCount(Long id) {
    Faq faq = faqRepository.findById(id).orElseThrow(() -> new RuntimeException("FAQ not found"));

    faq.incrementViewCount();
    faqRepository.save(faq);
  }

  // Rate FAQ as helpful
  public void rateFaqAsHelpful(Long id) {
    Faq faq = faqRepository.findById(id).orElseThrow(() -> new RuntimeException("FAQ not found"));

    faq.setHelpfulCount(faq.getHelpfulCount() + 1);
    faqRepository.save(faq);
  }

  // Rate FAQ as not helpful
  public void rateFaqAsNotHelpful(Long id) {
    Faq faq = faqRepository.findById(id).orElseThrow(() -> new RuntimeException("FAQ not found"));

    faq.setNotHelpfulCount(faq.getNotHelpfulCount() + 1);
    faqRepository.save(faq);
  }
}
