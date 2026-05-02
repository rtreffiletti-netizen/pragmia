package io.pragmia.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/saml/idp")
public class SamlIdpController {

    private final SamlProperties properties;
    private String cachedMetadataXml;

    @Autowired
    public SamlIdpController(SamlProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String metadata() {
        if (cachedMetadataXml == null) {
            cachedMetadataXml = generateMetadataXml();
        }
        return cachedMetadataXml;
    }

    @GetMapping("/metadata.json")
    @ResponseBody
    public Map<String, Object> metadataJson() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("entityId", properties.getIdp().getEntityId());
        meta.put("ssoUrl", properties.getIdp().getSsoUrl());
        meta.put("sloUrl", properties.getIdp().getSloUrl());
        meta.put("binding", "HTTP-POST");
        return meta;
    }

    private String generateMetadataXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<EntitiesDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\">\n" +
            "  <EntityDescriptor entityID=\"" + properties.getIdp().getEntityId() + "\">\n" +
            "    <IDPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
            "      <KeyDescriptor use=\"signing\">\n" +
            "        <KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\"></KeyInfo>\n" +
            "      </KeyDescriptor>\n" +
            "      <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" +
            properties.getIdp().getSsoUrl() + "\"/>\n" +
            "      <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"" +
            properties.getIdp().getSsoUrl() + "\"/>\n" +
            "      <SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Post\" Location=\"" +
            properties.getIdp().getSloUrl() + "\"/>\n" +
            "    </IDPSSODescriptor>\n" +
            "  </EntityDescriptor>\n" +
            "</EntitiesDescriptor>";
    }
}
