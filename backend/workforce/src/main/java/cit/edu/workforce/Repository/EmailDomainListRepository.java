package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.EmailDomainListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailDomainListRepository extends JpaRepository<EmailDomainListEntity, String> {
    Optional<EmailDomainListEntity> findByDomainName(String domainName);
    List<EmailDomainListEntity> findByIsActive(boolean isActive);
    Boolean existsByDomainName(String domainName);
}