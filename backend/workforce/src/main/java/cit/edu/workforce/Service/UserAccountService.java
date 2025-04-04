package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAccountService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccountEntity createUserAccount(String email, String password) {
        if (userAccountRepository.existsByEmailAddress(email)) {
            throw new RuntimeException("Email address is already taken");
        }

        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.setEmailAddress(email);
        userAccount.setPassword(passwordEncoder.encode(password));
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setActive(true);

        return userAccountRepository.save(userAccount);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccountEntity> findByEmail(String email) {
        return userAccountRepository.findByEmailAddress(email);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccountEntity> findById(UUID userId) {
        return userAccountRepository.findById(userId);
    }

    @Transactional
    public UserAccountEntity updateLastLogin(UserAccountEntity userAccount) {
        userAccount.setLastLogin(LocalDateTime.now());
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public UserAccountEntity activateUser(UserAccountEntity userAccount) {
        userAccount.setActive(true);
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public UserAccountEntity deactivateUser(UserAccountEntity userAccount) {
        userAccount.setActive(false);
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userAccountRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailDomain(String email) {
        // In a real application, you would check the domain against the email_domain_list table
        // This is a simplified version
        String domain = email.substring(email.indexOf("@") + 1);
        // Check if the domain is in the whitelist
        return true; // This should be replaced with actual domain verification
    }
} 