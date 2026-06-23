package com.customersupport.service;

import com.customersupport.dto.LoginDTO;
import com.customersupport.entity.User;
import com.customersupport.repository.UserRepository;
import com.customersupport.util.PasswordEncoder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleAuthService {

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  public Map<String, Object> simpleLogin(LoginDTO loginDTO) {
    try {
      // Find user by email
      User user =
          userRepository
              .findByEmail(loginDTO.getEmail())
              .orElseThrow(() -> new RuntimeException("Invalid email or password"));

      // Check password
      if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid email or password");
      }

      // Create response
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("userId", user.getId());
      response.put("email", user.getEmail());
      response.put("firstName", user.getFirstname());
      response.put("lastName", user.getLastname());
      response.put("role", user.getRole() != null ? user.getRole().getName() : "UNKNOWN");
      response.put("companyId", user.getCompany() != null ? user.getCompany().getId() : null);
      response.put("companyName", user.getCompany() != null ? user.getCompany().getName() : null);

      return response;
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("error", e.getMessage());
      return errorResponse;
    }
  }
}
