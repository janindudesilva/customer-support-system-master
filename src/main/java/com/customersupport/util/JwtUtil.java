package com.customersupport.util;

import com.customersupport.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret:defaultSecretKey12345678901234567890}")
  private String secret;

  @Value("${jwt.expiration:86400000}") // Default 24 hours
  private long expirationTime;

  private Key getSigningKey() {
    byte[] keyBytes = secret.getBytes();
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("email", user.getEmail());
    claims.put("role", user.getRole().getName());

    if (user.getCompany() != null) {
      claims.put("companyId", user.getCompany().getId());
      claims.put("companyName", user.getCompany().getName());
    }

    return createToken(claims);
  }

  private String createToken(Map<String, Object> claims) {
    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public Map<String, Object> validateTokenAndGetData(String token) {
    final Claims claims = getAllClaimsFromToken(token);

    // Check if token is expired
    if (claims.getExpiration().before(new Date())) {
      throw new RuntimeException("Token has expired");
    }

    Map<String, Object> userData = new HashMap<>();
    userData.put("userId", claims.get("userId"));
    userData.put("email", claims.get("email"));
    userData.put("role", claims.get("role"));

    if (claims.get("companyId") != null) {
      userData.put("companyId", claims.get("companyId"));
      userData.put("companyName", claims.get("companyName"));
    }

    return userData;
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }
}
