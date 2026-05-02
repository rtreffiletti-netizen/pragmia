package io.pragmia.virgilio.user;

import io.pragmia.virgilio.user.model.VirgilioUser;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<VirgilioUser, UUID> {
    Optional<VirgilioUser> findByUsername(String username);
    Optional<VirgilioUser> findByEmail(String email);
    boolean existsByUsername(String username);
    Page<VirgilioUser> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String username, String email, Pageable pageable);

    @Modifying
    @Query("UPDATE VirgilioUser u SET u.loginAttempts = u.loginAttempts + 1 WHERE u.id = :id")
    void incrementLoginAttempts(UUID id);

    @Modifying
    @Query("UPDATE VirgilioUser u SET u.loginAttempts = 0, u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void resetLoginAttempts(UUID id);

    @Modifying
    @Query("UPDATE VirgilioUser u SET u.locked = true WHERE u.id = :id")
    void lockUser(UUID id);

    long countByEnabled(boolean enabled);
    long countByLocked(boolean locked);
}
