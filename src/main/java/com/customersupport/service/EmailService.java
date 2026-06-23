package com.customersupport.service;

import com.customersupport.dto.CompanyAdminRegistrationDTO;
import com.customersupport.dto.ReviewDTO;
import com.customersupport.dto.TicketDTO;
import com.customersupport.entity.Company;
import com.customersupport.entity.Review;
import com.customersupport.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

  @Autowired private JavaMailSender emailSender;

  @Autowired private SpringTemplateEngine templateEngine;

  @Value("${spring.mail.username}")
  private String fromEmail;

  /**
   * Send company admin registration email with login credentials
   *
   * @param adminDTO The company admin registration data
   * @param company The company details
   * @param password The generated or provided password
   */
  public void sendCompanyAdminRegistrationEmail(
      CompanyAdminRegistrationDTO adminDTO, Company company, String password) {
    String subject = "Welcome to Customer Support System - Your Admin Account Details";

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("adminName", adminDTO.getFirstName() + " " + adminDTO.getLastName());
    templateModel.put("companyName", company.getName());
    templateModel.put("email", adminDTO.getEmail());
    templateModel.put("password", password);
    templateModel.put("loginUrl", "http://localhost:8082/auth/login");
    templateModel.put("companyDetails", formatCompanyDetails(company));

    try {
      sendHtmlMessage(adminDTO.getEmail(), subject, "company-admin-registration", templateModel);
    } catch (MessagingException e) {
      // Log the error but don't interrupt the user creation flow
      e.printStackTrace();
    }
  }

  /**
   * Send password reset email
   *
   * @param user The user entity
   * @param newPassword The new password
   */
  public void sendPasswordResetEmail(User user, String newPassword) {
    String subject = "Customer Support System - Password Reset";

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("userName", user.getFirstname() + " " + user.getLastname());
    templateModel.put("newPassword", newPassword);
    templateModel.put("loginUrl", "http://localhost:8082/auth/login");

    try {
      sendHtmlMessage(user.getEmail(), subject, "password-reset", templateModel);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  /**
   * Send agent registration email with login credentials
   *
   * @param firstName The agent's first name
   * @param lastName The agent's last name
   * @param email The agent's email address
   * @param password The generated or provided password
   * @param company The company the agent belongs to
   * @param department The agent's department
   * @param specialization The agent's specialization
   */
  public void sendAgentRegistrationEmail(
      String firstName,
      String lastName,
      String email,
      String password,
      Company company,
      String department,
      String specialization) {
    String subject = "Welcome to " + company.getName() + " Support Team - Your Account Details";

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("agentName", firstName + " " + lastName);
    templateModel.put("companyName", company.getName());
    templateModel.put("email", email);
    templateModel.put("password", password);
    templateModel.put("department", department != null ? department : "General Support");
    templateModel.put(
        "specialization", specialization != null ? specialization : "General Support");
    templateModel.put("loginUrl", "http://localhost:8082/auth/login");
    templateModel.put("companyDetails", formatCompanyDetails(company));

    try {
      sendHtmlMessage(email, subject, "agent-registration", templateModel);
    } catch (MessagingException e) {
      // Log the error but don't interrupt the agent creation flow
      e.printStackTrace();
    }
  }

  /**
   * Utility method to send HTML email using a template
   *
   * @param to Recipient email address
   * @param subject Email subject
   * @param templateName Name of the HTML template (without .html extension)
   * @param templateModel Model containing template variables
   * @throws MessagingException If there's an error sending the email
   */
  private void sendHtmlMessage(
      String to, String subject, String templateName, Map<String, Object> templateModel)
      throws MessagingException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    Context context = new Context();
    context.setVariables(templateModel);
    String htmlContent = templateEngine.process(templateName, context);

    helper.setFrom(fromEmail);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    emailSender.send(message);
  }

  /**
   * Format company details as HTML
   *
   * @param company The company entity
   * @return Formatted HTML string with company details
   */
  private String formatCompanyDetails(Company company) {
    StringBuilder details = new StringBuilder();
    details.append("<ul>");
    details.append("<li><strong>Company Name:</strong> ").append(company.getName()).append("</li>");
    details.append("<li><strong>Email:</strong> ").append(company.getEmail()).append("</li>");

    if (company.getPhone() != null && !company.getPhone().isEmpty()) {
      details.append("<li><strong>Phone:</strong> ").append(company.getPhone()).append("</li>");
    }

    if (company.getAddress() != null && !company.getAddress().isEmpty()) {
      details.append("<li><strong>Address:</strong> ").append(company.getAddress()).append("</li>");
    }

    if (company.getWebsite() != null && !company.getWebsite().isEmpty()) {
      details.append("<li><strong>Website:</strong> ").append(company.getWebsite()).append("</li>");
    }

    details
        .append("<li><strong>Subscription Plan:</strong> ")
        .append(company.getSubscriptionPlan())
        .append("</li>");
    details
        .append("<li><strong>Maximum Agents:</strong> ")
        .append(company.getMaxAgents())
        .append("</li>");
    details
        .append("<li><strong>Maximum Customers:</strong> ")
        .append(company.getMaxCustomers())
        .append("</li>");
    details.append("</ul>");

    return details.toString();
  }

  /**
   * Send notification to agents when a new ticket is created
   *
   * @param agentEmail The email address of the support agent
   * @param agentName The full name of the support agent
   * @param ticket The ticket entity that was created
   * @param customerName The name of the customer who created the ticket
   */
  public void sendNewTicketNotification(
      String agentEmail, String agentName, TicketDTO ticket, String customerName) {
    String subject = "New Support Ticket: " + ticket.getTicketNumber() + " - " + ticket.getTitle();

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("agentName", agentName);
    templateModel.put("customerName", customerName);
    templateModel.put("ticketNumber", ticket.getTicketNumber());
    templateModel.put("ticketTitle", ticket.getTitle());
    templateModel.put("ticketDescription", ticket.getDescription());
    templateModel.put("ticketPriority", ticket.getPriority().toString());
    templateModel.put(
        "ticketCategory",
        ticket.getCategoryName() != null ? ticket.getCategoryName() : "Uncategorized");
    templateModel.put(
        "ticketCreatedAt",
        ticket.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    templateModel.put(
        "ticketUrl", "http://localhost:8082/agent/ticket-response?ticketId=" + ticket.getId());

    try {
      sendHtmlMessage(agentEmail, subject, "ticket-notification", templateModel);
    } catch (MessagingException e) {
      // Log the error but don't interrupt the ticket creation flow
      e.printStackTrace();
    }
  }

  /**
   * Send notification to customer when a support agent claims their ticket
   *
   * @param customerEmail The email address of the customer
   * @param customerName The full name of the customer
   * @param ticket The ticket entity that was claimed
   * @param agentName The name of the agent who claimed the ticket
   */
  public void sendTicketClaimedNotification(
      String customerEmail, String customerName, TicketDTO ticket, String agentName) {
    String subject = "Your Support Ticket Has Been Assigned: " + ticket.getTicketNumber();

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("customerName", customerName);
    templateModel.put("agentName", agentName);
    templateModel.put("ticketNumber", ticket.getTicketNumber());
    templateModel.put("ticketTitle", ticket.getTitle());
    templateModel.put("ticketPriority", ticket.getPriority().toString());
    templateModel.put(
        "ticketCategory",
        ticket.getCategoryName() != null ? ticket.getCategoryName() : "Uncategorized");
    templateModel.put(
        "ticketCreatedAt",
        ticket.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    templateModel.put("ticketUrl", "http://localhost:8082/customer/tickets/" + ticket.getId());

    try {
      sendHtmlMessage(customerEmail, subject, "ticket-claimed", templateModel);
    } catch (MessagingException e) {
      // Log the error but don't interrupt the ticket claiming flow
      e.printStackTrace();
    }
  }

  /**
   * Send notification to company admins when a customer submits a review
   *
   * @param adminEmail The email address of the company admin
   * @param adminName The name of the company admin
   * @param review The review data that was submitted
   * @param customerName The name of the customer who submitted the review
   * @param ticketNumber The ticket number that was reviewed
   * @param companyName The name of the company
   */
  public void sendReviewNotification(
      String adminEmail,
      String adminName,
      ReviewDTO review,
      String customerName,
      String ticketNumber,
      String companyName) {
    String subject = "New Customer Review for " + companyName;

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("adminName", adminName);
    templateModel.put("customerName", customerName);
    templateModel.put("ticketNumber", ticketNumber);
    templateModel.put("reviewId", review.getId());
    templateModel.put("rating", review.getRating());
    templateModel.put("feedback", review.getFeedback());
    templateModel.put("serviceQualityRating", review.getServiceQualityRating());
    templateModel.put("responseTimeRating", review.getResponseTimeRating());
    templateModel.put("professionalismRating", review.getProfessionalismRating());
    templateModel.put("wouldRecommend", review.getWouldRecommend());
    templateModel.put("additionalComments", review.getAdditionalComments());
    templateModel.put(
        "createdAt",
        review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    templateModel.put(
        "reviewUrl", "http://localhost:8082/company-admin/reviews?reviewId=" + review.getId());
    templateModel.put("companyName", companyName);

    try {
      sendHtmlMessage(adminEmail, subject, "review-notification", templateModel);
    } catch (MessagingException e) {
      // Log the error but don't interrupt the review creation flow
      e.printStackTrace();
    }
  }

  /**
   * Send notification to a company admin when a customer submits a review
   *
   * @param review The review entity that was submitted
   * @param admin The company admin user to notify
   */
  public void sendNewReviewNotification(Review review, User admin) {
    String subject = "New Customer Review for " + review.getCompany().getName();

    // Get customer name
    String customerName =
        review.getCustomer().getUser().getFirstname()
            + " "
            + review.getCustomer().getUser().getLastname();

    // Get ticket number
    String ticketNumber = review.getTicket().getTicketNumber();

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("adminName", admin.getFirstname() + " " + admin.getLastname());
    templateModel.put("customerName", customerName);
    templateModel.put("ticketNumber", ticketNumber);
    templateModel.put("reviewId", review.getId());
    templateModel.put("rating", review.getRating());
    templateModel.put("feedback", review.getFeedback());
    templateModel.put("serviceQualityRating", review.getServiceQualityRating());
    templateModel.put("responseTimeRating", review.getResponseTimeRating());
    templateModel.put("professionalismRating", review.getProfessionalismRating());
    templateModel.put("wouldRecommend", review.getWouldRecommend());
    templateModel.put("additionalComments", review.getAdditionalComments());
    templateModel.put(
        "createdAt",
        review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    templateModel.put(
        "reviewUrl", "http://localhost:8082/company-admin/reviews?reviewId=" + review.getId());
    templateModel.put("companyName", review.getCompany().getName());

    try {
      sendHtmlMessage(admin.getEmail(), subject, "review-notification", templateModel);
    } catch (MessagingException e) {
      // Log the error but don't interrupt the review creation flow
      e.printStackTrace();
    }
  }
}
