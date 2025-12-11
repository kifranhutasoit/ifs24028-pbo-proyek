    package org.delcom.app.dto;

    import java.time.LocalDateTime;
    import java.util.UUID;

    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.web.multipart.MultipartFile;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;

    // GANTI NAMA CLASS
    public class BarangForm {

        private UUID id;

        // Dulu: judul
        @NotBlank(message = "Nama barang harus diisi")
        private String namaBarang;

        // Dulu: mataKuliah
        @NotBlank(message = "Kategori harus diisi")
        private String kategori;

        private String deskripsi;

        // Dulu: deadline
        @NotNull(message = "Tanggal restock harus diisi")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") 
        private LocalDateTime tanggalMasuk;

        // Field untuk upload gambar
        private MultipartFile fileGambar;

        // Untuk menyimpan nama file lama saat edit
        private String existingFoto;

        private String status = "READY"; // Default status Thrift

        // Constructor
        public BarangForm() {
        }

        // --- GETTERS AND SETTERS (Sudah disesuaikan namanya) ---

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getNamaBarang() { // Ganti getJudul
            return namaBarang;
        }

        public void setNamaBarang(String namaBarang) { // Ganti setJudul
            this.namaBarang = namaBarang;
        }

        public String getKategori() { // Ganti getMataKuliah
            return kategori;
        }

        public void setKategori(String kategori) { // Ganti setMataKuliah
            this.kategori = kategori;
        }

        public String getDeskripsi() {
            return deskripsi;
        }

        public void setDeskripsi(String deskripsi) {
            this.deskripsi = deskripsi;
        }

        public LocalDateTime getTanggalMasuk() { // Ganti getDeadline
            return tanggalMasuk;
        }

        public void setTanggalMasuk(LocalDateTime tanggalMasuk) { // Ganti setDeadline
            this.tanggalMasuk = tanggalMasuk;
        }

        public MultipartFile getFileGambar() {
            return fileGambar;
        }

        public void setFileGambar(MultipartFile fileGambar) {
            this.fileGambar = fileGambar;
        }

        public String getExistingFoto() {
            return existingFoto;
        }

        public void setExistingFoto(String existingFoto) {
            this.existingFoto = existingFoto;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        // --- Helper Validation ---
        
        public boolean hasImage() {
            return fileGambar != null && !fileGambar.isEmpty();
        }

        public boolean isValidImageFormat() {
            if (!hasImage()) return true; 
            String type = fileGambar.getContentType();
            return type != null && (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/webp"));
        }
    }