package io.pragmia.virgilio.user;

import io.pragmia.virgilio.user.model.VirgilioUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VirgilioUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        VirgilioUser u = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.builder()
            .username(u.getUsername())
            .password(u.getPasswordHash())
            .authorities(u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .collect(Collectors.toSet()))
            .disabled(!u.isEnabled())
            .accountLocked(u.isLocked())
            .build();
    }
}
