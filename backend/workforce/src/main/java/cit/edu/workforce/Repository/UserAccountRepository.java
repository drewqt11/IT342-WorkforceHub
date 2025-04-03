package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailAddress(String emailAddress);
    boolean existsByEmailAddress(String emailAddress);
} 