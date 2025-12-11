package org.delcom.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProfileController {
    
    @GetMapping("/admin-profile")
    public String adminProfilePage() {
        return "pages/admin-profile";
    }
}