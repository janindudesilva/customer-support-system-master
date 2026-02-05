package com.customersupport.repository;

import com.customersupport.entity.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

  Optional<Company> findByEmail(String email);

  Optional<Company> findByName(String name);

  List<Company> findByStatus(Company.CompanyStatus status);

  List<Company> findBySubscriptionPlan(Company.SubscriptionPlan subscriptionPlan);

  @Query("SELECT c FROM Company c WHERE c.name LIKE %:keyword% OR c.email LIKE %:keyword%")
  List<Company> searchCompanies(@Param("keyword") String keyword);

  @Query(
      "SELECT COUNT(u) FROM User u WHERE u.company.id = :companyId AND u.role.name ="
          + " 'SUPPORT_AGENT'")
  Integer countAgentsByCompanyId(@Param("companyId") Long companyId);

  @Query("SELECT COUNT(c) FROM Customer c WHERE c.company.id = :companyId")
  Integer countCustomersByCompanyId(@Param("companyId") Long companyId);

  @Query("SELECT c FROM Company c ORDER BY c.createdAt DESC")
  List<Company> findAllOrderByCreatedAtDesc();

  boolean existsByEmail(String email);

  boolean existsByName(String name);

  Integer countByStatus(Company.CompanyStatus status);
}
