package io.pragmia.virgilio.login;

import io.pragmia.virgilio.flow.model.FlowDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ActiveFlowProvider {

    private final JdbcTemplate jdbc;

    public Optional<String> getActiveFlowJson() {
        try {
            String sql = "SELECT flow_definition FROM pragmia_virgilio_auth_flows WHERE is_active = true LIMIT 1";
            return jdbc.queryForList(sql, String.class).stream().findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
