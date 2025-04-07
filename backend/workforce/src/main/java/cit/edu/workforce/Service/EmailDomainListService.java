package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.EmailDomainListEntity;
import cit.edu.workforce.Repository.EmailDomainListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmailDomainListService {

    private final EmailDomainListRepository emailDomainListRepository;

    @Autowired
    public EmailDomainListService(EmailDomainListRepository emailDomainListRepository) {
        this.emailDomainListRepository = emailDomainListRepository;
    }

    @Transactional(readOnly = true)
    public List<EmailDomainListEntity> getAllDomains() {
        return emailDomainListRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EmailDomainListEntity> getActiveDomains() {
        return emailDomainListRepository.findByIsActive(true);
    }

    @Transactional(readOnly = true)
    public Optional<EmailDomainListEntity> getDomainById(String domainId) {
        return emailDomainListRepository.findById(domainId);
    }

    @Transactional(readOnly = true)
    public Optional<EmailDomainListEntity> getDomainByName(String domainName) {
        return emailDomainListRepository.findByDomainName(domainName);
    }

    @Transactional
    public EmailDomainListEntity addDomain(String domainName) {
        if (emailDomainListRepository.existsByDomainName(domainName)) {
            throw new RuntimeException("Domain already exists");
        }

        EmailDomainListEntity domain = new EmailDomainListEntity();
        domain.setDomainName(domainName);
        domain.setActive(true);
        domain.setAddedAt(LocalDateTime.now());

        return emailDomainListRepository.save(domain);
    }

    @Transactional
    public EmailDomainListEntity updateDomain(String domainId, String domainName, boolean isActive) {
        EmailDomainListEntity domain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found"));

        domain.setDomainName(domainName);
        domain.setActive(isActive);

        return emailDomainListRepository.save(domain);
    }

    @Transactional
    public void deleteDomain(String domainId) {
        emailDomainListRepository.deleteById(domainId);
    }

    @Transactional
    public EmailDomainListEntity activateDomain(String domainId) {
        EmailDomainListEntity domain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found"));

        domain.setActive(true);
        return emailDomainListRepository.save(domain);
    }

    @Transactional
    public EmailDomainListEntity deactivateDomain(String domainId) {
        EmailDomainListEntity domain = emailDomainListRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found"));

        domain.setActive(false);
        return emailDomainListRepository.save(domain);
    }

    @Transactional(readOnly = true)
    public boolean isValidDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        // Extract domain from email
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        // Check if the domain is @cit.edu as required
        if ("cit.edu".equals(domain)) {
            return true;
        }

        // If not cit.edu, check against the database
        List<EmailDomainListEntity> activeDomains = emailDomainListRepository.findByIsActive(true);

        // If no domains are configured in the database, only allow cit.edu
        if (activeDomains.isEmpty()) {
            return false;
        }

        // Check if the domain is in the active list
        Optional<EmailDomainListEntity> domainEntity = emailDomainListRepository.findByDomainName(domain);
        return domainEntity.isPresent() && domainEntity.get().isActive();
    }
}