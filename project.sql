USE customer_support_system;

-- Company table
CREATE TABLE companies(
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL UNIQUE,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          phone VARCHAR(12),
                          address TEXT,
                          website VARCHAR(255),
                          status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
                          subscription_plans ENUM('BASIC', 'PREMIUM', 'ENTERPRISE') DEFAULT 'BASIC',
                          max_agent INT DEFAULT 10,
                          max_customers INT DEFAULT 1000,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          created_date BIGINT,
                          created_by BIGINT,
                          INDEX idx_company_status (status),
                          INDEX idx_company_name (name),
                          CHECK (max_agent > 0),
                          CHECK (max_customers > 0)
);

-- Roles table
CREATE TABLE roles(
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(50) NOT NULL UNIQUE,
                      description TEXT,
                      permissions JSON,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert roles
INSERT INTO roles(name, description, permissions) VALUES
                                                      ('SUPER_ADMIN', 'System Super Administrator','[
                                                        "CREATE_COMPANIES",
                                                        "READ_ALL_COMPANIES",
                                                        "UPDATE_COMPANIES",
                                                        "DELETE_COMPANIES",
                                                        "DEACTIVATE_COMPANIES",
                                                        "SUSPEND_COMPANIES",
                                                        "MANAGE_SUBSCRIPTION_PLANS",
                                                        "VIEW_ALL_DATA",
                                                        "VIEW_SYSTEM_ANALYTICS",
                                                        "SYSTEM_SETTINGS",
                                                        "VIEW_ALL_AUDIT_LOGS",
                                                        "MANAGE_SUPER_USERS",
                                                        "ACCESS_ALL_COMPANY_DATA"
                                                      ]'),
                                                      ('COMPANY_ADMIN', 'Company Administrator', '["CREATE_USERS",
                                                        "READ_COMPANY_USERS",
                                                        "UPDATE_USERS",
                                                        "DELETE_USERS",
                                                        "DEACTIVATE_USERS",
                                                        "CREATE_AGENTS",
                                                        "READ_AGENTS",
                                                        "UPDATE_AGENTS",
                                                        "DELETE_AGENTS",
                                                        "CREATE_CUSTOMERS",
                                                        "READ_CUSTOMERS",
                                                        "UPDATE_CUSTOMERS",
                                                        "DELETE_CUSTOMERS",
                                                        "ASSIGN_ROLES",
                                                        "RESET_PASSWORDS",
                                                        "VIEW_USER_ACTIVITY_LOGS",
                                                        "MANAGE_USER_PERMISSIONS",
                                                        "CREATE_FAQ",
                                                        "READ_FAQ",
                                                        "UPDATE_FAQ",
                                                        "DELETE_FAQ",
                                                        "CREATE_CATEGORIES",
                                                        "READ_CATEGORIES",
                                                        "UPDATE_CATEGORIES",
                                                        "DELETE_CATEGORIES",
                                                        "MANAGE_FAQ_CATEGORIES",
                                                        "VIEW_COMPANY_ANALYTICS",
                                                        "CREATE_PERFORMANCE_REPORTS",
                                                        "READ_COMPANY_REVIEWS",
                                                        "MODERATE_REVIEWS",
                                                        "DELETE_INAPPROPRIATE_REVIEWS",
                                                        "VIEW_AGENT_PERFORMANCE",
                                                        "MANAGE_COMPANY_SETTINGS",
                                                        "VIEW_COMPANY_AUDIT_LOGS",
                                                        "EXPORT_COMPANY_DATA"
                                                      ]'),
                                                      ('SUPPORT_AGENT', 'Customer Support Agent', '["READ_ASSIGNED_TICKETS",
                                                        "UPDATE_ASSIGNED_TICKETS",
                                                        "UPDATE_TICKET_STATUS",
                                                        "CREATE_TICKET_RESPONSES",
                                                        "READ_TICKET_RESPONSES",
                                                        "UPDATE_TICKET_RESPONSES",
                                                        "DELETE_DRAFT_RESPONSES",
                                                        "CREATE_INTERNAL_NOTES",
                                                        "READ_INTERNAL_NOTES",
                                                        "UPDATE_INTERNAL_NOTES",
                                                        "DELETE_INTERNAL_NOTES",
                                                        "VIEW_CUSTOMER_INFO",
                                                        "VIEW_CUSTOMER_HISTORY",
                                                        "VIEW_TICKET_HISTORY",
                                                        "CHANGE_TICKET_PRIORITY",
                                                        "ASSIGN_TICKETS_TO_OTHERS",
                                                        "CREATE_FOLLOW_UP_TASKS",
                                                        "UPDATE_RESOLUTION_DETAILS",
                                                        "ARCHIVE_COMPLETED_TICKETS",
                                                        "GENERATE_TICKET_REPORTS",
                                                        "VIEW_TICKET_QUEUE",
                                                        "VIEW_ASSIGNED_REVIEWS",
                                                        "RESPOND_TO_REVIEWS",
                                                        "READ_FAQ",
                                                        "SEARCH_FAQ",
                                                        "VIEW_OWN_PERFORMANCE_METRICS"]'),
                                                      ('CUSTOMER', 'Customer User', '[ "CREATE_TICKETS",
                                                        "READ_OWN_TICKETS",
                                                        "UPDATE_OWN_TICKETS",
                                                        "DELETE_OWN_TICKETS",
                                                        "CANCEL_OWN_TICKETS",
                                                        "CREATE_TICKET_RESPONSES",
                                                        "READ_OWN_TICKET_RESPONSES",
                                                        "UPDATE_OWN_TICKET_RESPONSES",
                                                        "ADD_TICKET_COMMENTS",
                                                        "UPDATE_TICKET_PRIORITY",
                                                        "UPLOAD_ATTACHMENTS",
                                                        "VIEW_TICKET_STATUS",
                                                        "VIEW_TICKET_HISTORY",
                                                        "WITHDRAW_TICKET_REQUESTS",
                                                        "CREATE_REVIEWS",
                                                        "READ_OWN_REVIEWS",
                                                        "UPDATE_OWN_REVIEWS",
                                                        "DELETE_OWN_REVIEWS",
                                                        "CREATE_SATISFACTION_RATINGS",
                                                        "VIEW_OWN_FEEDBACK_HISTORY",
                                                        "READ_FAQ",
                                                        "SEARCH_FAQ",
                                                        "VIEW_FAQ_CATEGORIES",
                                                        "BROWSE_KNOWLEDGE_BASE",
                                                        "RATE_FAQ_HELPFULNESS",
                                                        "VIEW_OWN_PROFILE",
                                                        "UPDATE_OWN_PROFILE",
                                                        "VIEW_OWN_NOTIFICATIONS",
                                                        "MARK_NOTIFICATIONS_READ"]');


-- User table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       company_id BIGINT,
                       role_id BIGINT NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),
                       status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
                       email_verified BOOLEAN DEFAULT FALSE,
                       last_login TIMESTAMP NULL,
                       failed_login_attempts INT DEFAULT 0,
                       password_reset_token VARCHAR(255) NULL,
                       password_reset_expires TIMESTAMP NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       created_by BIGINT,
                       FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                       FOREIGN KEY (role_id) REFERENCES roles(id),
                       UNIQUE KEY unique_email_company (email, company_id),
                       INDEX idx_user_email (email),
                       INDEX idx_user_company (company_id),
                       INDEX idx_user_role (role_id),
                       INDEX idx_user_status (status)
);

