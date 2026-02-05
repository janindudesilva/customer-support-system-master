package com.customersupport.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.customersupport.repository")
public class DatabaseConfig {

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Value("${spring.datasource.username}")
  private String dataSourceUsername;

  @Value("${spring.datasource.password}")
  private String dataSourcePassword;

  @Value("${spring.datasource.driver-class-name}")
  private String dataSourceDriverClassName;

  @Value("${spring.jpa.hibernate.ddl-auto}")
  private String hibernateDdlAuto;

  @Value("${spring.jpa.show-sql}")
  private boolean showSql;

  @Value("${spring.jpa.properties.hibernate.format_sql}")
  private boolean formatSql;

  @Value("${spring.jpa.properties.hibernate.dialect}")
  private String hibernateDialect;

  @Value("${spring.datasource.hikari.connection-timeout:30000}")
  private int connectionTimeout;

  @Value("${spring.datasource.hikari.maximum-pool-size:10}")
  private int maximumPoolSize;

  @Value("${spring.datasource.hikari.minimum-idle:5}")
  private int minimumIdle;

  @Value("${spring.datasource.hikari.idle-timeout:600000}")
  private int idleTimeout;

  @Bean
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(dataSourceUrl);
    config.setUsername(dataSourceUsername);
    config.setPassword(dataSourcePassword);
    config.setDriverClassName(dataSourceDriverClassName);
    config.setConnectionTimeout(connectionTimeout);
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(minimumIdle);
    config.setIdleTimeout(idleTimeout);

    return new HikariDataSource(config);
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource());
    em.setPackagesToScan("com.customersupport.entity");

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);

    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
    properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
    properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));
    properties.setProperty("hibernate.dialect", hibernateDialect);
    em.setJpaProperties(properties);

    return em;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
    return transactionManager;
  }
}
