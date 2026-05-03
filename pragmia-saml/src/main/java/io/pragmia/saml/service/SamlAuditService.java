package io.pragmia.saml.service;

import io.pragmia.saml.model.SamlAuditEvent;
import io.pragmia.saml.repository.SamlAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SamlAuditService {

    private final SamlAuditEventRepository auditRepository;

    public void logSsoSuccess(String userId, String spEntityId, String ip) {
        save("SSO_SUCCESS", userId, spEntityId, null, ip, "SUCCESS", null);
    }

    public void logSsoFailure(String userId, String spEntityId, String ip, String reason) {
        save("SSO_FAILURE", userId, spEntityId, null, ip, "FAILURE", reason);
    }

    public void logSloRequest(String userId, String spEntityId, String ip) {
        save("SLO_REQUEST", userId, spEntityId, null, ip, "INITIATED", null);
    }

    public void logMetadataRequest(String userId, String entityId, String ip) {
        save("METADATA_REQUEST", userId, entityId, null, ip, "SUCCESS", null);
    }

    private void save(String type, String userId, String spEntityId, String idpEntityId,
                      String ip, String result, String details) {
        SamlAuditEvent e = new SamlAuditEvent();
        e.setEventType(type);
        e.setUserId(userId);
        e.setSpEntityId(spEntityId);
        e.setIdpEntityId(idpEntityId);
        e.setClientIp(ip);
        e.setResult(result);
        e.setDetails(details);
        auditRepository.save(e);
    }
}
