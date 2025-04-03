package cit.edu.workforce.Controller;

import cit.edu.workforce.Entity.EmailDomainList;
import cit.edu.workforce.Repository.EmailDomainListRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/system/email-domains")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class EmailDomainController {

    private final EmailDomainListRepository emailDomainListRepository;

    @Autowired
    public EmailDomainController(EmailDomainListRepository emailDomainListRepository) {
        this.emailDomainListRepository = emailDomainListRepository;
    }

    @GetMapping
    public ResponseEntity<List<EmailDomainList>> getAllEmailDomains() {
        List<EmailDomainList> emailDomains = emailDomainListRepository.findAll();
        return ResponseEntity.ok(emailDomains);
    }

    @GetMapping("/{domainId}")
    public ResponseEntity<EmailDomainList> getEmailDomainById(@PathVariable UUID domainId) {
        EmailDomainList emailDomain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email domain not found"));
        return ResponseEntity.ok(emailDomain);
    }

    @PostMapping
    public ResponseEntity<EmailDomainList> addEmailDomain(@Valid @RequestBody EmailDomainList emailDomain) {
        // Check if domain already exists
        if (emailDomainListRepository.findByDomainName(emailDomain.getDomainName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email domain already exists");
        }
        
        // Set defaults
        emailDomain.setIsActive(true);
        emailDomain.setAddedAt(LocalDateTime.now());
        
        EmailDomainList savedEmailDomain = emailDomainListRepository.save(emailDomain);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmailDomain);
    }

    @PutMapping("/{domainId}/activate")
    public ResponseEntity<EmailDomainList> activateEmailDomain(@PathVariable UUID domainId) {
        EmailDomainList emailDomain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email domain not found"));
        
        emailDomain.setIsActive(true);
        EmailDomainList updatedEmailDomain = emailDomainListRepository.save(emailDomain);
        
        return ResponseEntity.ok(updatedEmailDomain);
    }

    @PutMapping("/{domainId}/deactivate")
    public ResponseEntity<EmailDomainList> deactivateEmailDomain(@PathVariable UUID domainId) {
        EmailDomainList emailDomain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email domain not found"));
        
        emailDomain.setIsActive(false);
        EmailDomainList updatedEmailDomain = emailDomainListRepository.save(emailDomain);
        
        return ResponseEntity.ok(updatedEmailDomain);
    }

    @DeleteMapping("/{domainId}")
    public ResponseEntity<Void> deleteEmailDomain(@PathVariable UUID domainId) {
        EmailDomainList emailDomain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email domain not found"));
        
        // In a real application, you might want to check if there are users with this domain
        // and handle that accordingly
        
        emailDomainListRepository.delete(emailDomain);
        return ResponseEntity.noContent().build();
    }
} 