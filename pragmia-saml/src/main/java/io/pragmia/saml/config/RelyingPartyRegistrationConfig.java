package io.pragmia.saml.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.registration.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
public class RelyingPartyRegistrationConfig {

    private final SamlProperties samlProperties;

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        List<RelyingPartyRegistration> registrations = new ArrayList<>();

        for (SamlProperties.ExternalIdp idpConf : samlProperties.getSp().getExternalIdps()) {
            try {
                RelyingPartyRegistration.Builder builder = RelyingPartyRegistrations
                    .fromMetadataLocation(idpConf.getMetadataUrl())
                    .registrationId(idpConf.getRegistrationId())
                    .entityId(samlProperties.getSp().getEntityId())
                    .assertionConsumerServiceLocation(
                        "{baseUrl}/saml/sp/" + idpConf.getRegistrationId() + "/acs");

                // Configurazione specifica per tipo IdP
                if ("spid".equalsIgnoreCase(idpConf.getType())) {
                    applySpidProfile(builder);
                } else if ("cie".equalsIgnoreCase(idpConf.getType())) {
                    applyCieProfile(builder);
                }

                registrations.add(builder.build());
                log.info("[PRAGMIA-SAML] IdP esterno registrato: {} ({})", idpConf.getName(), idpConf.getType());
            } catch (Exception e) {
                log.error("[PRAGMIA-SAML] Impossibile caricare metadata IdP {}: {}", idpConf.getRegistrationId(), e.getMessage());
            }
        }

        if (registrations.isEmpty()) {
            log.warn("[PRAGMIA-SAML] Nessun IdP esterno configurato. Il modulo SP è inattivo.");
            // Bean vuoto per evitare NPE in fase di startup se SAML SP non è configurato
            return new InMemoryRelyingPartyRegistrationRepository(new ArrayList<>());
        }

        return new InMemoryRelyingPartyRegistrationRepository(registrations);
    }

    private void applySpidProfile(RelyingPartyRegistration.Builder builder) {
        // SPID richiede AttributeConsumingServiceIndex e AuthnContextClassRef specifici
        // Configurazione aggiuntiva nel SamlIdpController per authn request customizzate
        log.debug("[PRAGMIA-SAML] Profilo SPID applicato");
    }

    private void applyCieProfile(RelyingPartyRegistration.Builder builder) {
        log.debug("[PRAGMIA-SAML] Profilo CIE applicato");
    }
}
