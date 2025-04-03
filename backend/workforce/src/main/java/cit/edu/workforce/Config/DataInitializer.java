package cit.edu.workforce.Config;

import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Entity.EmailDomainList;
import cit.edu.workforce.Entity.Role;
import cit.edu.workforce.Entity.UserAccount;
import cit.edu.workforce.Repository.EmailDomainListRepository;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.RoleRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private EmailDomainListRepository emailDomainListRepository;
    
    @Autowired
    private UserAccountRepository userAccountRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Initialize roles if they don't exist
            if (roleRepository.count() == 0) {
                roleRepository.save(new Role("SYSTEM_ADMIN", "System Administrator"));
                roleRepository.save(new Role("HR_ADMIN", "HR Administrator"));
                roleRepository.save(new Role("EMPLOYEE", "Regular Employee"));
                roleRepository.save(new Role("PENDING", "Pending User"));
                System.out.println("Initialized default roles");
            }
            
            // Initialize email domains if they don't exist
            if (emailDomainListRepository.count() == 0) {
                EmailDomainList domain = new EmailDomainList();
                domain.setDomainName("workforcehub.com");
                domain.setIsActive(true);
                domain.setAddedAt(LocalDateTime.now());
                emailDomainListRepository.save(domain);
                
                EmailDomainList domain2 = new EmailDomainList();
                domain2.setDomainName("example.com");
                domain2.setIsActive(true);
                domain2.setAddedAt(LocalDateTime.now());
                emailDomainListRepository.save(domain2);
                
                System.out.println("Initialized default email domains");
            }
            
            // Initialize system admin user if no users exist
            if (userAccountRepository.count() == 0) {
                // Create user account for system admin
                UserAccount adminAccount = new UserAccount();
                adminAccount.setEmailAddress("admin@workforcehub.com");
                adminAccount.setPassword(passwordEncoder.encode("admin123")); // In production, use a secure password
                adminAccount.setIsActive(true);
                adminAccount.setCreatedAt(LocalDateTime.now());
                userAccountRepository.save(adminAccount);
                
                // Create employee record for system admin
                Employee adminEmployee = new Employee();
                adminEmployee.setFirstName("System");
                adminEmployee.setLastName("Administrator");
                adminEmployee.setEmail("admin@workforcehub.com");
                adminEmployee.setHireDate(LocalDate.now());
                adminEmployee.setStatus("ACTIVE");
                adminEmployee.setUserAccount(adminAccount);
                
                // Assign system admin role
                Role adminRole = roleRepository.findById("SYSTEM_ADMIN")
                        .orElseThrow(() -> new RuntimeException("System admin role not found"));
                adminEmployee.setRole(adminRole);
                
                employeeRepository.save(adminEmployee);
                
                System.out.println("Initialized system admin user");
            }
        };
    }
} 