package com.customersupport.config;

import com.customersupport.entity.User;
import com.customersupport.repository.UserRepository;
import com.customersupport.util.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SuperAdminPasswordBootstrap {

  private static final Logger log = LoggerFactory.getLogger(SuperAdminPasswordBootstrap.class);

  @Value("${app.reset-superadmin-password:false}")
  private boolean resetSuperAdminPassword;

  @Value("${app.superadmin.email:superadmin@system.com}")
  private String superAdminEmail;

  @Value("${app.superadmin.password:password}")
  private String superAdminPassword;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public SuperAdminPasswordBootstrap(
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void resetSuperAdminPasswordIfEnabled() {
    if (!resetSuperAdminPassword) {
      return;
    }

    userRepository
        .findByEmail(superAdminEmail)
        .ifPresentOrElse(
            user -> {
              user.setPassword(passwordEncoder.encode(superAdminPassword));
              user.setStatus(User.UserStatus.ACTIVE);
              userRepository.save(user);
              log.warn(
                  "Super admin password reset for {}. Disable app.reset-superadmin-password after"
                      + " login.",
                  superAdminEmail);
            },
            () ->
                log.warn(
                    "app.reset-superadmin-password is enabled but no user found for {}",
                    superAdminEmail));
  }
}
