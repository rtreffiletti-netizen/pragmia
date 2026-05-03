package io.pragmia.saml.controller;

import io.pragmia.saml.service.SamlMetadataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint discovery metadata SAML2 (URL standard per interoperabilità).
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
public class SamlMetadataController {

    private final SamlMetadataService metadataService;

    /** URL standard usato da molti SP per auto-discovery */
    @GetMapping(value = "/FederationMetadata/2007-06/FederationMetadata.xml",
                produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> federationMetadata(HttpServletRequest req) {
        String base = req.getScheme() + "://" + req.getServerName()
            + (req.getServerPort() != 80 && req.getServerPort() != 443 ? ":" + req.getServerPort() : "");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML)
            .body(metadataService.generateIdpMetadata(base));
    }
}
