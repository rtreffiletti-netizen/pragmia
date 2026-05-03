package io.pragmia.saml.service;

import io.pragmia.saml.config.SamlProperties;
import io.pragmia.saml.model.SamlServiceProvider;
import io.pragmia.saml.repository.SamlSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SamlAssertionServiceTest {

    @Mock SamlSessionRepository sessionRepository;
    @Mock SamlSignatureService signatureService;
    @InjectMocks SamlAssertionService service;

    private SamlProperties props;
    private SamlServiceProvider sp;

    @BeforeEach
    void setUp() {
        props = new SamlProperties();
        props.getIdp().setEntityId("https://auth.example.com/saml/idp");
        // Inject props manualmente per il test
        try {
            var f = SamlAssertionService.class.getDeclaredField("samlProperties");
            f.setAccessible(true);
            f.set(service, props);
        } catch (Exception e) { throw new RuntimeException(e); }

        sp = new SamlServiceProvider();
        sp.setEntityId("https://myapp.example.com");
        sp.setAcsUrl("https://myapp.example.com/saml/acs");
        sp.setName("MyApp");
    }

    @Test
    void buildSignedResponse_shouldReturnValidXml() {
        Map<String, List<String>> attrs = Map.of("email", List.of("user@example.com"));

        String result = service.buildSignedResponse("user1", "user@example.com", sp, attrs, "_req123");

        assertThat(result).contains("samlp:Response");
        assertThat(result).contains("user@example.com");
        assertThat(result).contains("https://myapp.example.com");
        assertThat(result).contains("urn:oasis:names:tc:SAML:2.0:status:Success");
        verify(sessionRepository).save(any());
    }

    @Test
    void buildSignedResponse_idpInitiated_shouldNotContainInResponseTo() {
        String result = service.buildSignedResponse("user1", "user@example.com", sp, null, null);
        assertThat(result).doesNotContain("InResponseTo=");
    }
}
