package com.customersupport.dto;

import com.customersupport.entity.TicketResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

  private Long id;

  @NotNull(message = "Ticket ID is required")
  private Long ticketId;

  @NotNull(message = "User ID is required")
  private Long userId;

  private String userName;

  private TicketResponse.ResponseType responseType = TicketResponse.ResponseType.AGENT_REPLY;

  @NotBlank(message = "Message is required")
  private String message;

  private String attachments;

  private Boolean isPublic = true;

  private BigDecimal responseTime;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public static TicketResponseDTO fromEntity(TicketResponse response) {
    TicketResponseDTO dto = new TicketResponseDTO();
    dto.setId(response.getId());
    dto.setTicketId(response.getTicket().getId());
    dto.setUserId(response.getUser().getId());
    dto.setUserName(response.getUser().getFirstname() + " " + response.getUser().getLastname());
    dto.setResponseType(response.getResponseType());
    dto.setMessage(response.getMessage());
    dto.setAttachments(response.getAttachments());
    dto.setIsPublic(response.getIsPublic());
    dto.setResponseTime(response.getResponseTime());
    dto.setCreatedAt(response.getCreatedAt());
    dto.setUpdatedAt(response.getUpdatedAt());
    return dto;
  }
}
