package io.pragmia;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/swagger-ui.html";
    }
}
