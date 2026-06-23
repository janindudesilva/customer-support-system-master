package com.customersupport.repository;

import com.customersupport.entity.SystemFeedback;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemFeedbackRepository extends JpaRepository<SystemFeedback, Long> {

  List<SystemFeedback> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

  List<SystemFeedback> findByCompanyIdAndStatusOrderByCreatedAtDesc(
      Long companyId, SystemFeedback.Status status);

  List<SystemFeedback> findByStatusOrderByCreatedAtDesc(SystemFeedback.Status status);

  Page<SystemFeedback> findByStatus(SystemFeedback.Status status, Pageable pageable);
}
