package cit.edu.workforce.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.UserAccountEntity;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByEmailAddress(String emailAddress);
    Boolean existsByEmailAddress(String emailAddress);
} 