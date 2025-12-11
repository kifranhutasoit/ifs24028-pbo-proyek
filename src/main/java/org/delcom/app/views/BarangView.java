    package org.delcom.app.views;

    import java.io.IOException;
    import java.util.List;
    import java.util.UUID;

    import org.delcom.app.entities.Barang;
    import org.delcom.app.entities.User;
    import org.delcom.app.services.BarangService;
    import org.springframework.security.authentication.AnonymousAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.multipart.MultipartFile;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    @Controller
    @RequestMapping("/barang") // <--- URL SUDAH DIGANTI JADI /barang
    public class BarangView {

        private final BarangService barangService;

        public BarangView(BarangService barangService) {
            this.barangService = barangService;
        }

        // --- HELPER: Mengambil User yang sedang Login ---
        private User getAuthenticatedUser() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                Object principal = auth.getPrincipal();
                if (principal instanceof User) {
                    return (User) principal;
                }
            }
            return null;
        }

        // 1. Tampilkan Halaman DAFTAR BARANG (Stok)
        @GetMapping
        public String showList(Model model) {
            User user = getAuthenticatedUser();
            if (user == null) return "redirect:/auth/login";
            
            List<Barang> listBarang = barangService.getAllBarang(user.getId());
            
            // Catatan: Nama attribute tetap "listTugas" agar cocok dengan th:each di index.html
            model.addAttribute("listTugas", listBarang);
            model.addAttribute("userName", user.getName());
            
            // UPDATE: Ambil dari folder 'barang'
            return "pages/barang/index"; 
        }

        // 2. Tampilkan Form UPLOAD BARANG
        @GetMapping("/create")
        public String showCreateForm(Model model) {
            if (getAuthenticatedUser() == null) return "redirect:/auth/login";
            
            // Catatan: Nama attribute tetap "tugas" agar cocok dengan th:object di create.html
            model.addAttribute("barang", new Barang());
            
            // UPDATE: Ambil dari folder 'barang'
            return "pages/barang/create";
        }

        // 3. Proses SIMPAN BARANG BARU
        @PostMapping("/store")
        public String storeBarang(
                @ModelAttribute Barang barang,
                @RequestParam("fileGambar") MultipartFile file, 
                RedirectAttributes redirectAttributes
        ) {
            User user = getAuthenticatedUser();
            if (user == null) return "redirect:/auth/login";

            try {
                barang.setUser(user);
                barangService.createBarang(barang, file);
                redirectAttributes.addFlashAttribute("success", "Barang berhasil ditambahkan ke stok!");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Gagal mengupload foto barang.");
            }

            // UPDATE: Redirect ke /barang
            return "redirect:/barang";
        }

        // 4. Tampilkan Form EDIT BARANG
        @GetMapping("/edit/{id}")
        public String showEditForm(@PathVariable UUID id, Model model) {
            if (getAuthenticatedUser() == null) return "redirect:/auth/login";

            Barang barang = barangService.getBarangById(id);
            if (barang == null) return "redirect:/barang";

            model.addAttribute("barang", barang);
            
            // UPDATE: Ambil dari folder 'barang'
            return "pages/barang/edit";
        }

        // 5. Proses UPDATE BARANG
        @PostMapping("/update") 
        public String updateBarang(
                @ModelAttribute Barang barang,
                @RequestParam(value = "fileGambar", required = false) MultipartFile file,
                RedirectAttributes redirectAttributes
        ) {
            if (getAuthenticatedUser() == null) return "redirect:/auth/login";

            try {
                barangService.updateBarang(barang.getId(), barang, file);
                redirectAttributes.addFlashAttribute("success", "Data barang berhasil diperbarui!");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Gagal update gambar.");
            }

            // UPDATE: Redirect ke /barang
            return "redirect:/barang";
        }

        // 6. Proses DELETE BARANG
        @PostMapping("/delete/{id}")
        public String deleteBarang(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
            if (getAuthenticatedUser() == null) return "redirect:/auth/login";

            barangService.deleteBarang(id);
            redirectAttributes.addFlashAttribute("success", "Barang berhasil dihapus dari stok.");
            
            // UPDATE: Redirect ke /barang
            return "redirect:/barang";
        }

        // 7. Proses TANDAI SOLD (Selesai)
        @PostMapping("/{id}/selesai")
        public String tandaiSold(@PathVariable UUID id) {
            if (getAuthenticatedUser() == null) return "redirect:/auth/login";
            
            barangService.updateStatus(id, "Selesai");
            
            // UPDATE: Redirect ke /barang
            return "redirect:/barang";
        }
    }