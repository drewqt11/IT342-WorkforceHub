package cit.edu.workforce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.UserAccountEntity;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, String> {

    Optional<UserAccountEntity> findByEmailAddress(String emailAddress);

    Boolean existsByEmailAddress(String emailAddress);
}
