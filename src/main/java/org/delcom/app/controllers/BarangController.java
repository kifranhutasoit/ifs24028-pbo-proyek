package org.delcom.app.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Barang;
import org.delcom.app.entities.User;
import org.delcom.app.services.BarangService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/barang")
public class BarangController {

    private final BarangService barangService;
    private final AuthContext authContext; // 1. Ubah jadi final & hapus @Autowired

    // 2. Masukkan AuthContext ke dalam Constructor
    public BarangController(BarangService barangService, AuthContext authContext) {
        this.barangService = barangService;
        this.authContext = authContext;
    }

    // 1. GET: Ambil semua stok barang
    @GetMapping
    public ResponseEntity<ApiResponse<List<Barang>>> getAllBarang() {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }
        
        User user = authContext.getAuthUser();
        List<Barang> barangList = barangService.getAllBarang(user.getId());
        
        return ResponseEntity.ok(new ApiResponse<>("success", "Berhasil mengambil data stok barang", barangList));
    }

    // 2. POST: Upload Barang Baru
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Barang>> createBarang(
            @RequestParam("namaBarang") String namaBarang,
            @RequestParam("kategori") String kategori,
            @RequestParam(value = "deskripsi", required = false) String deskripsi,
            @RequestParam("tanggalMasuk") String tanggalStr,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        try {
            User user = authContext.getAuthUser();
            LocalDateTime tanggalMasuk = LocalDateTime.parse(tanggalStr);

            Barang newBarang = new Barang(namaBarang, kategori, deskripsi, tanggalMasuk, user);
            Barang savedBarang = barangService.createBarang(newBarang, file);

            return ResponseEntity.ok(new ApiResponse<>("success", "Barang berhasil diupload ke stok", savedBarang));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal upload foto barang", null));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Format tanggal salah (Gunakan ISO-8601)", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Terjadi kesalahan: " + e.getMessage(), null));
        }
    }

    // 3. PUT: Update Detail Barang
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Barang>> updateBarang(
            @PathVariable UUID id,
            @RequestParam("namaBarang") String namaBarang,
            @RequestParam("kategori") String kategori,
            @RequestParam(value = "deskripsi", required = false) String deskripsi,
            @RequestParam("tanggalMasuk") String tanggalStr,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        try {
            LocalDateTime tanggalMasuk = LocalDateTime.parse(tanggalStr);
            
            Barang barangDetails = new Barang();
            barangDetails.setNamaBarang(namaBarang);
            barangDetails.setKategori(kategori);
            barangDetails.setDeskripsi(deskripsi);
            barangDetails.setTanggalMasuk(tanggalMasuk);

            Barang updatedBarang = barangService.updateBarang(id, barangDetails, file);

            // Perbaikan logic: Biasanya service throw exception jika null, tapi jika return null kita handle disini
            if (updatedBarang == null) {
                return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Barang tidak ditemukan", null));
            }

            return ResponseEntity.ok(new ApiResponse<>("success", "Data barang berhasil diupdate", updatedBarang));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal memproses gambar", null));
        } catch (DateTimeParseException e) {
             return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Format tanggal salah", null));
        } catch (RuntimeException e) {
             return ResponseEntity.status(404).body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    // 4. PATCH: Update Status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Barang>> updateStatus(
            @PathVariable UUID id, 
            @RequestBody String status 
    ) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }
        
        // Perbaikan: Cek null sebelum replace
        if (status == null) {
             return ResponseEntity.badRequest().body(new ApiResponse<>("fail", "Status tidak boleh kosong", null));
        }

        String cleanStatus = status.replace("\"", "").trim();

        try {
            Barang updatedBarang = barangService.updateStatus(id, cleanStatus);
            return ResponseEntity.ok(new ApiResponse<>("success", "Status barang berhasil diubah", updatedBarang));
        } catch (RuntimeException e) { // Menangkap jika ID tidak ditemukan dari Service
            return ResponseEntity.status(404).body(new ApiResponse<>("fail", "Barang tidak ditemukan", null));
        }
    }

    // 5. DELETE: Hapus Barang
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBarang(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ApiResponse<>("fail", "Unauthorized", null));
        }

        try {
            barangService.deleteBarang(id);
            return ResponseEntity.ok(new ApiResponse<>("success", "Barang berhasil dihapus dari stok", null));
        } catch (Exception e) {
            // Jaga-jaga jika delete gagal karena constraint DB dll
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Gagal menghapus barang", null));
        }
    }
}