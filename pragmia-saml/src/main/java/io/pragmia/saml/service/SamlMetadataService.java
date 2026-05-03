package io.pragmia.saml.service;

import io.pragmia.saml.config.SamlProperties;
import io.pragmia.saml.model.SamlServiceProvider;
import io.pragmia.saml.repository.SamlServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Genera e serve i metadata XML SAML2 per IdP e SP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlMetadataService {

    private final SamlProperties samlProperties;
    private final SamlServiceProviderRepository spRepository;

    /**
     * Genera il metadata XML di PRAGMIA come IdP.
     * L'SP lo usa per configurare la trust federation.
     */
    public String generateIdpMetadata(String baseUrl) {
        String entityId = samlProperties.getIdp().getEntityId();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\""
            + " xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\""
            + " xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\""
            + " entityID=\"" + entityId + "\">"
            + "<IDPSSODescriptor WantAuthnRequestsSigned=\"true\""
            + " protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
            + "<SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\""
            + " Location=\"" + baseUrl + "/saml/idp/sso\"/>"
            + "<SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\""
            + " Location=\"" + baseUrl + "/saml/idp/sso\"/>"
            + "<SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\""
            + " Location=\"" + baseUrl + "/saml/idp/slo\"/>"
            + "<NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</NameIDFormat>"
            + "<NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</NameIDFormat>"
            + "<NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</NameIDFormat>"
            + "</IDPSSODescriptor>"
            + "</EntityDescriptor>";
    }

    /**
     * Genera il metadata XML di PRAGMIA come SP verso un IdP esterno.
     */
    public String generateSpMetadata(String registrationId, String baseUrl) {
        String entityId = samlProperties.getSp().getEntityId();
        String acsUrl   = baseUrl + "/saml/sp/" + registrationId + "/acs";
        String sloUrl   = baseUrl + "/saml/sp/" + registrationId + "/slo";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\""
            + " entityID=\"" + entityId + "\">"
            + "<SPSSODescriptor AuthnRequestsSigned=\"true\" WantAssertionsSigned=\"true\""
            + " protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
            + "<AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\""
            + " Location=\"" + acsUrl + "\" index=\"0\" isDefault=\"true\"/>"
            + "<SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\""
            + " Location=\"" + sloUrl + "\"/>"
            + "</SPSSODescriptor>"
            + "</EntityDescriptor>";
    }
}
