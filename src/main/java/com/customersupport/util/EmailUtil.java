package com.customersupport.util;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

  @Autowired private JavaMailSender mailSender;

  @Value("${spring.mail.username:noreply@customersupport.com}")
  private String fromEmail;

  public void sendEmail(String to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Error sending email" + e.getMessage());
    }
  }

  public void sendEmailWithAttachment(
      String to, String subject, String body, String pathToAttachment) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true);

      File file = new File(pathToAttachment);
      helper.addAttachment(file.getName(), file);

      mailSender.send(message);

    } catch (Exception e) {
      System.err.println("Error sending email with attachment" + e.getMessage());
    }
  }

  public void sendHtmlEmail(String to, String subject, String htmlBody) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true);

      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Error sending HTML email" + e.getMessage());
    }
  }

  public void sendMultipleRecipients(String[] to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Error sending email to multiple recipients: " + e.getMessage());
    }
  }
}
