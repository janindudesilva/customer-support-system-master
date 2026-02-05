package com.customersupport.repository;

import com.customersupport.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);

  Optional<Role> findById(Long id);

  boolean existsByName(String name);
}
