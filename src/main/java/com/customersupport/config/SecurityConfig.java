package com.customersupport.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  @SuppressWarnings("removal")
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
    http.csrf()
        .disable()
        .cors()
        .configurationSource(corsConfigurationSource())
        .and()
        .authorizeHttpRequests()
        .requestMatchers(
            "/",
            "/index.html",
            "/login.html",
            "/register.html",
            "/company-selection.html",
            "/favicon.ico")
        .permitAll()
        .requestMatchers(
            "/api/auth/**",
            "/auth/**",
            "/api/test/**",
            "/api/customers/register",
            "/css/**",
            "/js/**",
            "/images/**",
            "/static/**")
        .permitAll()
        .requestMatchers("/super-admin/**", "/company-admin/**", "/agent/**", "/customer/**")
        .permitAll()
        .requestMatchers("/api/system-feedback/public")
        .permitAll()
        .requestMatchers(HttpMethod.POST, "/api/system-feedback")
        .hasRole("COMPANY_ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/system-feedback/company/**")
        .hasRole("COMPANY_ADMIN")
        .requestMatchers("/api/system-feedback/pending", "/api/system-feedback/status/**")
        .hasRole("SUPER_ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/system-feedback/*/status")
        .hasRole("SUPER_ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/system-feedback/*")
        .hasRole("COMPANY_ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/system-feedback/*")
        .hasRole("SUPER_ADMIN")
        .requestMatchers("/api/super-admin/**")
        .hasRole("SUPER_ADMIN")
        .requestMatchers("/api/company-admin/**")
        .hasRole("COMPANY_ADMIN")
        .requestMatchers("/api/agents/**")
        .hasRole("SUPPORT_AGENT")
        .requestMatchers("/api/customers/**")
        .hasAnyRole("CUSTOMER", "COMPANY_ADMIN")
        .requestMatchers("/api/faqs/company/*/published/**")
        .permitAll()
        .requestMatchers("/api/reviews/company/*/published", "/api/reviews/company/*/featured")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling()
        .authenticationEntryPoint(
            (request, response, authException) -> {
              System.err.println(
                  "Authentication entry point triggered for: "
                      + request.getRequestURI()
                      + ", Method: "
                      + request.getMethod()
                      + ", Exception: "
                      + authException.getMessage());
              response.setContentType("application/json");
              response.setStatus(401);
              response
                  .getWriter()
                  .write(
                      "{\"error\":\"Unauthorized\",\"message\":\""
                          + authException.getMessage()
                          + "\"}");
            })
        .accessDeniedHandler(
            (request, response, accessDeniedException) -> {
              System.err.println(
                  "Access denied for: "
                      + request.getRequestURI()
                      + ", Method: "
                      + request.getMethod()
                      + ", Exception: "
                      + accessDeniedException.getMessage());
              response.setContentType("application/json");
              response.setStatus(403);
              response
                  .getWriter()
                  .write(
                      "{\"error\":\"Forbidden\",\"message\":\""
                          + accessDeniedException.getMessage()
                          + "\"}");
            });

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
