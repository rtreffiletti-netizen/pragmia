package io.pragmia.saml.controller;

import io.pragmia.saml.dto.SamlSpRegistrationRequest;
import io.pragmia.saml.dto.SamlSpRegistrationResponse;
import io.pragmia.saml.repository.SamlAuditEventRepository;
import io.pragmia.saml.repository.SamlSessionRepository;
import io.pragmia.saml.service.SamlSpManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SamlAdminController.class)
class SamlAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @MockBean SamlSpManagementService spService;
    @MockBean SamlSessionRepository sessionRepository;
    @MockBean SamlAuditEventRepository auditRepository;

    @Test
    @WithMockUser(authorities = "SCOPE_pragmia:admin")
    void registerSp_shouldReturn200() throws Exception {
        SamlSpRegistrationRequest req = new SamlSpRegistrationRequest();
        req.setEntityId("https://app.example.com");
        req.setName("TestApp");
        req.setAcsUrl("https://app.example.com/saml/acs");

        SamlSpRegistrationResponse resp = SamlSpRegistrationResponse.builder()
            .id("uuid-1").entityId(req.getEntityId()).name(req.getName())
            .enabled(true).message("SP registrato con successo").build();

        when(spService.registerSp(any())).thenReturn(resp);

        mockMvc.perform(post("/api/admin/v1/saml/service-providers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.entityId").value("https://app.example.com"))
            .andExpect(jsonPath("$.message").value("SP registrato con successo"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_pragmia:admin")
    void listSps_shouldReturnEmptyList() throws Exception {
        when(spService.listEnabledSps()).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/v1/saml/service-providers"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }
}
