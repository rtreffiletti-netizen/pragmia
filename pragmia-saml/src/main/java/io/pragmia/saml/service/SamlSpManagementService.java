package io.pragmia.saml.service;

import io.pragmia.saml.dto.SamlSpRegistrationRequest;
import io.pragmia.saml.dto.SamlSpRegistrationResponse;
import io.pragmia.saml.model.SamlServiceProvider;
import io.pragmia.saml.repository.SamlServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Gestisce la registrazione e il ciclo di vita degli SP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlSpManagementService {

    private final SamlServiceProviderRepository spRepository;

    @Transactional
    public SamlSpRegistrationResponse registerSp(SamlSpRegistrationRequest req) {
        if (spRepository.findByEntityId(req.getEntityId()).isPresent()) {
            throw new IllegalArgumentException("SP già registrato con entityId: " + req.getEntityId());
        }
        SamlServiceProvider sp = new SamlServiceProvider();
        sp.setEntityId(req.getEntityId());
        sp.setName(req.getName());
        sp.setAcsUrl(req.getAcsUrl());
        sp.setSloUrl(req.getSloUrl());
        sp.setMetadataUrl(req.getMetadataUrl());
        sp.setSigningCertificate(req.getSigningCertificate());
        sp.setAttributeMapping(req.getAttributeMapping());
        sp.setAllowedFlow(req.getAllowedFlow());
        sp.setRequireSignedRequests(req.isRequireSignedRequests());
        sp.setEncryptAssertions(req.isEncryptAssertions());
        spRepository.save(sp);
        log.info("[PRAGMIA-SAML] SP registrato: {}", sp.getEntityId());
        return SamlSpRegistrationResponse.builder()
            .id(sp.getId()).entityId(sp.getEntityId()).name(sp.getName())
            .acsUrl(sp.getAcsUrl()).enabled(sp.isEnabled())
            .message("SP registrato con successo").build();
    }

    public List<SamlServiceProvider> listEnabledSps() {
        return spRepository.findByEnabledTrue();
    }

    @Transactional
    public void deleteSp(String id) {
        spRepository.deleteById(id);
        log.info("[PRAGMIA-SAML] SP rimosso: {}", id);
    }
}
