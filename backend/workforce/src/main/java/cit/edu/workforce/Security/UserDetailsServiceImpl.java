package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.EmployeeEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.EmployeeRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccountEntity userAccount = userAccountRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if user is active
        if (!userAccount.isActive()) {
            throw new UsernameNotFoundException("User account is not active");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Get the employee's role
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findByUserAccount(userAccount);
        if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            if (employee.getRole() != null) {
                // Add the role as an authority
                authorities.add(new SimpleGrantedAuthority(employee.getRole().getRoleId()));
            }
        } else {
            // If no employee record is found, assign a minimal role or handle accordingly
            authorities.add(new SimpleGrantedAuthority("ROLE_INCOMPLETE"));
        }

        return new User(userAccount.getEmailAddress(), userAccount.getPassword(), authorities);
    }
}