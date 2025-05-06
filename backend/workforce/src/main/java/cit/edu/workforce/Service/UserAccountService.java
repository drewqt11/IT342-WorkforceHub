package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.RoleEntity;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleService roleService;

    @Autowired
    public UserAccountService(UserAccountRepository userAccountRepository, EmployeeRepository employeeRepository, RoleService roleService) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.roleService = roleService;
    }

    @Transactional
    public UserAccountEntity createUserAccount(String email) {
        if (userAccountRepository.existsByEmailAddress(email)) {
            throw new RuntimeException("Email address is already taken");
        }

        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.setEmailAddress(email);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setActive(false);

        return userAccountRepository.save(userAccount);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccountEntity> findByEmail(String email) {
        return userAccountRepository.findByEmailAddress(email);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccountEntity> findById(String userId) {
        return userAccountRepository.findById(userId);
    }

    @Transactional
    public UserAccountEntity updateLastLogin(UserAccountEntity userAccount) {
        userAccount.setLastLogin(LocalDateTime.now());
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public UserAccountEntity activateUser(UserAccountEntity userAccount) {
        userAccount.setActive(true);

        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            employee.setStatus(false);
            employee.setEmploymentStatus("PENDING");
            
            // Get the ROLE_EMPLOYEE role and set it
            RoleEntity role = roleService.getRoleById("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found"));
            employee.setRole(role);
            
            employeeRepository.save(employee);
        }
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public UserAccountEntity deactivateUser(UserAccountEntity userAccount) {
        userAccount.setActive(false);
        
        // Find and deactivate associated employee
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            employee.setStatus(false);
            employee.setEmploymentStatus("RESIGNED / TERMINATED");
            employee.setRole(null);
            employeeRepository.save(employee);
        }
        
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public void deleteUser(String userId) {
        userAccountRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailDomain(String email) {
        // Extract domain from email
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        // Check if the domain is @cit.edu as required
        return "cit.edu".equals(domain);
    }

    @Transactional(readOnly = true)
    public UserAccountEntity getUserAccountByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User account not found for email: " + email));
    }

    @Transactional(readOnly = true)
    public List<UserAccountEntity> getAllUserAccounts() {
        return userAccountRepository.findAll();
    }

    @Transactional
    public UserAccountEntity saveUserAccount(UserAccountEntity userAccount) {
        return userAccountRepository.save(userAccount);
    }
}