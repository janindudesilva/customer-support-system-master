package com.customersupport.controller;

import com.customersupport.dto.LoginDTO;
import com.customersupport.dto.PasswordResetRequestDTO;
import com.customersupport.dto.PasswordUpdateDTO;
import com.customersupport.service.AuthService;
import com.customersupport.service.SimpleAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  @Autowired private AuthService authService;

  @Autowired private SimpleAuthService simpleAuthService;

  @PostMapping
  public ResponseEntity<?> login(
      @Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
    try {
      System.out.println(
          "AuthController: Login request received for email: " + loginDTO.getEmail());
      System.out.println("AuthController: Request URI: " + request.getRequestURI());
      System.out.println("AuthController: Request Method: " + request.getMethod());

      Map<String, Object> response =
          authService.login(loginDTO, request.getRemoteAddr(), request.getHeader("User-Agent"));
      System.out.println("AuthController: Login successful, response prepared: " + response);

      ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(response);
      System.out.println(
          "AuthController: Returning ResponseEntity with status: "
              + responseEntity.getStatusCode());

      return responseEntity;
    } catch (RuntimeException e) {
      System.err.println("AuthController: Login failed with error: " + e.getMessage());
      e.printStackTrace();
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
      System.err.println("AuthController: Unexpected error during login: " + e.getMessage());
      e.printStackTrace();
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", "An unexpected error occurred");
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @PostMapping("/simple")
  public ResponseEntity<?> simpleLogin(@Valid @RequestBody LoginDTO loginDTO) {
    try {
      Map<String, Object> response = simpleAuthService.simpleLogin(loginDTO);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/test")
  public ResponseEntity<?> test() {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Test endpoint working");
    response.put("timestamp", System.currentTimeMillis());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestParam String token) {
    try {
      authService.logout(token);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Logged out successfully");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/password-reset-request")
  public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
    try {
      authService.requestPasswordReset(email);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Password reset email sent successfully");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/password-reset")
  public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequestDTO resetRequest) {
    try {
      authService.resetPassword(resetRequest.getToken(), resetRequest.getNewPassword());
      Map<String, String> response = new HashMap<>();
      response.put("message", "Password reset successfully");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/password-update")
  public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdateDTO updateRequest) {
    try {
      authService.updatePassword(
          updateRequest.getUserId(),
          updateRequest.getCurrentPassword(),
          updateRequest.getNewPassword());
      Map<String, String> response = new HashMap<>();
      response.put("message", "Password updated successfully");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/verify-token")
  public ResponseEntity<?> verifyToken(@RequestParam String token) {
    try {
      Map<String, Object> userData = authService.verifyToken(token);
      return ResponseEntity.ok(userData);
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/companies")
  public ResponseEntity<?> getActiveCompanies() {
    try {
      return ResponseEntity.ok(authService.getActiveCompanies());
    } catch (RuntimeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}
