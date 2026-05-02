package io.pragmia;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/admin/index.html";
    }
}
