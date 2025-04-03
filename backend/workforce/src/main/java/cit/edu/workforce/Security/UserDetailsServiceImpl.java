package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.Employee;
import cit.edu.workforce.Entity.UserAccount;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public UserDetailsServiceImpl(UserAccountRepository userAccountRepository, EmployeeRepository employeeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if user is active
        if (!userAccount.getIsActive()) {
            throw new UsernameNotFoundException("User account is inactive: " + email);
        }

        // Find the employee associated with this user account to get the role
        Employee employee = employeeRepository.findByUserAccountUserId(userAccount.getUserId())
                .orElse(null);

        // Default role for users without an employee record yet
        String role = "PENDING";
        
        if (employee != null && employee.getRole() != null) {
            role = employee.getRole().getRoleId();
        }

        return UserDetailsImpl.build(userAccount, role);
    }
} 