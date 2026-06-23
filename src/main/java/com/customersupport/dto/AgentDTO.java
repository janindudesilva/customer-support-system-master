package com.customersupport.dto;

import com.customersupport.entity.Agent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentDTO {
  private Long id;
  private Long userId;
  private String firstName;
  private String lastName;
  private String fullName;
  private String email;
  private String phone;
  private Long companyId;
  private String companyName;
  private String department;
  private String specialization;
  private Integer maxConcurrentTickets;
  private Integer currentTicketCount;
  private Integer totalTicketsHandled;
  private BigDecimal averageResolutionTime;
  private BigDecimal customerSatisfactionRating;
  private Boolean isAvailable;
  private LocalDateTime shiftStart;
  private LocalDateTime shiftEnd;
  private String workingDays;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public AgentDTO fromEntity(Agent agent) {
    AgentDTO dto = new AgentDTO();
    dto.setId(agent.getId());
    dto.setUserId(agent.getUser().getId());
    dto.setFirstName(agent.getUser().getFirstname());
    dto.setLastName(agent.getUser().getLastname());
    dto.setEmail(agent.getUser().getEmail());
    dto.setPhone(agent.getUser().getPhone());
    dto.setCompanyId(agent.getCompany().getId());
    dto.setCompanyName(agent.getCompany().getName());
    dto.setDepartment(agent.getDepartment());
    dto.setSpecialization(agent.getSpecialization());
    dto.setMaxConcurrentTickets(agent.getMaxConcurrentTickets());
    dto.setCurrentTicketCount(agent.getCurrentTicketCount());
    dto.setTotalTicketsHandled(agent.getTotalTicketsHandled());
    dto.setAverageResolutionTime(agent.getAverageResolutionTime());
    dto.setCustomerSatisfactionRating(agent.getCustomerSatisfactionRating());
    dto.setIsAvailable(agent.getIsAvailable());
    dto.setShiftStart(agent.getShiftStart());
    dto.setShiftEnd(agent.getShiftEnd());
    dto.setWorkingDays(agent.getWorkingDays());
    dto.setCreatedAt(agent.getCreatedAt());
    dto.setUpdatedAt(agent.getUpdatedAt());
    return dto;
  }
}
