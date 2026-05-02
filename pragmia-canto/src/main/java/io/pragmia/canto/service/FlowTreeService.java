package io.pragmia.canto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pragmia.canto.dag.DagValidator;
import io.pragmia.canto.model.FlowTree;
import io.pragmia.canto.repository.FlowTreeRepository;
import io.pragmia.api.audit.AuditEventType;
import io.pragmia.kernel.audit.AuditEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FlowTreeService {

    private final FlowTreeRepository repo;
    private final DagValidator validator;
    private final AuditEventPublisher audit;
    private final ObjectMapper mapper;

    public List<FlowTree> findAll() { return repo.findAll(); }

    public FlowTree findById(String id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("FlowTree not found: " + id));
    }

    @Transactional
    public FlowTree create(FlowTree tree, String adminId) {
        var result = validator.validate(tree.getDefinitionJson());
        if (!result.valid()) throw new IllegalArgumentException("Invalid flow: " + result.error());
        var saved = repo.save(tree);
        audit.publish(AuditEventType.ADMIN_FLOW_UPDATED, adminId, null, null,
            null, "flow_tree", "CREATE", "OK", null,
            Map.of("flowId", saved.getId(), "flowName", saved.getName()));
        return saved;
    }

    @Transactional
    public FlowTree update(String id, FlowTree patch, String adminId) {
        var existing = findById(id);
        var result = validator.validate(patch.getDefinitionJson());
        if (!result.valid()) throw new IllegalArgumentException("Invalid flow: " + result.error());
        existing.setName(patch.getName());
        existing.setDescription(patch.getDescription());
        existing.setDefinitionJson(patch.getDefinitionJson());
        existing.setVersion(existing.getVersion() + 1);
        existing.setUpdatedBy(adminId);
        var saved = repo.save(existing);
        audit.publish(AuditEventType.ADMIN_FLOW_UPDATED, adminId, null, null,
            null, "flow_tree", "UPDATE", "OK", null,
            Map.of("flowId", id, "version", saved.getVersion()));
        return saved;
    }

    @Transactional
    public void activate(String id, String adminId) {
        repo.findAll().forEach(f -> { f.setActive(false); repo.save(f); });
        var tree = findById(id);
        tree.setActive(true);
        repo.save(tree);
        audit.publish(AuditEventType.ADMIN_FLOW_UPDATED, adminId, null, null,
            null, "flow_tree", "ACTIVATE", "OK", null, Map.of("flowId", id));
    }

    @Transactional
    public void delete(String id, String adminId) {
        repo.deleteById(id);
        audit.publish(AuditEventType.ADMIN_FLOW_UPDATED, adminId, null, null,
            null, "flow_tree", "DELETE", "OK", null, Map.of("flowId", id));
    }
}
