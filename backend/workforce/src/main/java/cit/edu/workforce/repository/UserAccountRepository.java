package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, String> {
    Optional<UserAccountEntity> findByEmailAddress(String emailAddress);
    Boolean existsByEmailAddress(String emailAddress);
}