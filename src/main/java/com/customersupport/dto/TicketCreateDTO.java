package com.customersupport.dto;

import com.customersupport.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketCreateDTO {

  private Long companyId;

  private Long customerId;

  private Long categoryId;

  @NotBlank(message = "Title is required")
  @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
  private String title;

  @NotBlank(message = "Description is required")
  @Size(min = 10, message = "Description must be at least 10 characters")
  private String description;

  private Ticket.Priority priority = Ticket.Priority.MEDIUM;

  private Ticket.Source source = Ticket.Source.WEB;

  private String tags;

  private String attachments;
}
