package com.customersupport.repository;

import com.customersupport.entity.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

  List<Ticket> findByCompanyId(Long companyId);

  List<Ticket> findByCustomerId(Long customerId);

  List<Ticket> findByAgentId(Long agentId);

  List<Ticket> findByCompanyIdAndStatus(Long companyId, Ticket.Status status);

  List<Ticket> findByCustomerIdAndStatus(Long customerId, Ticket.Status status);

  List<Ticket> findByAgentIdAndStatus(Long agentId, Ticket.Status status);

  @Query(
      "SELECT t FROM Ticket t WHERE t.company.id = :companyId AND (t.title LIKE %:keyword% OR"
          + " t.description LIKE %:keyword% OR t.ticketNumber LIKE %:keyword%)")
  List<Ticket> searchTicketByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query(
      "SELECT t FROM Ticket t WHERE t.customer.id = :customerId AND (t.title LIKE %:keyword% OR"
          + " t.description LIKE %:keyword% OR t.ticketNumber LIKE %:keyword%)")
  List<Ticket> searchTicketsByCustomer(
      @Param("customerId") Long customerId, @Param("keyword") String keyword);

  @Query(
      "SELECT t FROM Ticket t WHERE t.agent.id = :agentId AND (t.title LIKE %:keyword% OR"
          + " t.description LIKE %:keyword% OR t.ticketNumber LIKE %:keyword%)")
  List<Ticket> searchTicketByAgent(
      @Param("agentId") Long agentId, @Param("keyword") String keyword);

  @Query("SELECT COUNT(t) FROM Ticket t WHERE t.company.id = :companyId AND t.status = :status")
  Long countByCompanyIdAndStatus(
      @Param("companyId") Long companyId, @Param("status") Ticket.Status status);

  @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer.id = :customerId AND t.status = :status")
  Long countByCustomerIdAndStatus(
      @Param("customerId") Long customerId, @Param("status") Ticket.Status status);

  @Query(
      "SELECT t FROM Ticket t WHERE t.company.id = :companyId AND t.createdAt BETWEEN :startDate"
          + " AND :endDate")
  List<Ticket> findByCompanyIdAndDateRange(
      @Param("companyId") Long companyId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  List<Ticket> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

  @Query("SELECT t FROM Ticket t WHERE t.customer.id = :customerId ORDER BY t.createdAt DESC")
  List<Ticket> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

  @Query(
      "SELECT t FROM Ticket t WHERE t.agent.id = :agentId ORDER BY t.priority DESC, t.createdAt"
          + " ASC")
  List<Ticket> findByAgentIdOrderByPriorityDescCreatedAtAsc(@Param("agentId") Long agentId);

  @Query(
      "SELECT t FROM Ticket t WHERE t.company.id = :companyId AND t.priority = :priority ORDER BY"
          + " t.createdAt ASC")
  List<Ticket> findByCompanyIdAndPriorityOrderByCreatedAtAsc(
      @Param("companyId") Long companyId, Ticket.Priority priority);

  @Query(
      "SELECT t FROM Ticket t WHERE t.company.id = :companyId AND t.agent IS NULL AND t.status ="
          + " 'OPEN' ORDER BY t.priority DESC, t.createdAt ASC")
  List<Ticket> findUnassignedTicketsByCompany(@Param("companyId") Long companyId);

  @Query(
      "SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND t.lastActivity <"
          + " :thresholdTime")
  List<Ticket> findStaleTickets(@Param("thresholdTime") LocalDateTime thresholdTime);

  Optional<Ticket> findByTicketNumber(String ticketNumber);

  boolean existsByTicketNumber(String ticketNumber);

  // Additional missing methods
  Integer countByStatus(Ticket.Status status);

  Long countByCompanyId(Long companyId);

  Long countByAgentId(Long agentId);

  Long countByCustomerId(Long customerId);

  @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agent.id = :agentId AND t.status = :status")
  Long countByAgentIdAndStatus(
      @Param("agentId") Long agentId, @Param("status") Ticket.Status status);

  @Query(
      "SELECT t FROM Ticket t WHERE t.agent.id = :agentId AND t.status = :status AND t.closedAt"
          + " BETWEEN :startDate AND :endDate")
  List<Ticket> findByAgentIdAndStatusAndClosedAtBetween(
      @Param("agentId") Long agentId,
      @Param("status") Ticket.Status status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
