package io.pragmia.luce.service;

import io.pragmia.luce.check.ComplianceCheck;
import io.pragmia.luce.model.*;
import io.pragmia.luce.repository.ComplianceReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceReportService {

    private final List<ComplianceCheck> checks;
    private final ComplianceReportRepository reportRepo;

    public ComplianceReport generate(ComplianceFramework framework, String requestedBy) {
        List<ControlResult> results = checks.stream()
            .map(check -> {
                try { return check.execute(); }
                catch (Exception e) {
                    return ControlResult.builder()
                        .controlId(check.getControlId())
                        .status(ControlStatus.FAIL)
                        .details("Exception: " + e.getMessage()).build();
                }
            })
            .toList();

        long passed = results.stream().filter(r -> r.getStatus() == ControlStatus.PASS).count();
        long failed = results.stream().filter(r -> r.getStatus() == ControlStatus.FAIL).count();
        long na     = results.stream().filter(r -> r.getStatus() == ControlStatus.NOT_APPLICABLE
                                                || r.getStatus() == ControlStatus.MANUAL_CHECK_REQUIRED).count();

        var report = ComplianceReport.builder()
            .framework(framework)
            .generatedBy(requestedBy)
            .totalControls(results.size())
            .passedControls((int) passed)
            .failedControls((int) failed)
            .notApplicable((int) na)
            .results(results)
            .build();

        var saved = reportRepo.save(report);
        log.info("[LUCE] {} report generated: {}/{} PASS, score={:.1f}%",
            framework, passed, results.size(), saved.getComplianceScore());
        return saved;
    }

    @Scheduled(cron = "0 0 2 * * MON")  // ogni lunedì alle 02:00
    public void weeklyReports() {
        for (ComplianceFramework fw : ComplianceFramework.values()) {
            try { generate(fw, "system-scheduler"); }
            catch (Exception e) { log.error("[LUCE] Weekly report failed for {}: {}", fw, e.getMessage()); }
        }
    }
}
