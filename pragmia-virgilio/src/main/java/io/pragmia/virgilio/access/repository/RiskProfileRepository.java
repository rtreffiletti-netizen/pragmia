package io.pragmia.virgilio.access.repository;

import io.pragmia.virgilio.access.model.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, UUID> {
}
