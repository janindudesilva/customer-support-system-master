package com.customersupport.controller;

import com.customersupport.entity.User;
import com.customersupport.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {

  @Autowired private UserService userService;

  /**
   * Test endpoint for sending password reset email This endpoint should be removed in production
   */
  @PostMapping("/reset-password")
  public ResponseEntity<?> testPasswordReset(@RequestParam String email) {
    try {
      User user =
          userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
      String newPassword = userService.resetPassword(user.getId());
      return ResponseEntity.ok("Password reset email sent with new password: " + newPassword);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
  }
}
