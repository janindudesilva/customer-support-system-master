package com.customersupport.config;

import com.customersupport.entity.User;
import com.customersupport.repository.UserRepository;
import com.customersupport.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    // Skip JWT processing for public endpoints
    if (shouldSkipJwtProcessing(requestURI)) {
      System.out.println("JWT Filter: Skipping public endpoint: " + requestURI);
      filterChain.doFilter(request, response);
      return;
    }

    // Get Authorization header
    final String authHeader = request.getHeader("Authorization");

    // If no token or not Bearer token, continue chain
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      System.out.println("JWT Filter: No Bearer token found for: " + requestURI);
      filterChain.doFilter(request, response);
      return;
    }

    // Extract token
    final String jwt = authHeader.substring(7);

    try {
      // Validate token and get user data
      Map<String, Object> userData = jwtUtil.validateTokenAndGetData(jwt);

      // If token is valid and user is not already authenticated
      if (userData != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Long userId = Long.valueOf(userData.get("userId").toString());
        String roleName = userData.get("role").toString();

        // Get user from database
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
          User user = userOpt.get();

          // Verify user is active
          if (user.getStatus() != User.UserStatus.ACTIVE) {
            filterChain.doFilter(request, response);
            return;
          }

          // Create authorities from role
          List<GrantedAuthority> authorities = new ArrayList<>();
          authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

          // Create authentication token
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);

          // Set authentication details
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // Set authentication in context
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      // Token validation failed - do not set authentication
      logger.error("Error validating JWT token: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  /** Check if JWT processing should be skipped for the given URI */
  private boolean shouldSkipJwtProcessing(String requestURI) {
    System.out.println("DEBUG: shouldSkipJwtProcessing called for URI: " + requestURI);

    final Set<String> exactPublicPaths =
        Set.of(
            "/",
            "/favicon.ico",
            "/index.html",
            "/login.html",
            "/register.html",
            "/company-selection.html",
            "/super-admin/dashboard.html",
            "/super-admin/company-management.html",
            "/super-admin/company-registration.html",
            "/company-admin/dashboard.html",
            "/company-admin/user-management.html",
            "/company-admin/agent-registration.html",
            "/agent/dashboard.html",
            "/agent/ticket-queue.html",
            "/agent/ticket-response.html",
            "/customer/dashboard.html",
            "/customer/registration.html",
            "/customer/ticket-create.html",
            "/customer/ticket-list.html",
            "/auth/login.html",
            "/auth/company-selection.html",
            "/auth/password-reset-request.html",
            "/auth/password-reset.html",
            "/api/customers/register");

    final List<String> prefixPublicPaths =
        List.of("/css/", "/js/", "/images/", "/static/", "/api/auth/", "/api/test/");

    if (exactPublicPaths.contains(requestURI)) {
      return true;
    }

    for (String prefix : prefixPublicPaths) {
      if (requestURI.startsWith(prefix)) {
        System.out.println(
            "DEBUG: Request URI '"
                + requestURI
                + "' matches public endpoint prefix '"
                + prefix
                + "'");
        return true;
      }
    }

    System.out.println(
        "DEBUG: Request URI '"
            + requestURI
            + "' does NOT match any public endpoint pattern. Should require JWT.");
    return false;
  }
}
