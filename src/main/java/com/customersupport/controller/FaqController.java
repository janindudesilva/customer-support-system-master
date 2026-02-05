package com.customersupport.controller;

import com.customersupport.dto.FaqDTO;
import com.customersupport.service.FaqService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faqs")
@CrossOrigin(origins = "*")
public class FaqController {

  @Autowired private FaqService faqService;

  // Create FAQ
  @PostMapping
  public ResponseEntity<?> createFaq(
      @Valid @RequestBody FaqDTO faqDTO, @RequestParam Long createdById) {
    try {
      FaqDTO createdFaq = faqService.createFaq(faqDTO, createdById);
      return ResponseEntity.ok(createdFaq);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Get FAQ by ID
  @GetMapping("/{id}")
  public ResponseEntity<FaqDTO> getFaqById(@PathVariable Long id) {
    return faqService
        .getFaqById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Get FAQs by company
  @GetMapping("/company/{companyId}")
  public ResponseEntity<List<FaqDTO>> getFaqsByCompany(@PathVariable Long companyId) {
    List<FaqDTO> faqs = faqService.getFaqsByCompany(companyId);
    return ResponseEntity.ok(faqs);
  }

  // Get published FAQs by company
  @GetMapping("/company/{companyId}/published")
  public ResponseEntity<List<FaqDTO>> getPublishedFaqsByCompany(@PathVariable Long companyId) {
    List<FaqDTO> faqs = faqService.getPublishedFaqsByCompany(companyId);
    return ResponseEntity.ok(faqs);
  }

  // Get featured FAQs by company
  @GetMapping("/company/{companyId}/featured")
  public ResponseEntity<List<FaqDTO>> getFeaturedFaqsByCompany(@PathVariable Long companyId) {
    List<FaqDTO> faqs = faqService.getFeaturedFaqsByCompany(companyId);
    return ResponseEntity.ok(faqs);
  }

  // Get FAQs by category
  @GetMapping("/category/{categoryId}")
  public ResponseEntity<List<FaqDTO>> getFaqsByCategory(@PathVariable Long categoryId) {
    List<FaqDTO> faqs = faqService.getFaqsByCategory(categoryId);
    return ResponseEntity.ok(faqs);
  }

  // Search FAQs by company
  @GetMapping("/company/{companyId}/search")
  public ResponseEntity<List<FaqDTO>> searchFaqsByCompany(
      @PathVariable Long companyId, @RequestParam String keyword) {
    List<FaqDTO> faqs = faqService.searchFaqsByCompany(companyId, keyword);
    return ResponseEntity.ok(faqs);
  }

  // Search published FAQs by company
  @GetMapping("/company/{companyId}/published/search")
  public ResponseEntity<List<FaqDTO>> searchPublishedFaqsByCompany(
      @PathVariable Long companyId, @RequestParam String keyword) {
    List<FaqDTO> faqs = faqService.searchPublishedFaqsByCompany(companyId, keyword);
    return ResponseEntity.ok(faqs);
  }

  // Get most viewed FAQs
  @GetMapping("/company/{companyId}/most-viewed")
  public ResponseEntity<List<FaqDTO>> getMostViewedFaqsByCompany(
      @PathVariable Long companyId, @RequestParam(defaultValue = "5") int limit) {
    List<FaqDTO> faqs = faqService.getMostViewedFaqsByCompany(companyId, limit);
    return ResponseEntity.ok(faqs);
  }

  // Get most helpful FAQs
  @GetMapping("/company/{companyId}/most-helpful")
  public ResponseEntity<List<FaqDTO>> getMostHelpfulFaqsByCompany(
      @PathVariable Long companyId, @RequestParam(defaultValue = "5") int limit) {
    List<FaqDTO> faqs = faqService.getMostHelpfulFaqsByCompany(companyId, limit);
    return ResponseEntity.ok(faqs);
  }

  // Get recent FAQs
  @GetMapping("/company/{companyId}/recent")
  public ResponseEntity<List<FaqDTO>> getRecentFaqsByCompany(
      @PathVariable Long companyId, @RequestParam(defaultValue = "5") int limit) {
    List<FaqDTO> faqs = faqService.getRecentFaqsByCompany(companyId, limit);
    return ResponseEntity.ok(faqs);
  }

  // Update FAQ
  @PutMapping("/{id}")
  public ResponseEntity<?> updateFaq(
      @PathVariable Long id, @Valid @RequestBody FaqDTO faqDTO, @RequestParam Long updatedById) {
    try {
      FaqDTO updatedFaq = faqService.updateFaq(id, faqDTO, updatedById);
      return ResponseEntity.ok(updatedFaq);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Delete FAQ
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteFaq(@PathVariable Long id) {
    try {
      faqService.deleteFaq(id);
      return ResponseEntity.ok().body("FAQ deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Increment view count
  @PostMapping("/{id}/view")
  public ResponseEntity<?> incrementViewCount(@PathVariable Long id) {
    try {
      faqService.incrementViewCount(id);
      return ResponseEntity.ok().body("View count incremented successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Rate FAQ as helpful
  @PostMapping("/{id}/helpful")
  public ResponseEntity<?> rateFaqAsHelpful(@PathVariable Long id) {
    try {
      faqService.rateFaqAsHelpful(id);
      return ResponseEntity.ok().body("FAQ rated as helpful");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // Rate FAQ as not helpful
  @PostMapping("/{id}/not-helpful")
  public ResponseEntity<?> rateFaqAsNotHelpful(@PathVariable Long id) {
    try {
      faqService.rateFaqAsNotHelpful(id);
      return ResponseEntity.ok().body("FAQ rated as not helpful");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
