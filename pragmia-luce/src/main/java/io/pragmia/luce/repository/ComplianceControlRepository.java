package io.pragmia.luce.repository;

import io.pragmia.luce.model.ComplianceControl;
import io.pragmia.luce.model.ComplianceFramework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceControlRepository extends JpaRepository<ComplianceControl, String> {
    List<ComplianceControl> findByFramework(ComplianceFramework framework);
}
