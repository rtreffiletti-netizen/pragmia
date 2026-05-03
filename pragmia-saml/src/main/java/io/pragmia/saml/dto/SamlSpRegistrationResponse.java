package io.pragmia.saml.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SamlSpRegistrationResponse {
    private String id;
    private String entityId;
    private String name;
    private String metadataUrl;
    private String acsUrl;
    private boolean enabled;
    private String message;
}