-- Customer table
CREATE TABLE customers (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL UNIQUE,
                           company_id BIGINT NOT NULL,
                           customer_type ENUM('INDIVIDUAL', 'BUSINESS') DEFAULT 'INDIVIDUAL',
                           date_of_birth DATETIME,
                           address TEXT,
                           preferred_contact_method ENUM('EMAIL', 'PHONE', 'SMS') DEFAULT 'EMAIL',
                           timezone VARCHAR(50) DEFAULT 'UTC',
                           language_preference VARCHAR(10) DEFAULT 'en',
                           satisfaction_score DECIMAL(3,2) DEFAULT 0.00,
                           total_tickets INT DEFAULT 0,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                           INDEX idx_customer_company (company_id),
                           INDEX idx_customer_type (customer_type)
);

-- Agents table
CREATE TABLE agents (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL UNIQUE,
                        company_id BIGINT NOT NULL,
                        department VARCHAR(100),
                        specialization TEXT,
                        max_concurrent_tickets INT DEFAULT 10,
                        current_ticket_count INT DEFAULT 0,
                        total_tickets_handled INT DEFAULT 0,
                        average_resolution_time DECIMAL(8,2) DEFAULT 0.00,
                        customer_satisfaction_rating DECIMAL(3,2) DEFAULT 0.00,
                        is_available BOOLEAN DEFAULT TRUE,
                        shift_start DATETIME,
                        shift_end DATETIME,
                        working_days JSON,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                        INDEX idx_agent_company (company_id),
                        INDEX idx_agent_availability (is_available),
                        INDEX idx_agent_department (department)
);

