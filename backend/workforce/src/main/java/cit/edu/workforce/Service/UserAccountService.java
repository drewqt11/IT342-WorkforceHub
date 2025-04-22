package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.UserAccountEntity;
import cit.edu.workforce.Repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    @Autowired
    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public UserAccountEntity createUserAccount(String email) {
        if (userAccountRepository.existsByEmailAddress(email)) {
            throw new RuntimeException("Email address is already taken");
        }

        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.setEmailAddress(email);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setActive(true);

        return userAccountRepository.save(userAccount);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccountEntity> findByEmail(String email) {
        return userAccountRepository.findByEmailAddress(email);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccountEntity> findById(String userId) {
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
    public void deleteUser(String userId) {
        userAccountRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailDomain(String email) {
        // Extract domain from email
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        // Check if the domain is @cit.edu as required
        return "cit.edu".equals(domain);
    }
}