package com.customersupport.service;

import com.customersupport.dto.CustomerDTO;
import com.customersupport.dto.CustomerRegistrationDTO;
import com.customersupport.entity.Company;
import com.customersupport.entity.Customer;
import com.customersupport.entity.Role;
import com.customersupport.entity.User;
import com.customersupport.repository.CompanyRepository;
import com.customersupport.repository.CustomerRepository;
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
public class CustomerService {

  @Autowired private CustomerRepository customerRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  // Get all customers
  public List<CustomerDTO> getAllCustomers() {
    return customerRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Get customers by company
  public List<CustomerDTO> getCustomersByCompany(Long companyId) {
    return customerRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Get customer by ID
  public Optional<CustomerDTO> getCustomerById(Long id) {
    return customerRepository.findById(id).map(this::convertToDTO);
  }

  // Get customer by user ID
  public Optional<CustomerDTO> getCustomerByUserId(Long userId) {
    return customerRepository.findByUserId(userId).map(this::convertToDTO);
  }

  // Search customers by company
  public List<CustomerDTO> searchCustomersByCompany(Long companyId, String keyword) {
    return customerRepository.searchCustomerByCompany(companyId, keyword).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  // Register customer
  public CustomerDTO registerCustomer(CustomerRegistrationDTO registrationDTO) {
    Company company =
        companyRepository
            .findById(registrationDTO.getId())
            .orElseThrow(() -> new RuntimeException("Company not found"));

    // Check if email already exists within this company (proper constraint)
    if (userRepository.existsByEmailAndCompanyId(registrationDTO.getEmail(), company.getId())) {
      throw new RuntimeException("Email already in use within this company");
    }

    // Check if company has reached max customers
    Integer currentCustomerCount = customerRepository.countByCompanyId(company.getId());
    if (currentCustomerCount >= company.getMaxCustomers()) {
      throw new RuntimeException("Company has reached maximum customer limit");
    }

    Role customerRole =
        roleRepository
            .findByName("CUSTOMER")
            .orElseThrow(() -> new RuntimeException("Customer role not found"));

    // Create user
    User user = new User();
    user.setCompany(company);
    user.setRole(customerRole);
    user.setEmail(registrationDTO.getEmail());
    user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
    user.setFirstname(registrationDTO.getFirstName());
    user.setLastname(registrationDTO.getLastName());
    user.setPhone(registrationDTO.getPhone());
    user.setStatus(User.UserStatus.ACTIVE);

    User savedUser = userRepository.save(user);

    // Create customer
    Customer customer = new Customer();
    customer.setUser(savedUser);
    customer.setCompany(company);
    customer.setCustomerType(registrationDTO.getCustomerType());
    customer.setDateOfBirth(registrationDTO.getDateOfBirth());
    customer.setAddress(registrationDTO.getAddress());
    customer.setPreferredContactMethod(registrationDTO.getPreferredContactMethod());
    customer.setTimezone(registrationDTO.getTimezone());
    customer.setLanguagePreference(registrationDTO.getLanguagePreference());

    Customer savedCustomer = customerRepository.save(customer);

    return convertToDTO(savedCustomer);
  }

  // Update customer
  public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    User user = customer.getUser();

    // Check if email is changed and if it's already in use
    if (!user.getEmail().equals(customerDTO.getEmail())
        && userRepository.existsByEmail(customerDTO.getEmail())) {
      throw new RuntimeException("Email already in use");
    }

    // Update user
    user.setEmail(customerDTO.getEmail());
    user.setFirstname(customerDTO.getFirstName());
    user.setLastname(customerDTO.getLastName());
    user.setPhone(customerDTO.getPhone());
    userRepository.save(user);

    // Update customer
    customer.setCustomerType(customerDTO.getCustomerType());
    customer.setAddress(customerDTO.getAddress());
    customer.setPreferredContactMethod(customerDTO.getPreferredContactMethod());
    customer.setTimezone(customerDTO.getTimeZone());
    customer.setLanguagePreference(customerDTO.getLanguagePreference());

    Customer updatedCustomer = customerRepository.save(customer);
    return convertToDTO(updatedCustomer);
  }

  // Update customer profile
  public CustomerDTO updateCustomerProfile(Long id, CustomerDTO customerDTO) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    User user = customer.getUser();

    user.setFirstname(customerDTO.getFirstName());
    user.setLastname(customerDTO.getLastName());
    user.setPhone(customerDTO.getPhone());
    userRepository.save(user);

    customer.setAddress(customerDTO.getAddress());
    customer.setPreferredContactMethod(customerDTO.getPreferredContactMethod());
    customer.setTimezone(customerDTO.getTimeZone());
    customer.setLanguagePreference(customerDTO.getLanguagePreference());

    Customer updatedCustomer = customerRepository.save(customer);
    return convertToDTO(updatedCustomer);
  }

  // Delete customer
  public void deleteCustomer(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    userRepository.delete(customer.getUser());
  }

  // Verify email
  public void verifyEmail(String token) {
    throw new RuntimeException("Email verification not implemented");
  }

  // Helper method to convert entity to DTO
  private CustomerDTO convertToDTO(Customer customer) {
    return CustomerDTO.fromEntity(customer);
  }
}
