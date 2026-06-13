package com.factory.management.service;

import com.factory.management.entity.Employee;
import com.factory.management.entity.Role;
import com.factory.management.entity.User;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.RoleRepository;
import com.factory.management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }

    public Employee save(Employee employee) {
        if (employee.getPosition() != null) {
            employee.setPosition(employee.getPosition().trim());
        }
        // Null out factory if it's empty to avoid JPA errors
        if (employee.getFactory() != null && employee.getFactory().getId() == null) {
            employee.setFactory(null);
        }
        handleUserCredentials(employee);
        return employeeRepository.save(employee);
    }

    public Employee update(Long id, Employee updated) {
        Employee existing = findById(id);
        existing.setName(updated.getName());
        existing.setNic(updated.getNic());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setPosition(updated.getPosition() != null ? updated.getPosition().trim() : null);
        existing.setDepartment(updated.getDepartment());
        existing.setSalary(updated.getSalary());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        
        // Handle cases where the factory might be selected but empty
        if (updated.getFactory() != null && updated.getFactory().getId() == null) {
            existing.setFactory(null);
        } else {
            existing.setFactory(updated.getFactory());
        }
        
        existing.setUsername(updated.getUsername());
        existing.setPassword(updated.getPassword());
        
        handleUserCredentials(existing);
        
        return employeeRepository.save(existing);
    }

    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> search(String query) {
        if (query == null || query.trim().isEmpty()) return findAll();
        return employeeRepository.searchEmployees(query.trim());
    }

    public long countActive() {
        return employeeRepository.countByStatus("ACTIVE");
    }

    public long count() {
        return employeeRepository.count();
    }

    private void handleUserCredentials(Employee employee) {
        String pos = employee.getPosition() != null ? employee.getPosition().trim().toLowerCase() : "";
        // If this employee is a cashier, we need to create or update their system login
        boolean isCashier = pos.equals("cashier") || pos.equals("chashier") || pos.contains("cashier");
        
        if (isCashier) {
            String username = employee.getUsername();
            String password = employee.getPassword();
            
            if (employee.getUser() == null) {
                // Create a new login if it doesn't exist
                if (username == null || username.trim().isEmpty()) {
                    throw new RuntimeException("Username is required for Cashier");
                }
                if (password == null || password.trim().isEmpty()) {
                    throw new RuntimeException("Password is required for Cashier");
                }
                if (userRepository.existsByUsername(username.trim())) {
                    throw new RuntimeException("Username '" + username + "' is already taken");
                }
                
                User user = new User();
                user.setUsername(username.trim());
                user.setPassword(passwordEncoder.encode(password));
                user.setFullName(employee.getName());
                user.setEmail(employee.getEmail());
                user.setEnabled("ACTIVE".equalsIgnoreCase(employee.getStatus()));
                
                Role role = roleRepository.findByName("ROLE_CASHIER")
                        .orElseGet(() -> roleRepository.save(new Role("ROLE_CASHIER")));
                user.setRole(role);
                
                employee.setUser(user);
            } else {
                // Just update the existing login information
                User user = employee.getUser();
                if (username != null && !username.trim().isEmpty() && !username.equals(user.getUsername())) {
                    if (userRepository.existsByUsername(username.trim())) {
                        throw new RuntimeException("Username '" + username + "' is already taken");
                    }
                    user.setUsername(username.trim());
                }
                if (password != null && !password.trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password));
                }
                user.setFullName(employee.getName());
                user.setEmail(employee.getEmail());
                user.setEnabled("ACTIVE".equalsIgnoreCase(employee.getStatus()));
                
                Role role = roleRepository.findByName("ROLE_CASHIER")
                        .orElseGet(() -> roleRepository.save(new Role("ROLE_CASHIER")));
                user.setRole(role);
            }
        } else {
            // Remove the login if the person is no longer a cashier
            if (employee.getUser() != null) {
                User userToDelete = employee.getUser();
                employee.setUser(null);
                userRepository.delete(userToDelete);
            }
        }
    }
}
