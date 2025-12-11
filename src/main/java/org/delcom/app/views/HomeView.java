package org.delcom.app.views;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeView {

    @GetMapping
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Jika user SUDAH LOGIN, lempar ke /barang (JANGAN ke /tugas)
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/barang"; // <--- PASTIKAN INI /barang
        }

        // Jika BELUM LOGIN, lempar ke halaman login
        return "redirect:/auth/login";
    }
}