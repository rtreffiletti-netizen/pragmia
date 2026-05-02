package io.pragmia.virgilio.user;

import io.pragmia.api.audit.*;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.virgilio.user.model.VirgilioUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository       repo;
    private final PasswordEncoder      enc;
    private final AuditEventPublisher  audit;

    @Transactional(readOnly = true)
    public Optional<VirgilioUser> findById(UUID id) { return repo.findById(id); }

    @Transactional(readOnly = true)
    public Optional<VirgilioUser> findByUsername(String u) { return repo.findByUsername(u); }

    @Transactional(readOnly = true)
    public Page<VirgilioUser> findAll(int page, int size, String search) {
        Pageable p = PageRequest.of(page, size, Sort.by("username"));
        return (search != null && !search.isBlank())
            ? repo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, p)
            : repo.findAll(p);
    }

    @Transactional
    public VirgilioUser createUser(String username, String email, String pwd, String fullName, UUID adminId) {
        if (repo.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        VirgilioUser u = new VirgilioUser();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(enc.encode(pwd));
        u.setFullName(fullName);
        VirgilioUser saved = repo.save(u);
        audit.publish(new AuditEvent(UUID.randomUUID().toString(), AuditEventType.ADMIN_USER_CREATED,
            Instant.now(), null, adminId != null ? adminId.toString() : null,
            null, null, null, saved.getId().toString(), "CREATE_USER", "SUCCESS", null,
            Map.of("username", username)));
        return saved;
    }

    @Transactional
    public void disableUser(UUID userId, UUID adminId) {
        repo.findById(userId).ifPresent(u -> {
            u.setEnabled(false);
            repo.save(u);
            audit.publish(new AuditEvent(UUID.randomUUID().toString(), AuditEventType.ADMIN_USER_DISABLED,
                Instant.now(), null, adminId != null ? adminId.toString() : null,
                null, null, null, userId.toString(), "DISABLE_USER", "SUCCESS", null, Map.of()));
        });
    }

    @Transactional
    public boolean verifyPassword(VirgilioUser user, String plainPassword) {
        boolean ok = enc.matches(plainPassword, user.getPasswordHash());
        if (ok) {
            repo.resetLoginAttempts(user.getId());
        } else {
            repo.incrementLoginAttempts(user.getId());
            if (user.getLoginAttempts() + 1 >= MAX_ATTEMPTS) {
                repo.lockUser(user.getId());
                log.warn("[Virgilio] User {} locked after {} failed attempts", user.getUsername(), MAX_ATTEMPTS);
            }
        }
        return ok;
    }
}
