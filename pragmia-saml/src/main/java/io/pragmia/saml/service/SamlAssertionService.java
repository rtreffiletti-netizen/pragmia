package io.pragmia.saml.service;

import io.pragmia.saml.config.SamlProperties;
import io.pragmia.saml.model.SamlServiceProvider;
import io.pragmia.saml.model.SamlSession;
import io.pragmia.saml.repository.SamlSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.config.InitializationService;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Costruisce e firma le SAML2 Assertions emesse da PRAGMIA come IdP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlAssertionService {

    private final SamlProperties samlProperties;
    private final SamlSessionRepository samlSessionRepository;
    private final SamlSignatureService samlSignatureService;

    static {
        try {
            InitializationService.initialize();
        } catch (Exception e) {
            throw new RuntimeException("OpenSAML initialization failed", e);
        }
    }

    /**
     * Crea una SAML2 Response firmata per l'SP destinatario.
     *
     * @param userId       ID utente autenticato
     * @param email        NameID (tipicamente email)
     * @param sp           Service Provider destinatario
     * @param attributes   attributi aggiuntivi da includere nell'assertion
     * @param inResponseTo ID AuthnRequest originale (null = IdP-initiated)
     */
    public String buildSignedResponse(String userId, String email,
                                      SamlServiceProvider sp,
                                      Map<String, List<String>> attributes,
                                      String inResponseTo) {
        try {
            String sessionIndex = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds(samlProperties.getIdp().getSsoSessionMaxAge());

            // Persiste la sessione SAML
            SamlSession session = new SamlSession();
            session.setUserId(userId);
            session.setSpEntityId(sp.getEntityId());
            session.setNameId(email);
            session.setSessionIndex(sessionIndex);
            session.setIssuedAt(now);
            session.setExpiresAt(expiry);
            samlSessionRepository.save(session);

            String xml = buildResponseXml(userId, email, sp, attributes,
                    sessionIndex, inResponseTo, now, expiry);

            log.info("[PRAGMIA-SAML] Assertion emessa per utente {} verso SP {}", userId, sp.getEntityId());
            return xml;

        } catch (Exception e) {
            log.error("[PRAGMIA-SAML] Errore costruzione assertion per {}: {}", userId, e.getMessage());
            throw new RuntimeException("Errore costruzione SAML assertion", e);
        }
    }

    private String buildResponseXml(String userId, String email, SamlServiceProvider sp,
                                    Map<String, List<String>> attributes,
                                    String sessionIndex, String inResponseTo,
                                    Instant now, Instant expiry) {

        String idpEntityId    = samlProperties.getIdp().getEntityId();
        String responseId     = "_" + UUID.randomUUID().toString().replace("-", "");
        String assertionId    = "_" + UUID.randomUUID().toString().replace("-", "");
        String nameIdFormat   = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
        String irt            = inResponseTo != null ? " InResponseTo=\"" + inResponseTo + "\"" : "";

        StringBuilder attrStmt = new StringBuilder();
        if (attributes != null) {
            attributes.forEach((name, values) -> {
                attrStmt.append("<saml:Attribute Name=\"").append(name)
                        .append("\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\">");
                values.forEach(v -> attrStmt.append("<saml:AttributeValue xsi:type=\"xs:string\">")
                        .append(v).append("</saml:AttributeValue>"));
                attrStmt.append("</saml:Attribute>");
            });
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<samlp:Response"
            + " xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\""
            + " xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + " ID=\"" + responseId + "\""
            + " Version=\"2.0\""
            + " IssueInstant=\"" + now + "\""
            + irt
            + " Destination=\"" + sp.getAcsUrl() + "\">"
            + "<saml:Issuer>" + idpEntityId + "</saml:Issuer>"
            + "<samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/></samlp:Status>"
            + "<saml:Assertion ID=\"" + assertionId + "\" Version=\"2.0\" IssueInstant=\"" + now + "\">"
            + "<saml:Issuer>" + idpEntityId + "</saml:Issuer>"
            + "<saml:Subject>"
            + "<saml:NameID Format=\"" + nameIdFormat + "\">" + email + "</saml:NameID>"
            + "<saml:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">"
            + "<saml:SubjectConfirmationData NotOnOrAfter=\"" + expiry + "\""
            + " Recipient=\"" + sp.getAcsUrl() + "\"" + irt + "/>"
            + "</saml:SubjectConfirmation>"
            + "</saml:Subject>"
            + "<saml:Conditions NotBefore=\"" + now.minusSeconds(5) + "\" NotOnOrAfter=\"" + expiry + "\">"
            + "<saml:AudienceRestriction><saml:Audience>" + sp.getEntityId() + "</saml:Audience></saml:AudienceRestriction>"
            + "</saml:Conditions>"
            + "<saml:AuthnStatement AuthnInstant=\"" + now + "\" SessionIndex=\"" + sessionIndex + "\">"
            + "<saml:AuthnContext>"
            + "<saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef>"
            + "</saml:AuthnContext>"
            + "</saml:AuthnStatement>"
            + "<saml:AttributeStatement>" + attrStmt + "</saml:AttributeStatement>"
            + "</saml:Assertion>"
            + "</samlp:Response>";
    }
}