-- Categories table
CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            company_id BIGINT NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            description TEXT,
                            parent_id BIGINT NULL,
                            color_code VARCHAR(7) DEFAULT '#007bff',
                            is_active BOOLEAN DEFAULT TRUE,
                            sort_order INT DEFAULT 0,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            created_by BIGINT,
                            FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                            FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
                            UNIQUE KEY unique_category_company (name, company_id),
                            INDEX idx_category_company (company_id),
                            INDEX idx_category_parent (parent_id)
);

-- Ticket table
CREATE TABLE tickets(
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        ticket_number VARCHAR(20) NOT NULL UNIQUE,
                        company_id BIGINT NOT NULL,
                        customer_id BIGINT NOT NULL,
                        agent_id BIGINT NULL,
                        category_id BIGINT NULL,
                        title VARCHAR(255) NOT NULL,
                        description TEXT NOT NULL,
                        priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
                        status ENUM('OPEN', 'IN_PROGRESS', 'PENDING_CUSTOMER', 'RESOLVED', 'CLOSED', 'CANCELLED') DEFAULT 'OPEN',
                        source ENUM('WEB', 'EMAIL', 'PHONE', 'CHAT') DEFAULT 'WEB',
                        resolution TEXT NULL,
                        tags JSON,
                        attachment JSON,
                        estimated_resolution_time TIMESTAMP NULL,
                        actual_resolution_time TIMESTAMP NULL,
                        first_response_time TIMESTAMP NULL,
                        last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        closed_at TIMESTAMP NULL,
                        FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
                        FOREIGN KEY (agent_id) REFERENCES agents(id) ON DELETE SET NULL,
                        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                        INDEX idx_ticket_company (company_id),
                        INDEX idx_ticket_customer (customer_id),
                        INDEX idx_ticket_agent (agent_id),
                        INDEX idx_ticket_status (status),
                        INDEX idx_ticket_priority (priority),
                        INDEX idx_ticket_number (ticket_number),
                        INDEX idx_ticket_created (created_at)
);

-- Ticket response table
CREATE TABLE ticket_responses (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  ticket_id BIGINT NOT NULL,
                                  user_id BIGINT NOT NULL,
                                  response_type ENUM('CUSTOMER_REPLY', 'AGENT_REPLY', 'INTERNAL_NOTE', 'SYSTEM_UPDATE') DEFAULT 'AGENT_REPLY',
                                  message TEXT NOT NULL,
                                  attachments JSON,
                                  is_public BOOLEAN DEFAULT TRUE,
                                  response_time DECIMAL(8,2),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  INDEX idx_response_ticket (ticket_id),
                                  INDEX idx_response_user (user_id),
                                  INDEX idx_response_type (response_type),
                                  INDEX idx_response_created (created_at)
);

