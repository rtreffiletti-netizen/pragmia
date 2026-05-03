package io.pragmia.saml.service;

import io.pragmia.saml.config.SamlProperties;
import io.pragmia.saml.repository.SamlServiceProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SamlMetadataServiceTest {

    @Mock SamlServiceProviderRepository spRepository;
    @InjectMocks SamlMetadataService service;

    private final String BASE = "https://auth.example.com";

    @Test
    void generateIdpMetadata_shouldContainEntityDescriptor() {
        injectProps("https://auth.example.com/saml/idp");
        String xml = service.generateIdpMetadata(BASE);
        assertThat(xml).contains("EntityDescriptor");
        assertThat(xml).contains("IDPSSODescriptor");
        assertThat(xml).contains("SingleSignOnService");
        assertThat(xml).contains("/saml/idp/sso");
    }

    @Test
    void generateSpMetadata_shouldContainAcsUrl() {
        injectProps("https://auth.example.com/saml/sp");
        String xml = service.generateSpMetadata("azuread", BASE);
        assertThat(xml).contains("SPSSODescriptor");
        assertThat(xml).contains("AssertionConsumerService");
        assertThat(xml).contains("/saml/sp/azuread/acs");
    }

    private void injectProps(String entityId) {
        SamlProperties props = new SamlProperties();
        props.getIdp().setEntityId("https://auth.example.com/saml/idp");
        props.getSp().setEntityId("https://auth.example.com/saml/sp");
        try {
            var f = SamlMetadataService.class.getDeclaredField("samlProperties");
            f.setAccessible(true);
            f.set(service, props);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
