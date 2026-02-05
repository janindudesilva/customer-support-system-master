package com.customersupport.controller;

import com.customersupport.dto.LoginDTO;
import com.customersupport.service.SimpleAuthService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

  @Autowired private SimpleAuthService simpleAuthService;

  @GetMapping
  public ResponseEntity<?> test() {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Test controller working");
    response.put("timestamp", System.currentTimeMillis());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<?> testLogin(@RequestBody LoginDTO loginDTO) {
    try {
      Map<String, Object> response = simpleAuthService.simpleLogin(loginDTO);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/verify-password")
  public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request) {
    try {
      String email = request.get("email");
      String password = request.get("password");

      // Simple verification without complex logic
      Map<String, Object> response = new HashMap<>();
      response.put("email", email);
      response.put("password", password);
      response.put("message", "Password verification test");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}
