package cit.edu.workforce.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cit.edu.workforce.Entity.RefreshTokenEntity;
import cit.edu.workforce.Entity.UserAccountEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByToken(String token);

    Optional<RefreshTokenEntity> findByUserAccount(UserAccountEntity userAccount);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userAccount.userId = :userId")
    void revokeAllTokensByUser(String userId);

    void deleteByUserAccount(UserAccountEntity userAccount);
}
