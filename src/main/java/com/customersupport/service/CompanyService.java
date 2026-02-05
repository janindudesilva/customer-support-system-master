package com.customersupport.service;

import com.customersupport.dto.CompanyAdminRegistrationDTO;
import com.customersupport.dto.CompanyDTO;
import com.customersupport.dto.CompanyRegistrationDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CompanyService {
  @Autowired private CompanyRepository companyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EmailService emailService;

  // Get all companies
  public List<CompanyDTO> getAllCompanies() {
    return companyRepository.findAllOrderByCreatedAtDesc().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Get company by ID
  public Optional<CompanyDTO> getCompanyById(Long id) {
    return companyRepository.findById(id).map(this::convertToDTO);
  }

  // Get companies by status
  public List<CompanyDTO> getCompaniesByStatus(Company.CompanyStatus status) {
    return companyRepository.findByStatus(status).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Search companies
  public List<CompanyDTO> searchCompanies(String keyword) {
    return companyRepository.searchCompanies(keyword).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  public CompanyDTO registerCompany(CompanyRegistrationDTO registrationDTO) {
    // Check if company email or name already exists
    if (companyRepository.existsByEmail(registrationDTO.getEmail())) {
      throw new RuntimeException("Company with this email already exists");
    }
    if (companyRepository.existsByName(registrationDTO.getName())) {
      throw new RuntimeException("Company with this name already exists");
    }
    if (userRepository.existsByEmail(registrationDTO.getAdminEmail())) {
      throw new RuntimeException("User with this email already exists");
    }

    // Create new company
    Company company = new Company();
    company.setName(registrationDTO.getName());
    company.setEmail(registrationDTO.getEmail());
    company.setPhone(registrationDTO.getPhone());
    company.setAddress(registrationDTO.getAddress());
    company.setWebsite(registrationDTO.getWebsite());
    company.setSubscriptionPlan(registrationDTO.getSubscriptionPlan());
    company.setMaxAgents(registrationDTO.getMaxAgents());
    company.setMaxCustomers(registrationDTO.getMaxCustomers());
    company.setStatus(Company.CompanyStatus.ACTIVE);

    Company savedCompany = companyRepository.save(company);

    // Create company  admin user
    Role adminRole =
        roleRepository
            .findByName("COMPANY_ADMIN")
            .orElseThrow(() -> new RuntimeException("Admin role not found"));

    User adminUser = new User();
    adminUser.setCompany(savedCompany);
    adminUser.setRole(adminRole);
    adminUser.setEmail(registrationDTO.getAdminEmail());
    adminUser.setPassword(passwordEncoder.encode(registrationDTO.getAdminPassword()));
    adminUser.setFirstname(registrationDTO.getAdminFirstName());
    adminUser.setLastname(registrationDTO.getAdminLastName());
    adminUser.setPhone(registrationDTO.getAdminPhone());
    adminUser.setStatus(User.UserStatus.ACTIVE);
    adminUser.setEmailVerified(true);

    userRepository.save(adminUser);

    // Create a DTO to pass to the email service
    CompanyAdminRegistrationDTO adminInfoDTO = new CompanyAdminRegistrationDTO();
    adminInfoDTO.setCompanyId(savedCompany.getId());
    adminInfoDTO.setEmail(registrationDTO.getAdminEmail());
    adminInfoDTO.setFirstName(registrationDTO.getAdminFirstName());
    adminInfoDTO.setLastName(registrationDTO.getAdminLastName());
    adminInfoDTO.setPhone(registrationDTO.getAdminPhone());

    // Send email notification to the company admin
    emailService.sendCompanyAdminRegistrationEmail(
        adminInfoDTO, savedCompany, registrationDTO.getAdminPassword());

    return convertToDTO(savedCompany);
  }

  // Update companies
  public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO) {
    Company company =
        companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));

    // Check if email is being changed and if it's unique
    if (!company.getEmail().equals(companyDTO.getEmail())
        && companyRepository.existsByEmail(companyDTO.getEmail())) {
      throw new RuntimeException("Company with this email already exists");
    }

    // Check id name is being changed and if it's unique
    if (!company.getName().equals(companyDTO.getName())
        && companyRepository.existsByName(companyDTO.getName())) {
      throw new RuntimeException("Company with this name already exists");
    }

    // Update fields
    company.setName(companyDTO.getName());
    company.setEmail(companyDTO.getEmail());
    company.setPhone(companyDTO.getPhone());
    company.setAddress(companyDTO.getAddress());
    company.setWebsite(companyDTO.getWebsite());
    company.setStatus(companyDTO.getStatus());
    company.setSubscriptionPlan(companyDTO.getSubscriptionPlan());
    company.setMaxAgents(companyDTO.getMaxAgents());
    company.setMaxCustomers(companyDTO.getMaxCustomers());

    Company savedCompany = companyRepository.save(company);
    return convertToDTO(savedCompany);
  }

  // Deactivate company
  public void deactivateCompany(Long id) {
    Company company =
        companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));

    company.setStatus(Company.CompanyStatus.INACTIVE);
    companyRepository.save(company);

    // Deactivate all users in the company
    List<User> companyUsers = userRepository.findByCompanyId(id);
    companyUsers.forEach(
        user -> {
          user.setStatus(User.UserStatus.INACTIVE);
          userRepository.save(user);
        });
  }

  public void activateCompany(Long id) {
    Company company =
        companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));

    company.setStatus(Company.CompanyStatus.ACTIVE);
    companyRepository.save(company);

    // Activate all users in the company
    List<User> companyUsers = userRepository.findByCompanyId(id);
    companyUsers.forEach(
        user -> {
          user.setStatus(User.UserStatus.ACTIVE);
          userRepository.save(user);
        });
  }

  // Delete company
  public void deleteCompany(Long id) {
    Company company =
        companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));
    companyRepository.delete(company);
  }

  // Create company admin
  public void createCompanyAdmin(CompanyAdminRegistrationDTO adminDTO) {
    // Check if company exists
    Company company =
        companyRepository
            .findById(adminDTO.getCompanyId())
            .orElseThrow(() -> new RuntimeException("Company not found"));

    // Check if email already exists within this company (proper constraint)
    if (userRepository.existsByEmailAndCompanyId(adminDTO.getEmail(), adminDTO.getCompanyId())) {
      throw new RuntimeException("User with this email already exists in this company");
    }

    // Get company admin role
    Role adminRole =
        roleRepository
            .findByName("COMPANY_ADMIN")
            .orElseThrow(() -> new RuntimeException("Company admin role not found"));

    // Create company admin user
    User adminUser = new User();
    adminUser.setCompany(company);
    adminUser.setRole(adminRole);
    adminUser.setEmail(adminDTO.getEmail());
    adminUser.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
    adminUser.setFirstname(adminDTO.getFirstName());
    adminUser.setLastname(adminDTO.getLastName());
    adminUser.setPhone(adminDTO.getPhone());
    adminUser.setStatus(User.UserStatus.ACTIVE);
    adminUser.setEmailVerified(true);

    userRepository.save(adminUser);

    // Send email notification to the company admin
    emailService.sendCompanyAdminRegistrationEmail(adminDTO, company, adminDTO.getPassword());
  }

  private CompanyDTO convertToDTO(Company company) {
    CompanyDTO dto = CompanyDTO.fromEntity(company);

    // Get current counts
    Integer currentAgents = companyRepository.countAgentsByCompanyId(company.getId());
    Integer currentCustomers = companyRepository.countCustomersByCompanyId(company.getId());

    dto.setCurrentAgents(currentAgents);
    dto.setCurrentCustomers(currentCustomers);

    // Get company admins
    List<User> companyAdmins =
        userRepository.findByCompanyIdAndRoleName(company.getId(), "COMPANY_ADMIN");
    List<CompanyDTO.CompanyAdminInfo> adminInfos =
        companyAdmins.stream()
            .map(
                admin ->
                    new CompanyDTO.CompanyAdminInfo(
                        admin.getId(),
                        admin.getFirstname(),
                        admin.getLastname(),
                        admin.getEmail(),
                        admin.getPhone(),
                        admin.getCreatedAt()))
            .collect(Collectors.toList());
    dto.setCompanyAdmins(adminInfos);

    return dto;
  }
}