-- Reviews Table
CREATE TABLE reviews (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         ticket_id BIGINT NOT NULL,
                         customer_id BIGINT NOT NULL,
                         agent_id BIGINT,
                         company_id BIGINT NOT NULL,
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         feedback TEXT,
                         service_quality_rating INT CHECK (service_quality_rating >= 1 AND service_quality_rating <= 5),
                         response_time_rating INT CHECK (response_time_rating >= 1 AND response_time_rating <= 5),
                         professionalism_rating INT CHECK (professionalism_rating >= 1 AND professionalism_rating <= 5),
                         would_recommend BOOLEAN DEFAULT TRUE,
                         additional_comments TEXT,
                         is_published BOOLEAN DEFAULT FALSE,
                         is_featured BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
                         FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
                         FOREIGN KEY (agent_id) REFERENCES agents(id) ON DELETE SET NULL,
                         FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                         UNIQUE KEY unique_review_ticket (ticket_id),
                         INDEX idx_review_company (company_id),
                         INDEX idx_review_customer (customer_id),
                         INDEX idx_review_agent (agent_id),
                         INDEX idx_review_rating (rating),
                         INDEX idx_review_created (created_at)
);

-- System feedback table
CREATE TABLE system_feedback (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 company_id BIGINT NOT NULL,
                                 admin_id BIGINT NOT NULL,
                                 title VARCHAR(150) NOT NULL,
                                 feedback TEXT,
                                 rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                 status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                                 FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
                                 INDEX idx_system_feedback_company (company_id),
                                 INDEX idx_system_feedback_status (status),
                                 INDEX idx_system_feedback_admin (admin_id)
);

-- FAQ table
CREATE TABLE faqs(
                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                     company_id BIGINT NOT NULL,
                     category_id BIGINT NULL,
                     question TEXT NOT NULL,
                     answer TEXT NOT NULL,
                     keywords TEXT,
                     view_count INT DEFAULT 0,
                     helpful_count INT DEFAULT 0,
                     not_helpful_count INT DEFAULT 0,
                     is_featured BOOLEAN DEFAULT FALSE,
                     is_published BOOLEAN DEFAULT TRUE,
                     sort_order INT DEFAULT 0,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                     created_by BIGINT,
                     updated_by BIGINT,
                     FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                     FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                     FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
                     FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
                     INDEX idx_faq_company (company_id),
                     INDEX idx_faq_category (category_id),
                     INDEX idx_faq_published (is_published),
                     INDEX idx_faq_featured (is_featured),
                     FULLTEXT INDEX idx_faq_content (question, answer, keywords)
);

-- Analytics table
CREATE TABLE analytics (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           company_id BIGINT NOT NULL,
                           metric_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
                           metric_name VARCHAR(100) NOT NULL,
                           metric_value DECIMAL(15,2) NOT NULL,
                           additional_data JSON,
                           period_start DATE NOT NULL,
                           period_end DATE NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                           UNIQUE KEY unique_metric_period (company_id, metric_type, metric_name, period_start),
                           INDEX idx_analytics_company (company_id),
                           INDEX idx_analytics_type (metric_type),
                           INDEX idx_analytics_period (period_start, period_end)
);

-- Audit logs table
CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            company_id BIGINT,
                            user_id BIGINT,
                            action VARCHAR(100) NOT NULL,
                            resource_type VARCHAR(50) NOT NULL,
                            resource_id BIGINT,
                            old_values JSON,
                            new_values JSON,
                            ip_address VARCHAR(45),
                            user_agent TEXT,
                            session_id VARCHAR(255),
                            request_url VARCHAR(500),
                            http_method VARCHAR(10),
                            severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'LOW',
                            details TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                            INDEX idx_audit_company (company_id),
                            INDEX idx_audit_user (user_id),
                            INDEX idx_audit_action (action),
                            INDEX idx_audit_resource (resource_type, resource_id),
                            INDEX idx_audit_created (created_at)
);

