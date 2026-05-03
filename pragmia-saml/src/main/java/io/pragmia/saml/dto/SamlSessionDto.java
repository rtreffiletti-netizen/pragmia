package io.pragmia.saml.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class SamlSessionDto {
    private String id;
    private String userId;
    private String spEntityId;
    private String nameId;
    private String sessionIndex;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean active;
    private String authType;
    private String clientIp;
}
