package cit.edu.workforce.Security;

import cit.edu.workforce.Entity.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String username;
    @JsonIgnore
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isActive;

    public UserDetailsImpl(UUID userId, String username, String password, String role, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        this.isActive = isActive;
    }

    public static UserDetailsImpl build(UserAccount userAccount, String role) {
        return new UserDetailsImpl(
                userAccount.getUserId(),
                userAccount.getEmailAddress(),
                userAccount.getPassword(),
                role,
                userAccount.getIsActive()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
} 