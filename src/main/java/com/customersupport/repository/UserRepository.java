package com.customersupport.repository;

import com.customersupport.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  List<User> findByCompanyId(Long companyId);

  List<User> findByCompanyIdAndRoleName(Long companyId, String roleName);

  List<User> findByStatus(User.UserStatus status);

  @Query(
      "SELECT u FROM User u WHERE u.company.id = :companyId AND u.role.name = :roleName AND"
          + " u.status = :status")
  List<User> findByCompanyIdAndRoleNameAndStatus(
      @Param("companyId") Long companyId,
      @Param("roleName") String roleName,
      @Param("status") User.UserStatus status);

  @Query(
      "SELECT u FROM User u WHERE u.company.id = :companyId AND (u.firstname LIKE %:keyword% OR"
          + " u.lastname LIKE %:keyword% OR u.email LIKE %:keyword%)")
  List<User> searchUserByCompany(
      @Param("companyId") Long companyId, @Param("keyword") String keyword);

  @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.role.name = :roleName")
  Integer countByCompanyIdAndRoleName(
      @Param("companyId") Long companyId, @Param("roleName") String roleName);

  boolean existsByEmail(String email);

  boolean existsByEmailAndCompanyId(String email, Long companyId);

  Optional<User> findByPasswordResetToken(String token);

  @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
  List<User> findAllOrderByCreatedAtDesc();

  Long countByCompanyId(Long companyId);
}
