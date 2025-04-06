package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByEmailAddress(String emailAddress);
    Boolean existsByEmailAddress(String emailAddress);
} 