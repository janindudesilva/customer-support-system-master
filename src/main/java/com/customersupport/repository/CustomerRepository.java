package com.customersupport.repository;

import com.customersupport.entity.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  List<Customer> findByCompanyId(Long companyId);

  List<Customer> findByCustomerType(Customer.CustomerType customerType);

  List<Customer> findByCompanyIdAndCustomerType(Long companyId, Customer.CustomerType customerType);

  @Query(
      "SELECT c FROM Customer c WHERE c.company.id = :companyId AND(c.user.firstname LIKE"
          + " %:keyword% OR c.user.lastname LIKE %:keyword% OR c.user.email LIKE %:keyword%)")
  List<Customer> searchCustomerByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId ORDER BY c.createdAt DESC")
  List<Customer> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") Long companyId);

  @Query("SELECT AVG(c.satisfactionScore) FROM Customer c WHERE c.company.id = :companyId")
  Double getAverageSatisfactionScoreByCompany(@Param("companyId") Long companyId);

  @Query("SELECT COUNT(c) FROM Customer c WHERE c.company.id = :companyId")
  Integer countByCompanyId(@Param("companyId") Long companyId);

  Optional<Customer> findByUserId(Long userId);

  Optional<Customer> findByUserEmail(String email);
}
