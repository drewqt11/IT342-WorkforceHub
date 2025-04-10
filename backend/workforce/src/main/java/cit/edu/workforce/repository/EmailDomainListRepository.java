package cit.edu.workforce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.EmailDomainListEntity;

@Repository
public interface EmailDomainListRepository extends JpaRepository<EmailDomainListEntity, String> {

    Optional<EmailDomainListEntity> findByDomainName(String domainName);

    List<EmailDomainListEntity> findByIsActive(boolean isActive);

    Boolean existsByDomainName(String domainName);
}
