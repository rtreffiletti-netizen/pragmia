package io.pragmia.saml;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/saml/sp")
public class SamlSpController {

    private final SamlProperties properties;

    public SamlSpController(SamlProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String metadata() {
        return generateSpMetadata();
    }

    @GetMapping("/list")
    @ResponseBody
    public List<Map<String, Object>> listSp() {
        return properties.getSps().stream().map(sp -> {
            Map<String, Object> m = new HashMap<>();
            m.put("entityId", sp.getEntityId());
            m.put("acsUrl", sp.getAssertionConsumerServiceUrl());
            m.put("jitProvisioning", sp.isJitProvisioning());
            m.put("active", true);
            return m;
        }).toList();
    }

    @GetMapping("/acs")
    public String handleAssertion() {
        return "redirect:/home";
    }

    private String generateSpMetadata() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<EntitiesDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\">\n" +
            "  <EntityDescriptor entityID=\"https://localhost:8080/saml/sp\">\n" +
            "    <SPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
            "      <AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" " +
            "Location=\"/saml/sp/acs\"/>\n" +
            "      <SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Post\" " +
            "Location=\"/saml/sp/slo\"/>\n" +
            "    </SPSSODescriptor>\n" +
            "  </EntityDescriptor>\n" +
            "</EntitiesDescriptor>";
    }
}
