package com.customersupport.controller;

import com.customersupport.dto.DashboardDTO;
import com.customersupport.service.DashboardService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

  @Autowired private DashboardService dashboardService;

  @GetMapping("/super-admin")
  public ResponseEntity<?> getSuperAdminDashboard() {
    try {
      DashboardDTO dashboard = dashboardService.getSuperAdminDashboard();
      return ResponseEntity.ok(dashboard);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/company-admin/{companyId}")
  public ResponseEntity<?> getCompanyAdminDashboard(@PathVariable Long companyId) {
    try {
      DashboardDTO dashboard = dashboardService.getCompanyAdminDashboard(companyId);
      return ResponseEntity.ok(dashboard);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/agent/{agentId}")
  public ResponseEntity<?> getAgentDashboard(@PathVariable Long agentId) {
    try {
      DashboardDTO dashboard = dashboardService.getAgentDashboard(agentId);
      return ResponseEntity.ok(dashboard);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<?> getCustomerDashboard(@PathVariable Long customerId) {
    try {
      DashboardDTO dashboard = dashboardService.getCustomerDashboard(customerId);
      return ResponseEntity.ok(dashboard);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/recent-activities/{userId}")
  public ResponseEntity<?> getRecentActivities(@PathVariable Long userId) {
    try {
      return ResponseEntity.ok(dashboardService.getRecentActivities(userId));
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}
