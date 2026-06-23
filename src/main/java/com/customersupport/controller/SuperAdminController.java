package com.customersupport.controller;

import com.customersupport.dto.CompanyAdminRegistrationDTO;
import com.customersupport.dto.CompanyDTO;
import com.customersupport.dto.CompanyRegistrationDTO;
import com.customersupport.entity.Company;
import com.customersupport.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
@CrossOrigin(origins = "*")
public class SuperAdminController {

  @Autowired private CompanyService companyService;

  @GetMapping("/companies")
  public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
    List<CompanyDTO> companies = companyService.getAllCompanies();
    return ResponseEntity.ok(companies);
  }

  @GetMapping("/companies/{id}")
  public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long id) {
    return companyService
        .getCompanyById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/companies/status/{status}")
  public ResponseEntity<List<CompanyDTO>> getCompaniesByStatus(
      @PathVariable Company.CompanyStatus status) {
    List<CompanyDTO> companies = companyService.getCompaniesByStatus(status);
    return ResponseEntity.ok(companies);
  }

  @GetMapping("/companies/search")
  public ResponseEntity<List<CompanyDTO>> searchCompanies(@RequestParam String keyword) {
    List<CompanyDTO> companies = companyService.searchCompanies(keyword);
    return ResponseEntity.ok(companies);
  }

  @PostMapping("/companies")
  public ResponseEntity<?> createCompany(
      @Valid @RequestBody CompanyRegistrationDTO registrationDTO) {
    try {
      CompanyDTO company = companyService.registerCompany(registrationDTO);
      return ResponseEntity.ok(company);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/companies/register")
  public ResponseEntity<?> registerCompany(
      @Valid @RequestBody CompanyRegistrationDTO registrationDTO) {
    try {
      CompanyDTO company = companyService.registerCompany(registrationDTO);
      return ResponseEntity.ok(company);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/companies/{id}")
  public ResponseEntity<?> updateCompany(
      @PathVariable Long id, @Valid @RequestBody CompanyDTO companyDTO) {
    try {
      CompanyDTO updatedCompany = companyService.updateCompany(id, companyDTO);
      return ResponseEntity.ok(updatedCompany);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/companies/{id}/deactivate")
  public ResponseEntity<?> deactivateCompany(@PathVariable Long id) {
    try {
      companyService.deactivateCompany(id);
      return ResponseEntity.ok().body("Company deactivated successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/companies/{id}/activate")
  public ResponseEntity<?> activateCompany(@PathVariable Long id) {
    try {
      companyService.activateCompany(id);
      return ResponseEntity.ok().body("Company activated successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/companies/{id}")
  public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
    try {
      companyService.deleteCompany(id);
      return ResponseEntity.ok().body("Company deleted successfully");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/company-admin")
  public ResponseEntity<?> createCompanyAdmin(
      @Valid @RequestBody CompanyAdminRegistrationDTO adminDTO) {
    try {
      // Create company admin user
      companyService.createCompanyAdmin(adminDTO);
      return ResponseEntity.ok(new SuccessResponse("Company admin created successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
  }

  // Helper classes for JSON responses
  public static class SuccessResponse {
    private String message;

    public SuccessResponse(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  public static class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
      this.error = error;
    }

    public String getError() {
      return error;
    }
  }
}
