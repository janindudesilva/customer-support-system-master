package com.customersupport.repository;

import com.customersupport.entity.Agent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

  List<Agent> findByCompanyId(Long companyId);

  List<Agent> findByCompanyIdAndIsAvailable(Long companyId, Boolean isAvailable);

  List<Agent> findByDepartment(String department);

  List<Agent> findByCompanyIdAndDepartment(Long companyId, String department);

  @Query(
      "SELECT a FROM Agent a WHERE a.company.id = :companyId AND (a.user.firstname LIKE %:keyword%"
          + " OR a.user.lastname LIKE %:keyword% OR a.user.email LIKE %:keyword% OR a.department"
          + " LIKE %:keyword%)")
  List<Agent> searchAgentsByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query("SELECT a FROM Agent a WHERE a.company.id = :companyId ORDER BY a.createdAt DESC")
  List<Agent> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") Long companyId);

  @Query(
      "SELECT a FROM Agent a WHERE a.isAvailable = true AND a.currentTicketCount <"
          + " a.maxConcurrentTickets ORDER BY a.currentTicketCount ASC")
  List<Agent> findAvailableAgentsOrderByWorkload();

  @Query("SELECT AVG(a.customerSatisfactionRating) FROM Agent a WHERE a.company.id = :companyId")
  Double getAverageCustomerSatisfactionByCompany(@Param("companyId") Long companyId);

  @Query("SELECT COUNT(a) FROM Agent a WHERE a.company.id = :companyId")
  Integer countByCompanyId(@Param("companyId") Long companyId);

  Optional<Agent> findByUserId(Long userId);

  Optional<Agent> findByUserEmail(String email);

  @Query("SELECT a FROM Agent a JOIN FETCH a.company JOIN FETCH a.user WHERE a.user.id = :userId")
  Optional<Agent> findDetailedByUserId(@Param("userId") Long userId);
}
