package io.pragmia.saml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "pragmia.saml")
public class SamlProperties {

    private Idp idp = new Idp();
    private List<Sp> sps = new ArrayList<>();

    public static class Idp {
        private boolean enabled = false;
        private String entityId = "https://localhost:8080/saml/idp";
        private String ssoUrl = "/saml/idp/sso";
        private String sloUrl = "/saml/idp/slo";
        private String certificate = "classpath:certs/idp.crt";
        private String privateKey = "classpath:certs/idp.key";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getSsoUrl() { return ssoUrl; }
        public void setSsoUrl(String ssoUrl) { this.ssoUrl = ssoUrl; }
        public String getSloUrl() { return sloUrl; }
        public void setSloUrl(String sloUrl) { this.sloUrl = sloUrl; }
        public String getCertificate() { return certificate; }
        public void setCertificate(String certificate) { this.certificate = certificate; }
        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    }

    public static class Sp {
        private String entityId;
        private String assertionConsumerServiceUrl;
        private String singleLogoutServiceUrl;
        private String metadataUri;
        private String certificate;
        private boolean jitProvisioning = true;
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getAssertionConsumerServiceUrl() { return assertionConsumerServiceUrl; }
        public void setAssertionConsumerServiceUrl(String url) { this.assertionConsumerServiceUrl = url; }
        public String getSingleLogoutServiceUrl() { return singleLogoutServiceUrl; }
        public void setSingleLogoutServiceUrl(String url) { this.singleLogoutServiceUrl = url; }
        public String getMetadataUri() { return metadataUri; }
        public void setMetadataUri(String uri) { this.metadataUri = uri; }
        public String getCertificate() { return certificate; }
        public void setCertificate(String cert) { this.certificate = cert; }
        public boolean isJitProvisioning() { return jitProvisioning; }
        public void setJitProvisioning(boolean jit) { this.jitProvisioning = jit; }
    }

    public Idp getIdp() { return idp; }
    public void setIdp(Idp idp) { this.idp = idp; }
    public List<Sp> getSps() { return sps; }
    public void setSps(List<Sp> sps) { this.sps = sps; }
}
