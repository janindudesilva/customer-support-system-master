package com.customersupport.service;

import com.customersupport.dto.CompanyDTO;
import com.customersupport.dto.LoginDTO;
import com.customersupport.entity.AuditLog;
import com.customersupport.entity.Company;
import com.customersupport.entity.User;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.UserRepository;
import com.customersupport.util.EmailUtil;
import com.customersupport.util.JwtUtil;
import com.customersupport.util.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  @Autowired private UserRepository userRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private EmailUtil emailUtil;

  @Autowired private AuditService auditService;

  // Login user
  @Transactional
  public Map<String, Object> login(LoginDTO loginDTO, String ipAddress, String userAgent) {
    try {
      System.out.println("Login attempt for email: " + loginDTO.getEmail());

      User user =
          userRepository
              .findByEmail(loginDTO.getEmail())
              .orElseThrow(() -> new RuntimeException("Invalid email or password"));

      System.out.println("User found: " + user.getEmail() + ", Role: " + user.getRole().getName());

      // Check if company is active
      if (user.getCompany() != null
          && user.getCompany().getStatus() != Company.CompanyStatus.ACTIVE) {
        throw new RuntimeException("Company is not active. Please contact system administrator.");
      }

      // Check if user is active
      if (user.getStatus() != User.UserStatus.ACTIVE) {
        throw new RuntimeException(
            "User account is not active. Please contact system administrator.");
      }

      // Validate password
      if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid email or password");
      }

      System.out.println("Password validation successful");

      // Reset failed login attempts
      user.setFailedLoginAttempts(0);
      user.setLastLogin(LocalDateTime.now());
      userRepository.save(user); // Explicitly save the user

      // Generate token
      String token = jwtUtil.generateToken(user);
      System.out.println("Token generated successfully");

      // Prepare response
      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("userId", user.getId());
      response.put("email", user.getEmail());
      response.put("firstName", user.getFirstname());
      response.put("lastName", user.getLastname());
      response.put("role", user.getRole().getName());
      response.put("companyId", user.getCompany() != null ? user.getCompany().getId() : null);
      response.put("companyName", user.getCompany() != null ? user.getCompany().getName() : null);

      System.out.println(
          "Login successful for user: "
              + user.getEmail()
              + " with role: "
              + user.getRole().getName());

      return response;
    } catch (Exception e) {
      System.err.println("Login error: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  // Logout user
  public void logout(String token) {
    try {
      Map<String, Object> userData = jwtUtil.validateTokenAndGetData(token);
      Long userId = Long.valueOf(userData.get("userId").toString());
      Long companyId =
          userData.get("companyId") != null
              ? Long.valueOf(userData.get("companyId").toString())
              : null;

      auditService.logEvent(
          companyId,
          userId,
          "LOGOUT",
          "User",
          userId,
          null,
          null,
          null,
          null,
          AuditLog.Severity.LOW,
          "User logged out successfully");
    } catch (Exception e) {
      System.err.println("Error during logout: " + e.getMessage());
    }
  }

  // Request to password reset
  public void requestPasswordReset(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with this email"));

    // Generate reset token
    String resetToken = RandomStringUtils.randomAlphanumeric(30);
    user.setPasswordResetToken(resetToken);
    user.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
    userRepository.save(user);

    // Send reset email
    String resetLink = "https://your-app-domain.com/reset-password?token=" + resetToken;
    String emailBody =
        "Please click the link below to reset your password:\n\n"
            + resetLink
            + "\n\nThis link will expire in 1 hour.";
    emailUtil.sendEmail(user.getEmail(), "Password Reset Request", emailBody);

    // Log event
    auditService.logEvent(
        user.getCompany() != null ? user.getCompany().getId() : null,
        user.getId(),
        "PASSWORD_RESET_REQUEST",
        "User",
        user.getId(),
        null,
        null,
        null,
        null,
        AuditLog.Severity.MEDIUM,
        "Password reset requested");
  }

  // Reset password with token
  public void resetPassword(String token, String newPassword) {
    User user =
        userRepository
            .findByPasswordResetToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));

    // check if token is expired
    if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("Token has expired. Please request a new password reset.");
    }

    // Update password
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setPasswordResetToken(null);
    user.setPasswordResetExpires(null);
    userRepository.save(user);

    // Log event
    auditService.logEvent(
        user.getCompany() != null ? user.getCompany().getId() : null,
        user.getId(),
        "PASSWORD_RESET",
        "User",
        user.getId(),
        null,
        null,
        null,
        null,
        AuditLog.Severity.MEDIUM,
        "Password reset requested");
  }

  // Update password (When user is logged in)
  public void updatePassword(Long userId, String currentPassword, String newPassword) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with this id"));

    // Validate current password
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new RuntimeException("Current password dis incorrect");
    }

    // Update to new password
    user.setPassword(newPassword);
    userRepository.save(user);

    // Log event
    auditService.logEvent(
        user.getCompany() != null ? user.getCompany().getId() : null,
        user.getId(),
        "PASSWORD_UPDATE",
        "User",
        user.getId(),
        null,
        null,
        null,
        null,
        AuditLog.Severity.MEDIUM,
        "Password updated successfully");
  }

  // Verify token and get user data
  public Map<String, Object> verifyToken(String token) {
    return jwtUtil.validateTokenAndGetData(token);
  }

  // Get active companies for login selection
  public List<CompanyDTO> getActiveCompanies() {
    return companyRepository.findByStatus(Company.CompanyStatus.ACTIVE).stream()
        .map(CompanyDTO::fromEntity)
        .collect(Collectors.toList());
  }
}
