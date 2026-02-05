package com.customersupport.config;

import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class ThymeleafEmailConfig {

  @Primary
  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(htmlTemplateResolver());
    return templateEngine;
  }

  @Bean
  public SpringResourceTemplateResolver htmlTemplateResolver() {
    SpringResourceTemplateResolver emailTemplateResolver = new SpringResourceTemplateResolver();
    emailTemplateResolver.setPrefix("classpath:/templates/emails/");
    emailTemplateResolver.setSuffix(".html");
    emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
    emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    emailTemplateResolver.setCacheable(false);
    return emailTemplateResolver;
  }

  @Bean(name = "pdfTemplateEngine")
  public SpringTemplateEngine pdfTemplateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(pdfTemplateResolver());
    return templateEngine;
  }

  @Bean
  public ITemplateResolver pdfTemplateResolver() {
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
    templateResolver.setPrefix("classpath:/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    templateResolver.setCacheable(false);
    return templateResolver;
  }
}
