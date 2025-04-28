package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/hr/user-accounts")
@Tag(name = "User Account Management", description = "User account management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Autowired
    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get user account by email", description = "Get user account details by email address")
    @PreAuthorize("hasRole('ROLE_HR')")
    public ResponseEntity<UserAccountEntity> getUserAccountByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userAccountService.getUserAccountByEmail(email));
    }

    @GetMapping("/{email}/last-login")
    @Operation(summary = "Get last login time", description = "Get the last login time for a user account")
    @PreAuthorize("hasRole('ROLE_HR')")
    public ResponseEntity<LocalDateTime> getLastLogin(@PathVariable String email) {
        UserAccountEntity userAccount = userAccountService.getUserAccountByEmail(email);
        return ResponseEntity.ok(userAccount.getLastLogin());
    }

    @GetMapping("/{email}/active-status")
    @Operation(summary = "Get active status", description = "Get the active status for a user account")
    @PreAuthorize("hasRole('ROLE_HR')")
    public ResponseEntity<Boolean> getActiveStatus(@PathVariable String email) {
        UserAccountEntity userAccount = userAccountService.getUserAccountByEmail(email);
        return ResponseEntity.ok(userAccount.isActive());
    }

    @PutMapping("/{email}/account/deactivate")
    @Operation(summary = "Deactivate user account", description = "Deactivate a user account")
    @PreAuthorize("hasRole('ROLE_HR')")
    public ResponseEntity<Boolean> deactivateUserAccount(@PathVariable String email) {
        try {
            UserAccountEntity userAccount = userAccountService.getUserAccountByEmail(email);
            userAccountService.deactivateUser(userAccount);
            return ResponseEntity.ok(false);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User account not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            }
            throw e;
        }
    }

    @PutMapping("/{email}/account/activate")
    @Operation(summary = "Activate user account", description = "Activate a user account")
    @PreAuthorize("hasRole('ROLE_HR')")
    public ResponseEntity<Boolean> activateUserAccount(@PathVariable String email) {
        UserAccountEntity userAccount = userAccountService.getUserAccountByEmail(email);
        
        if (userAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(false);
        }

        userAccountService.activateUser(userAccount);
        return ResponseEntity.ok(true);
    }

} 