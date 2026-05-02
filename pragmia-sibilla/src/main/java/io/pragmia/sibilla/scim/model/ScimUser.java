package io.pragmia.sibilla.scim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScimUser {

    private String id;
    private String externalId;
    private String userName;
    private ScimName name;
    private List<ScimEmail> emails;
    private boolean active = true;

    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:User");

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ScimName {
        private String formatted;
        private String givenName;
        private String familyName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ScimEmail {
        private String value;
        private String type;
        private boolean primary;
    }
}
