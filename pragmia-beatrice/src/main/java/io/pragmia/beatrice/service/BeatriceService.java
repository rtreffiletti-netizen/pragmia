package io.pragmia.beatrice.service;

import io.pragmia.api.audit.AuditEventType;
import io.pragmia.beatrice.llm.OllamaClient;
import io.pragmia.beatrice.model.NlpCommand;
import io.pragmia.beatrice.model.NlpCommandStatus;
import io.pragmia.beatrice.repository.NlpCommandRepository;
import io.pragmia.kernel.audit.AuditEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeatriceService {

    private final NlpCommandRepository repo;
    private final OllamaClient ollama;
    private final AuditEventPublisher audit;

    @Transactional
    public NlpCommand submit(String prompt, String requestedBy) {
        String resolvedAction = ollama.chat(prompt, List.of());
        boolean isDestructive = isDestructive(resolvedAction);
        var cmd = NlpCommand.builder()
            .prompt(prompt)
            .resolvedAction(resolvedAction)
            .status(isDestructive ? NlpCommandStatus.PENDING_APPROVAL : NlpCommandStatus.APPROVED)
            .requestedBy(requestedBy)
            .build();
        var saved = repo.save(cmd);
        audit.publish(AuditEventType.NLP_COMMAND_RECEIVED, requestedBy, null, null, null,
            "nlp_command", "SUBMIT",
            isDestructive ? "PENDING" : "AUTO_APPROVED", null,
            Map.of("commandId", saved.getId(), "destructive", isDestructive));
        if (!isDestructive) execute(saved, requestedBy);
        return saved;
    }

    @Transactional
    public NlpCommand approve(String id, String approvedBy) {
        var cmd = repo.findById(id).orElseThrow();
        if (cmd.getStatus() != NlpCommandStatus.PENDING_APPROVAL)
            throw new IllegalStateException("Command not pending approval");
        if (cmd.getRequestedBy().equals(approvedBy))
            throw new IllegalStateException("Requester cannot approve own command (dual-approval)");
        cmd.setApprovedBy(approvedBy);
        cmd.setStatus(NlpCommandStatus.APPROVED);
        cmd.setDecidedAt(Instant.now());
        repo.save(cmd);
        audit.publish(AuditEventType.NLP_DUAL_APPROVAL_GRANTED, approvedBy, null, null, null,
            "nlp_command", "APPROVE", "OK", null, Map.of("commandId", id));
        execute(cmd, approvedBy);
        return cmd;
    }

    @Transactional
    public NlpCommand reject(String id, String rejectedBy, String notes) {
        var cmd = repo.findById(id).orElseThrow();
        cmd.setRejectedBy(rejectedBy);
        cmd.setStatus(NlpCommandStatus.REJECTED);
        cmd.setDecidedAt(Instant.now());
        cmd.setNotes(notes);
        repo.save(cmd);
        audit.publish(AuditEventType.NLP_DUAL_APPROVAL_DENIED, rejectedBy, null, null, null,
            "nlp_command", "REJECT", "OK", null, Map.of("commandId", id, "notes", notes));
        return cmd;
    }

    public List<NlpCommand> getPending() {
        return repo.findByStatus(NlpCommandStatus.PENDING_APPROVAL);
    }

    private void execute(NlpCommand cmd, String executedBy) {
        try {
            log.info("[BEATRICE] Executing command {}: {}", cmd.getId(), cmd.getResolvedAction());
            cmd.setStatus(NlpCommandStatus.EXECUTED);
            repo.save(cmd);
            audit.publish(AuditEventType.NLP_COMMAND_EXECUTED, executedBy, null, null, null,
                "nlp_command", "EXECUTE", "OK", null, Map.of("commandId", cmd.getId()));
        } catch (Exception e) {
            cmd.setStatus(NlpCommandStatus.FAILED);
            cmd.setNotes(e.getMessage());
            repo.save(cmd);
        }
    }

    private boolean isDestructive(String action) {
        if (action == null) return false;
        String lower = action.toLowerCase();
        return lower.contains("delete") || lower.contains("disable")
            || lower.contains("revoke") || lower.contains("drop")
            || lower.contains("reset_password") || lower.contains("kill_session");
    }
}
