package cit.edu.workforce.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cit.edu.workforce.entity.Employee;
import cit.edu.workforce.entity.UserAccount;
import cit.edu.workforce.repository.EmployeeRepository;
import cit.edu.workforce.repository.UserAccountRepository;

@Service
public class UserService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public UserAccount createOrUpdateUser(String idNumber, String givenName, String lastName, String emailAddress) {
        Optional<UserAccount> existingUser = userAccountRepository.findByEmailAddress(emailAddress);
        UserAccount userAccount;

        if (existingUser.isPresent()) {
            userAccount = existingUser.get();
            userAccount.setLastLogin(LocalDateTime.now());

            // Update employee information
            Employee employee = userAccount.getEmployee();
            if (employee != null) {
                updateEmployeeInfo(employee, idNumber, givenName, lastName);
            } else {
                // Create new employee for existing user
                employee = new Employee();
                employee.setEmployeeId(userAccount.getUserId());
                employee.setUserAccount(userAccount);
                updateEmployeeInfo(employee, idNumber, givenName, lastName);
                employee = employeeRepository.save(employee);
                userAccount.setEmployee(employee);
            }
        } else {
            // Create new user account first
            userAccount = new UserAccount();
            userAccount.setEmailAddress(emailAddress);
            userAccount = userAccountRepository.save(userAccount);

            // Create new employee
            Employee employee = new Employee();
            employee.setEmployeeId(userAccount.getUserId());
            employee.setUserAccount(userAccount);
            updateEmployeeInfo(employee, idNumber, givenName, lastName);
            employee = employeeRepository.save(employee);
            userAccount.setEmployee(employee);
        }

        return userAccountRepository.save(userAccount);
    }

    private void updateEmployeeInfo(Employee employee, String idNumber, String givenName, String lastName) {
        employee.setIdNumber(idNumber);
        employee.setFirstName(givenName);
        employee.setLastName(lastName);
    }
}
