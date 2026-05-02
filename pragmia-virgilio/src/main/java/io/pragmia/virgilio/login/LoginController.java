package io.pragmia.virgilio.login;

import io.pragmia.virgilio.flow.FlowExecutionResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final FlowAuthService flowAuthService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Model model) {
        if (error  != null) model.addAttribute("errorMsg", "Credenziali non valide");
        if (logout != null) model.addAttribute("logoutMsg", "Logout effettuato");
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String totpCode,
                           HttpServletRequest request,
                           Model model) {
        try {
            FlowExecutionResult result = flowAuthService.authenticate(
                username, password, totpCode,
                request.getSession(true).getId(),
                request.getParameter("client_id"),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

            if (result.isAllowed()) {
                var auth = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                HttpSession session = request.getSession(true);
                session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
                return "redirect:/admin/index.html";
            }

            if (result.isPending()) {
                model.addAttribute("pendingMfa", true);
                model.addAttribute("username", username);
                return "login";
            }

            model.addAttribute("errorMsg", "Accesso negato");
            return "login";

        } catch (Exception e) {
            log.error("[Login] Unexpected error: {}", e.getMessage());
            model.addAttribute("errorMsg", "Errore interno. Riprovare.");
            return "login";
        }
    }
}
