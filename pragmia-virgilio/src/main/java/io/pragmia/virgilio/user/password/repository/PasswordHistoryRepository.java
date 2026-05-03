
package io.pragmia.virgilio.user.password.repository;

import io.pragmia.virgilio.user.password.model.PasswordHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    @Query("SELECT h FROM PasswordHistory h WHERE h.userId = :userId ORDER BY h.createdAt DESC")
    List<PasswordHistory> findLatestByUserId(UUID userId, PageRequest pageable);

    long countByUserId(UUID userId);

    @Modifying
    @Query(value =
        "DELETE FROM pragmia_virgilio_password_history " +
        "WHERE user_id = :userId " +
        "  AND id NOT IN (" +
        "    SELECT id FROM pragmia_virgilio_password_history " +
        "    WHERE user_id = :userId " +
        "    ORDER BY created_at DESC " +
        "    LIMIT :keepCount" +
        "  )",
        nativeQuery = true)
    void pruneHistory(UUID userId, int keepCount);
}
