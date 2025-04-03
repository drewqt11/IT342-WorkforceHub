package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmailDomainList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailDomainListRepository extends JpaRepository<EmailDomainList, UUID> {
    Optional<EmailDomainList> findByDomainName(String domainName);
    boolean existsByDomainNameAndIsActiveTrue(String domainName);
} 