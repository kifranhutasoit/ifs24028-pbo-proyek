package org.delcom.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InformasiController {
    
    @GetMapping("/informasi")
    public String informasiPage() {
        return "pages/informasi";
    }
}