-- Sessions table
CREATE TABLE user_sessions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               session_token VARCHAR(255) NOT NULL UNIQUE,
                               ip_address VARCHAR(45),
                               user_agent TEXT,
                               is_active BOOLEAN DEFAULT TRUE,
                               expires_at TIMESTAMP NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               INDEX idx_session_token (session_token),
                               INDEX idx_session_user (user_id),
                               INDEX idx_session_expires (expires_at)
);

-- FAQ Voting table
CREATE TABLE faq_voting (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           faq_id BIGINT NOT NULL,
                           user_id BIGINT NULL,
                           session_id VARCHAR(255),
                           is_helpful BOOLEAN NOT NULL,
                           feedback TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           ip_address VARCHAR(45),
                           FOREIGN KEY (faq_id) REFERENCES faqs(id) ON DELETE CASCADE,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                           INDEX idx_faq_voting_faq (faq_id),
                           INDEX idx_faq_voting_user (user_id),
                           INDEX idx_faq_voting_session (session_id)
);

-- Notifications table
CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               company_id BIGINT NOT NULL,
                               type ENUM('TICKET_ASSIGNED', 'TICKET_UPDATED', 'TICKET_RESOLVED', 'REVIEW_RECEIVED', 'SYSTEM_ALERT') NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               related_resource_type VARCHAR(50),
                               related_resource_id BIGINT,
                               is_read BOOLEAN DEFAULT FALSE,
                               is_email_sent BOOLEAN DEFAULT FALSE,
                               priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
                               expires_at TIMESTAMP NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               read_at TIMESTAMP NULL,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                               INDEX idx_notification_user (user_id),
                               INDEX idx_notification_company (company_id),
                               INDEX idx_notification_read (is_read),
                               INDEX idx_notification_type (type),
                               INDEX idx_notification_created (created_at)
);

DELIMITER //

-- Trigger to update customer satisfaction score when review is added
CREATE TRIGGER update_customer_satisfaction
    AFTER INSERT ON reviews
    FOR EACH ROW
BEGIN
    UPDATE customers
    SET satisfaction_score = (
        SELECT AVG(rating)
        FROM reviews
        WHERE customer_id = NEW.customer_id
    )
    WHERE id = NEW.customer_id;
END//

-- Trigger to update agent performance
CREATE TRIGGER update_agent_performance
    AFTER UPDATE ON tickets
    FOR EACH ROW
BEGIN
    IF NEW.status = 'RESOLVED' AND OLD.status != 'RESOLVED' THEN
    UPDATE agents
    SET total_tickets_handled = total_tickets_handled + 1,
        current_ticket_count = current_ticket_count - 1
    WHERE id = NEW.agent_id;
END IF;

IF NEW.agent_id IS NOT NULL AND OLD.agent_id IS NULL THEN
UPDATE agents
SET current_ticket_count = current_ticket_count + 1
WHERE id = NEW.agent_id;
END IF;
END//

-- Trigger to update ticket count for customers
CREATE TRIGGER update_customer_ticket_count
    AFTER INSERT ON tickets
    FOR EACH ROW
BEGIN
    UPDATE customers
    SET total_tickets = total_tickets + 1
    WHERE id = NEW.customer_id;
END//

DELIMITER ;

-- Sample company for system
INSERT INTO companies (name, email, phone, address, website, status, subscription_plans, max_agent, max_customers)
VALUES ('System Company', 'system@company.com', '1234567890', 'System Address', 'https://system.com', 'ACTIVE', 'ENTERPRISE', 100, 10000);

-- Sample super admin (with hashed password)
INSERT INTO users (company_id, role_id, email, password, first_name, last_name, status, email_verified)
VALUES (1, 1, 'superadmin@system.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Super', 'Admin', 'ACTIVE', TRUE);