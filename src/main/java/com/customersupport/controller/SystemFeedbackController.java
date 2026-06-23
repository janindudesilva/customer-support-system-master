package com.customersupport.controller;

import com.customersupport.dto.SystemFeedbackDTO;
import com.customersupport.entity.SystemFeedback;
import com.customersupport.service.SystemFeedbackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system-feedback")
@CrossOrigin(origins = "*")
public class SystemFeedbackController {

  @Autowired private SystemFeedbackService systemFeedbackService;

  @PostMapping
  public ResponseEntity<?> submitFeedback(@Valid @RequestBody SystemFeedbackDTO feedbackDTO) {
    try {
      SystemFeedbackDTO created = systemFeedbackService.submitFeedback(feedbackDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(errorResponse(ex.getMessage()));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse(ex.getMessage()));
    }
  }

  @GetMapping("/company/{companyId}")
  public ResponseEntity<?> getFeedbackForCompany(
      @PathVariable Long companyId, @RequestParam(required = false) SystemFeedback.Status status) {
    try {
      List<SystemFeedbackDTO> feedback =
          systemFeedbackService.getFeedbackForCompany(companyId, status);
      return ResponseEntity.ok(feedback);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(errorResponse(ex.getMessage()));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse(ex.getMessage()));
    }
  }

  @GetMapping("/pending")
  public ResponseEntity<List<SystemFeedbackDTO>> getPendingFeedback() {
    List<SystemFeedbackDTO> feedback = systemFeedbackService.getPendingFeedback();
    return ResponseEntity.ok(feedback);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<?> getFeedbackByStatus(@PathVariable SystemFeedback.Status status) {
    try {
      List<SystemFeedbackDTO> feedback = systemFeedbackService.getFeedbackByStatus(status);
      return ResponseEntity.ok(feedback);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(errorResponse(ex.getMessage()));
    }
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<?> updateFeedbackStatus(
      @PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
    try {
      SystemFeedbackDTO updated =
          systemFeedbackService.updateFeedbackStatus(id, request.getStatus());
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(errorResponse(ex.getMessage()));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse(ex.getMessage()));
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateFeedback(
      @PathVariable Long id, @Valid @RequestBody SystemFeedbackDTO feedbackDTO) {
    try {
      SystemFeedbackDTO updated = systemFeedbackService.updateFeedback(id, feedbackDTO);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(errorResponse(ex.getMessage()));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse(ex.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
    try {
      systemFeedbackService.deleteFeedback(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(errorResponse(ex.getMessage()));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(errorResponse(ex.getMessage()));
    }
  }

  @GetMapping("/public")
  public ResponseEntity<List<SystemFeedbackDTO>> getPublicFeedback(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) Long companyId) {
    List<SystemFeedbackDTO> feedback = systemFeedbackService.getApprovedFeedback(limit, companyId);
    return ResponseEntity.ok(feedback);
  }

  private Map<String, String> errorResponse(String message) {
    Map<String, String> error = new HashMap<>();
    error.put("error", message);
    return error;
  }

  public static class StatusUpdateRequest {
    @NotNull(message = "Status is required")
    private SystemFeedback.Status status;

    public SystemFeedback.Status getStatus() {
      return status;
    }

    public void setStatus(SystemFeedback.Status status) {
      this.status = status;
    }
  }
}
