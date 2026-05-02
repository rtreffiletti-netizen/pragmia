package io.pragmia.virgilio.api.admin;

import io.pragmia.virgilio.user.*;
import io.pragmia.virgilio.user.model.VirgilioRole;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/v1/nlp")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PRAGMIA_ADMIN')")
@Tag(name = "NLP Admin", description = "Amministrazione in linguaggio naturale")
public class NlpAdminController {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, String> body) {
        String q = body.get("query");
        if (q == null) return ResponseEntity.badRequest().body(Map.of("error", "Query mancante"));
        String s = q.toLowerCase();
        if (s.equals("statistiche")) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("utenti_totali", userRepository.count());
            stats.put("utenti_attivi", userRepository.countByEnabled(true));
            stats.put("utenti_bloccati", userRepository.countByLocked(true));
            return ResponseEntity.ok(stats);
        }
        if (s.equals("stato cluster")) {
            return ResponseEntity.ok(Map.of("stato", "OK", "nodi", 2));
        }
        if (s.equals("ruoli")) {
            return ResponseEntity.ok(Map.of("ruoli", roleService.findAll().stream()
                .map(r -> Map.of("nome", r.getName())).toList()));
        }
        return ResponseEntity.ok(Map.of("message", "Intent non riconosciuto"));
    }

    @GetMapping("/intents")
    public ResponseEntity<List<SupportedIntent>> listIntents() {
        return ResponseEntity.ok(List.of(
            new SupportedIntent("statistiche", "Statistiche piattaforma", "statistiche"),
            new SupportedIntent("stato_cluster", "Stato nodi cluster", "stato cluster"),
            new SupportedIntent("ruoli", "Lista ruoli", "ruoli")
        ));
    }

    public record SupportedIntent(String code, String description, String example) {}
}
