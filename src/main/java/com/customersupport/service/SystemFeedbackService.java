package com.customersupport.service;

import com.customersupport.dto.SystemFeedbackDTO;
import com.customersupport.entity.Company;
import com.customersupport.entity.SystemFeedback;
import com.customersupport.entity.User;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.SystemFeedbackRepository;
import com.customersupport.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SystemFeedbackService {

  private static final int DEFAULT_PUBLIC_LIMIT = 6;
  private static final int MAX_PUBLIC_LIMIT = 20;

  @Autowired private SystemFeedbackRepository systemFeedbackRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  public SystemFeedbackDTO submitFeedback(SystemFeedbackDTO dto) {
    if (dto == null) {
      throw new IllegalArgumentException("Feedback payload is required");
    }

    validateRating(dto.getRating());

    String title = dto.getTitle() != null ? dto.getTitle().trim() : null;
    String message = dto.getFeedback() != null ? dto.getFeedback().trim() : null;

    if (title == null || title.isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }

    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Feedback is required");
    }

    Company company =
        companyRepository
            .findById(dto.getCompanyId())
            .orElseThrow(() -> new IllegalArgumentException("Company not found"));

    User admin =
        userRepository
            .findById(dto.getAdminId())
            .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

    if (admin.getRole() == null || admin.getRole().getName() == null) {
      throw new IllegalArgumentException("Admin role information is missing");
    }

    if (!"COMPANY_ADMIN".equalsIgnoreCase(admin.getRole().getName())) {
      throw new IllegalArgumentException("Only company administrators can submit system feedback");
    }

    if (admin.getCompany() == null || !admin.getCompany().getId().equals(company.getId())) {
      throw new IllegalArgumentException("Admin does not belong to the specified company");
    }

    SystemFeedback feedback = new SystemFeedback();
    feedback.setCompany(company);
    feedback.setAdmin(admin);
    feedback.setTitle(title);
    feedback.setFeedback(message);
    feedback.setRating(dto.getRating());
    feedback.setStatus(SystemFeedback.Status.PENDING);
    feedback.setCreatedAt(LocalDateTime.now());
    feedback.setUpdatedAt(LocalDateTime.now());

    SystemFeedback saved = systemFeedbackRepository.save(feedback);
    return SystemFeedbackDTO.fromEntity(saved);
  }

  public List<SystemFeedbackDTO> getFeedbackForCompany(
      Long companyId, SystemFeedback.Status status) {
    if (companyId == null) {
      throw new IllegalArgumentException("Company ID is required");
    }

    List<SystemFeedback> feedbackList;
    if (status != null) {
      feedbackList =
          systemFeedbackRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
    } else {
      feedbackList = systemFeedbackRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    return feedbackList.stream().map(SystemFeedbackDTO::fromEntity).collect(Collectors.toList());
  }

  public List<SystemFeedbackDTO> getFeedbackByStatus(SystemFeedback.Status status) {
    if (status == null) {
      throw new IllegalArgumentException("Status is required");
    }

    return systemFeedbackRepository.findByStatusOrderByCreatedAtDesc(status).stream()
        .map(SystemFeedbackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  public List<SystemFeedbackDTO> getPendingFeedback() {
    return getFeedbackByStatus(SystemFeedback.Status.PENDING);
  }

  public SystemFeedbackDTO updateFeedbackStatus(Long feedbackId, SystemFeedback.Status status) {
    if (feedbackId == null) {
      throw new IllegalArgumentException("Feedback ID is required");
    }

    if (status == null) {
      throw new IllegalArgumentException("Status is required");
    }

    SystemFeedback feedback =
        systemFeedbackRepository
            .findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

    feedback.setStatus(status);
    feedback.setUpdatedAt(LocalDateTime.now());

    SystemFeedback saved = systemFeedbackRepository.save(feedback);
    return SystemFeedbackDTO.fromEntity(saved);
  }

  public SystemFeedbackDTO updateFeedback(Long feedbackId, SystemFeedbackDTO dto) {
    if (feedbackId == null) {
      throw new IllegalArgumentException("Feedback ID is required");
    }

    if (dto == null) {
      throw new IllegalArgumentException("Feedback payload is required");
    }

    validateRating(dto.getRating());

    String title = dto.getTitle() != null ? dto.getTitle().trim() : null;
    String message = dto.getFeedback() != null ? dto.getFeedback().trim() : null;

    if (title == null || title.isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }

    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Feedback is required");
    }

    if (dto.getCompanyId() == null) {
      throw new IllegalArgumentException("Company ID is required");
    }

    if (dto.getAdminId() == null) {
      throw new IllegalArgumentException("Admin ID is required");
    }

    SystemFeedback feedback =
        systemFeedbackRepository
            .findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

    Company company =
        companyRepository
            .findById(dto.getCompanyId())
            .orElseThrow(() -> new IllegalArgumentException("Company not found"));

    User admin =
        userRepository
            .findById(dto.getAdminId())
            .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

    String roleName = admin.getRole() != null ? admin.getRole().getName() : null;
    if (!"COMPANY_ADMIN".equalsIgnoreCase(roleName)) {
      throw new IllegalArgumentException("Only company administrators can update system feedback");
    }

    if (admin.getCompany() == null || !admin.getCompany().getId().equals(company.getId())) {
      throw new IllegalArgumentException("Admin does not belong to the specified company");
    }

    if (!feedback.getAdmin().getId().equals(admin.getId())) {
      throw new IllegalArgumentException("You can only edit feedback you originally submitted");
    }

    if (!feedback.getCompany().getId().equals(company.getId())) {
      throw new IllegalArgumentException("Feedback is not associated with the specified company");
    }

    feedback.setTitle(title);
    feedback.setFeedback(message);
    feedback.setRating(dto.getRating());
    feedback.setStatus(SystemFeedback.Status.PENDING);
    feedback.setUpdatedAt(LocalDateTime.now());

    SystemFeedback saved = systemFeedbackRepository.save(feedback);
    return SystemFeedbackDTO.fromEntity(saved);
  }

  public void deleteFeedback(Long feedbackId) {
    if (feedbackId == null) {
      throw new IllegalArgumentException("Feedback ID is required");
    }

    SystemFeedback feedback =
        systemFeedbackRepository
            .findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

    systemFeedbackRepository.delete(feedback);
  }

  public List<SystemFeedbackDTO> getApprovedFeedback(Integer limit, Long companyId) {
    int pageSize =
        limit != null && limit > 0 ? Math.min(limit, MAX_PUBLIC_LIMIT) : DEFAULT_PUBLIC_LIMIT;

    if (companyId != null) {
      return systemFeedbackRepository
          .findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, SystemFeedback.Status.APPROVED)
          .stream()
          .limit(pageSize)
          .map(SystemFeedbackDTO::fromEntity)
          .collect(Collectors.toList());
    }

    Page<SystemFeedback> page =
        systemFeedbackRepository.findByStatus(
            SystemFeedback.Status.APPROVED,
            PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

    return page.getContent().stream()
        .map(SystemFeedbackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  private void validateRating(Integer rating) {
    if (rating == null) {
      throw new IllegalArgumentException("Rating is required");
    }

    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }
  }
}
