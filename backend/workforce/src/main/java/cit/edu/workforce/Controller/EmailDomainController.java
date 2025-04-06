package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.EmailDomainListEntity;
import cit.edu.workforce.Service.EmailDomainListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/domains")
@Tag(name = "Email Domains", description = "Email Domain Management API")
public class EmailDomainController {

    private final EmailDomainListService emailDomainListService;

    @Autowired
    public EmailDomainController(EmailDomainListService emailDomainListService) {
        this.emailDomainListService = emailDomainListService;
    }

    @GetMapping
    @Operation(summary = "Get all domains", description = "Retrieves all configured email domains")
    public ResponseEntity<List<EmailDomainListEntity>> getAllDomains() {
        return ResponseEntity.ok(emailDomainListService.getAllDomains());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active domains", description = "Retrieves all active email domains")
    public ResponseEntity<List<EmailDomainListEntity>> getActiveDomains() {
        return ResponseEntity.ok(emailDomainListService.getActiveDomains());
    }

    @GetMapping("/check")
    @Operation(summary = "Check if domain is valid", description = "Checks if an email domain is valid")
    public ResponseEntity<Map<String, Boolean>> isValidDomain(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", emailDomainListService.isValidDomain(email));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Add domain", description = "Adds a new email domain to the whitelist")
    public ResponseEntity<EmailDomainListEntity> addDomain(@RequestParam String domainName) {
        return ResponseEntity.ok(emailDomainListService.addDomain(domainName));
    }
} 