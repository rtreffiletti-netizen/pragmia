package io.pragmia.saml.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SamlSpRegistrationRequest {
    @NotBlank private String entityId;
    @NotBlank private String name;
    @NotBlank private String acsUrl;
    private String sloUrl;
    private String metadataUrl;
    private String signingCertificate;
    private String attributeMapping;
    private String allowedFlow = "both";
    private boolean requireSignedRequests = true;
    private boolean encryptAssertions = false;
}
