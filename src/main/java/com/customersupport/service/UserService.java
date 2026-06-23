package com.customersupport.service;

import com.customersupport.dto.UserDTO;
import com.customersupport.entity.Company;
import com.customersupport.entity.Role;
import com.customersupport.entity.User;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.RoleRepository;
import com.customersupport.repository.UserRepository;
import com.customersupport.util.PasswordEncoder;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EmailService emailService;

  // get all users
  public List<UserDTO> getAllUsers() {
    return userRepository.findAllOrderByCreatedAtDesc().stream()
        .map(UserDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // get all user by ID
  public Optional<UserDTO> getUserById(Long id) {
    return userRepository.findById(id).map(UserDTO::fromEntity);
  }

  // get user by company
  public List<UserDTO> getUsersByCompany(Long companyId) {
    return userRepository.findByCompanyId(companyId).stream()
        .map(UserDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // get users by company and role
  public List<UserDTO> getUsersByCompanyAndRole(Long companyId, String roleName) {
    return userRepository.findByCompanyIdAndRoleName(companyId, roleName).stream()
        .map(UserDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // search user by company
  public List<UserDTO> searchUsersByCompany(Long companyId, String keyword) {
    return userRepository.searchUserByCompany(companyId, keyword).stream()
        .map(UserDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // create user
  public UserDTO createUser(UserDTO userDTO, String password) {
    // check if email already exists
    if (userRepository.existsByEmail(userDTO.getEmail())) {
      throw new RuntimeException("Email already in use");
    }

    Company company = null;
    if (userDTO.getCompanyId() != null) {
      company =
          companyRepository
              .findById(userDTO.getCompanyId())
              .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    Role role =
        roleRepository
            .findById(userDTO.getRoleId())
            .orElseThrow(() -> new RuntimeException("Role not found"));

    User user = new User();
    user.setCompany(company);
    user.setRole(role);
    user.setEmail(userDTO.getEmail());
    user.setPassword(passwordEncoder.encode(password));
    user.setFirstname(userDTO.getFirstName());
    user.setLastname(userDTO.getLastName());
    user.setPhone(userDTO.getPhone());
    user.setStatus(userDTO.getStatus());
    user.setEmailVerified(userDTO.getEmailVerified());

    User savedUser = userRepository.save(user);
    return UserDTO.fromEntity(savedUser);
  }

  // Update user
  public UserDTO updateUser(Long id, UserDTO userDTO) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    // check if email is changed and if it's aleardy in use
    if (!user.getEmail().equals(userDTO.getEmail())
        && userRepository.existsByEmail(userDTO.getEmail())) {
      throw new RuntimeException("Email already in use");
    }

    Company company = null;
    if (userDTO.getCompanyId() != null) {
      company =
          companyRepository
              .findById(userDTO.getCompanyId())
              .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    Role role =
        roleRepository
            .findById(userDTO.getRoleId())
            .orElseThrow(() -> new RuntimeException("Role not found"));

    user.setCompany(company);
    user.setRole(role);
    user.setEmail(userDTO.getEmail());
    user.setFirstname(userDTO.getFirstName());
    user.setLastname(userDTO.getLastName());
    user.setPhone(userDTO.getPhone());
    user.setStatus(userDTO.getStatus());
    user.setEmailVerified(userDTO.getEmailVerified());

    User updatedUser = userRepository.save(user);
    return UserDTO.fromEntity(updatedUser);
  }

  // Activate user
  public void activateUser(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setStatus(User.UserStatus.ACTIVE);
    userRepository.save(user);
  }

  // Deactivate user
  public void deactivateUser(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setStatus(User.UserStatus.INACTIVE);
    userRepository.save(user);
  }

  // Delete user
  public void deleteUser(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    userRepository.delete(user);
  }

  // Reset password
  public String resetPassword(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    String newPassword = RandomStringUtils.randomAlphanumeric(10);
    user.setPassword(passwordEncoder.encode(newPassword));

    userRepository.save(user);

    // Send email with new password
    emailService.sendPasswordResetEmail(user, newPassword);

    return newPassword;
  }

  // Find by email
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  // change password
  public void changePassword(Long userId, String currentPassword, String newPassword) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new RuntimeException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }
}
