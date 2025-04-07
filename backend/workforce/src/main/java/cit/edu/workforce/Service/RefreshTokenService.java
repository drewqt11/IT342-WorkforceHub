package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.RefreshTokenEntity;
import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.RefreshTokenRepository;
import cit.edu.workforce.Repository.UserAccountRepository;
import cit.edu.workforce.Security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenDurationMs; // 7 days

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserAccountRepository userAccountRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshTokenEntity createRefreshToken(String userId) {
        UserAccountEntity userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Delete existing refresh tokens for the user
        refreshTokenRepository.findByUserAccount(userAccount)
                .ifPresent(token -> refreshTokenRepository.delete(token));

        // Create new refresh token
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUserAccount(userAccount);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.isRevoked() || token.isUsed() || token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void revokeAllByUser(String userId) {
        refreshTokenRepository.revokeAllTokensByUser(userId);
    }

    @Transactional
    public void markTokenAsUsed(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setUsed(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}