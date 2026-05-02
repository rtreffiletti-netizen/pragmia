package io.pragmia.luce.repository;

import io.pragmia.luce.model.ComplianceFramework;
import io.pragmia.luce.model.ComplianceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, String> {
    Page<ComplianceReport> findByFrameworkOrderByGeneratedAtDesc(ComplianceFramework framework, Pageable pageable);
}
