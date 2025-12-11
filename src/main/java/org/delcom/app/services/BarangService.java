package org.delcom.app.services;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Barang;
import org.delcom.app.repositories.BarangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BarangService { // Nama class berubah jadi BarangService

    private final BarangRepository barangRepository;
    private final FileStorageService fileStorageService;

    public BarangService(BarangRepository barangRepository, FileStorageService fileStorageService) {
        this.barangRepository = barangRepository;
        this.fileStorageService = fileStorageService;
    }

    // 1. Mengambil daftar barang (Stok), diurutkan dari yang TERBARU masuk
    @Transactional(readOnly = true)
    public List<Barang> getAllBarang(UUID userId) {
        // Kita pakai method baru yang sudah dibuat di Repository tadi (Desc = Terbaru diatas)
        return barangRepository.findAllByUserIdOrderByTanggalMasukDesc(userId);
    }

    // 2. Mengambil satu barang detail berdasarkan ID
    @Transactional(readOnly = true)
    public Barang getBarangById(UUID id) {
        return barangRepository.findById(id).orElse(null);
    }

    // 3. Menambah Barang Baru + Upload Foto
    @Transactional
    public Barang createBarang(Barang barang, MultipartFile file) throws IOException {
        // Simpan dulu ke database untuk generate ID
        Barang savedBarang = barangRepository.save(barang);

        // Jika ada file gambar yang diupload
        if (file != null && !file.isEmpty()) {
            // Simpan fisik file ke folder uploads
            String filename = fileStorageService.storeFile(file, savedBarang.getId());
            
            // Update nama file di database (field 'foto')
            savedBarang.setFoto(filename);
            return barangRepository.save(savedBarang); 
        }

        return savedBarang;
    }

    // 4. Update Barang + Ganti Foto (Jika ada)
    @Transactional
    public Barang updateBarang(UUID id, Barang barangDetails, MultipartFile file) throws IOException {
        Barang existingBarang = getBarangById(id);
        if (existingBarang == null) return null;

        // Update Data Text (Sesuai field baru di Entity Barang)
        existingBarang.setNamaBarang(barangDetails.getNamaBarang()); // Dulu: setJudul
        existingBarang.setKategori(barangDetails.getKategori());     // Dulu: setMataKuliah
        existingBarang.setDeskripsi(barangDetails.getDeskripsi());
        existingBarang.setTanggalMasuk(barangDetails.getTanggalMasuk()); // Dulu: setDeadline

        // Logic Ganti Foto
        if (file != null && !file.isEmpty()) {
            // Hapus file lama jika ada (agar storage tidak penuh sampah)
            if (existingBarang.getFoto() != null) {
                fileStorageService.deleteFile(existingBarang.getFoto());
            }
            
            // Upload file baru
            String newFilename = fileStorageService.storeFile(file, existingBarang.getId());
            existingBarang.setFoto(newFilename);
        }

        return barangRepository.save(existingBarang);
    }

    // 5. Update Status Saja (READY / SOLD)
    @Transactional
    public Barang updateStatus(UUID id, String status) {
        Barang barang = getBarangById(id);
        if (barang != null) {
            barang.setStatus(status);
            return barangRepository.save(barang);
        }
        return null;
    }

    // 6. Hapus Barang + Hapus File Foto
    @Transactional
    public void deleteBarang(UUID id) {
        Barang barang = getBarangById(id);
        if (barang != null) {
            // Hapus file fisiknya dari folder agar bersih
            if (barang.getFoto() != null) {
                fileStorageService.deleteFile(barang.getFoto());
            }
            // Hapus data dari database
            barangRepository.delete(barang);
        }
    }
}