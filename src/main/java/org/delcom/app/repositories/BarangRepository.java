package org.delcom.app.repositories;

import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Barang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarangRepository extends JpaRepository<Barang, UUID> {

    // 1. Mengambil semua barang milik user tertentu
    // PENTING: Kita ubah 'OrderByDeadlineAsc' menjadi 'OrderByTanggalMasukDesc'
    // Mengapa Desc? Agar barang yang BARU diupload muncul paling ATAS (Newest First).
    List<Barang> findAllByUserIdOrderByTanggalMasukDesc(UUID userId);

    // 2. Filter barang berdasarkan Status (Misal: Mau lihat yang "READY" saja)
    List<Barang> findAllByUserIdAndStatus(UUID userId, String status);

    // 3. (Opsional/Bonus) Pencarian Barang berdasarkan Kategori
    // Berguna jika nanti Anda ingin filter: "Tampilkan semua Sepatu"
    List<Barang> findAllByUserIdAndKategori(UUID userId, String kategori);
    
}