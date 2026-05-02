package io.pragmia.sibilla.scim.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScimListResponse<T> {

    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse");

    @JsonProperty("totalResults")
    private int totalResults;

    @JsonProperty("startIndex")
    private int startIndex = 1;

    @JsonProperty("itemsPerPage")
    private int itemsPerPage;

    @JsonProperty("Resources")
    private List<T> resources;
}
