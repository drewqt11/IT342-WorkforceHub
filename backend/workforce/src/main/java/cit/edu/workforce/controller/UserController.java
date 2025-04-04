package cit.edu.workforce.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import cit.edu.workforce.service.UserService;
import cit.edu.workforce.entity.UserAccount;
import cit.edu.workforce.entity.Employee;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        String givenName = oidcUser.getGivenName();
        NameAndId nameAndId = extractNameAndId(givenName);

        UserAccount userAccount = userService.createOrUpdateUser(
            nameAndId.id,
            nameAndId.name,
            oidcUser.getFamilyName(),
            oidcUser.getEmail()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("user_account", createUserAccountMap(userAccount));
        response.put("employee", createEmployeeMap(userAccount.getEmployee()));
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createUserAccountMap(UserAccount userAccount) {
        Map<String, Object> userAccountMap = new HashMap<>();
        userAccountMap.put("user_id", userAccount.getUserId());
        userAccountMap.put("email_address", userAccount.getEmailAddress());
        userAccountMap.put("created_at", userAccount.getCreatedAt());
        userAccountMap.put("last_login", userAccount.getLastLogin());
        userAccountMap.put("is_active", userAccount.isActive());
        return userAccountMap;
    }

    private Map<String, Object> createEmployeeMap(Employee employee) {
        Map<String, Object> employeeMap = new HashMap<>();
        if (employee != null) {
            employeeMap.put("employee_id", employee.getEmployeeId());
            employeeMap.put("id_number", employee.getIdNumber());
            employeeMap.put("first_name", employee.getFirstName());
            employeeMap.put("last_name", employee.getLastName());
            employeeMap.put("email", employee.getEmail());
            // Other fields are null by default
            employeeMap.put("middle_name", employee.getMiddleName());
            employeeMap.put("gender", employee.getGender());
            employeeMap.put("contact_number", employee.getContactNumber());
            employeeMap.put("address", employee.getAddress());
            employeeMap.put("date_of_birth", employee.getDateOfBirth());
            employeeMap.put("hire_date", employee.getHireDate());
            employeeMap.put("civil_status", employee.getCivilStatus());
            employeeMap.put("nationality", employee.getNationality());
            employeeMap.put("profile_photo_url", employee.getProfilePhotoUrl());
            employeeMap.put("employment_urls", employee.getEmploymentUrls());
        }
        return employeeMap;
    }

    private static class NameAndId {
        String name;
        String id;
        
        NameAndId(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    private NameAndId extractNameAndId(String input) {
        if (input == null) return new NameAndId(null, null);

        StringBuilder idBuilder = new StringBuilder();
        StringBuilder nameBuilder = new StringBuilder();
        
        boolean lastWasDash = false;
        
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                idBuilder.append(c);
                lastWasDash = false;
            } else if (c == '-' && idBuilder.length() > 0 && !lastWasDash) {
                idBuilder.append(c);
                lastWasDash = true;
            } else if (Character.isLetter(c) || c == ' ') {
                nameBuilder.append(c);
                lastWasDash = false;
            }
        }
        
        String name = nameBuilder.toString().trim().replaceAll("\\s+", " ");
        String id = idBuilder.length() > 0 ? idBuilder.toString() : null;
        
        return new NameAndId(name, id);
    }
} 