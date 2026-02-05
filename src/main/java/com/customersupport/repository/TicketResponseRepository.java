package com.customersupport.repository;

import com.customersupport.entity.TicketResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketResponseRepository extends JpaRepository<TicketResponse, Long> {

  List<TicketResponse> findByTicketId(Long ticketId);

  List<TicketResponse> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

  List<TicketResponse> findByUserId(Long userId);

  @Query(
      "SELECT tr FROM TicketResponse  tr WHERE tr.ticket.id = :ticketId AND tr.responseType ="
          + " :responseType ORDER BY tr.createdAt ASC")
  List<TicketResponse> findPublicResponsesByTicketId(@Param("ticketId") Long ticketId);

  @Query(
      "SELECT tr FROM TicketResponse tr WHERE tr.user.id = :userId AND DATE (tr.createdAt) ="
          + " CURRENT_DATE")
  List<TicketResponse> countTodayResponsesByUserId(@Param("userId") Long userId);

  @Query(
      "SELECT tr FROM TicketResponse tr WHERE tr.user.id = :userId AND tr.responseType ="
          + " 'AGENT_REPLY'")
  List<TicketResponse> getAverageResponseTimeUserId(@Param("userId") Long userId);

  List<TicketResponse> findByUserIdOrderByCreatedAtDesc(Long userId);
}
