package cit.edu.workforce.Repository;

import cit.edu.workforce.Entity.RefreshTokenEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);
    
    Optional<RefreshTokenEntity> findByUserAccount(UserAccountEntity userAccount);
    
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userAccount.userId = :userId")
    void revokeAllTokensByUser(UUID userId);
    
    void deleteByUserAccount(UserAccountEntity userAccount);
} 