package io.pragmia.saml.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "pragmia.saml")
public class SamlProperties {

    private boolean enabled = true;
    private Idp idp = new Idp();
    private Sp sp = new Sp();

    @Data
    public static class Idp {
        /** Entity ID esposto da PRAGMIA come IdP */
        private String entityId = "https://auth.example.com/saml/idp";
        /** Path al keystore JKS o PKCS12 per firma/cifratura */
        private String keystorePath = "classpath:saml/pragmia-saml.jks";
        private String keystorePassword = "changeit";
        private String keyAlias = "pragmia-saml";
        private String keyPassword = "changeit";
        /** Sessione SAML SSO: durata massima in secondi (default 8h) */
        private int ssoSessionMaxAge = 28800;
        /** SP registrati come trusted (per IdP-initiated flow) */
        private List<TrustedSp> trustedSps = new ArrayList<>();
    }

    @Data
    public static class Sp {
        /** Entity ID di PRAGMIA come SP (consumer di IdP esterni) */
        private String entityId = "https://auth.example.com/saml/sp";
        /** Lista IdP esterni (Azure AD, ADFS, SPID, CIE) */
        private List<ExternalIdp> externalIdps = new ArrayList<>();
    }

    @Data
    public static class TrustedSp {
        private String entityId;
        private String acsUrl;
        private String metadataUrl;
        private String name;
    }

    @Data
    public static class ExternalIdp {
        private String registrationId;
        private String entityId;
        private String metadataUrl;
        private String name;
        /** spid | cie | azuread | adfs | generic */
        private String type = "generic";
    }
}
