package io.pragmia.sibilla.service;

import io.pragmia.sibilla.scim.model.ScimUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProvisioningService {

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ProvisioningService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<ScimUser> listUsers(String filter, int offset, int limit) {
        return jdbc.query(
            "SELECT id, username, email, enabled FROM virgilio_users LIMIT ? OFFSET ?",
            (rs, i) -> ScimUser.builder()
                .id(rs.getString("id"))
                .userName(rs.getString("username"))
                .active(rs.getBoolean("enabled"))
                .emails(List.of(ScimUser.ScimEmail.builder()
                    .value(rs.getString("email"))
                    .type("work").primary(true).build()))
                .build(),
            limit, offset);
    }

    public ScimUser getUser(String id) {
        return jdbc.queryForObject(
            "SELECT id, username, email, enabled FROM virgilio_users WHERE id = ?",
            (rs, i) -> ScimUser.builder()
                .id(rs.getString("id"))
                .userName(rs.getString("username"))
                .active(rs.getBoolean("enabled"))
                .build(),
            id);
    }

    public ScimUser createUser(ScimUser user) {
        String id = UUID.randomUUID().toString();
        String tempPassword = encoder.encode(UUID.randomUUID().toString());
        String email = user.getEmails() != null && !user.getEmails().isEmpty()
            ? user.getEmails().get(0).getValue() : user.getUserName();
        jdbc.update(
            "INSERT INTO virgilio_users (id, username, password, email, enabled, role) VALUES (?,?,?,?,?,?)",
            id, user.getUserName(), tempPassword, email, user.isActive(), "USER");
        user.setId(id);
        log.info("[SIBILLA] User provisioned via SCIM: {}", user.getUserName());
        return user;
    }

    public ScimUser updateUser(ScimUser user) {
        String email = user.getEmails() != null && !user.getEmails().isEmpty()
            ? user.getEmails().get(0).getValue() : null;
        jdbc.update("UPDATE virgilio_users SET username=?, email=?, enabled=? WHERE id=?",
            user.getUserName(), email, user.isActive(), user.getId());
        return user;
    }

    public ScimUser patchUser(String id, Map<String, Object> patch) {
        var ops = (List<Map<String, Object>>) patch.get("Operations");
        if (ops != null) {
            for (var op : ops) {
                String opName = (String) op.get("op");
                Object val = op.get("value");
                if ("replace".equalsIgnoreCase(opName) && val instanceof Map valMap) {
                    if (valMap.containsKey("active")) {
                        boolean active = Boolean.parseBoolean(valMap.get("active").toString());
                        jdbc.update("UPDATE virgilio_users SET enabled=? WHERE id=?", active, id);
                    }
                }
            }
        }
        return getUser(id);
    }

    public void deleteUser(String id) {
        jdbc.update("UPDATE virgilio_users SET enabled=false WHERE id=?", id);
        log.info("[SIBILLA] User deprovisioned via SCIM: {}", id);
    }
}
