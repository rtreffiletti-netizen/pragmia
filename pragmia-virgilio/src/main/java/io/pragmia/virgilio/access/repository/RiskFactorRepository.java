package io.pragmia.virgilio.access.repository;

import io.pragmia.virgilio.access.model.RiskFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RiskFactorRepository extends JpaRepository<RiskFactor, UUID> {
}
