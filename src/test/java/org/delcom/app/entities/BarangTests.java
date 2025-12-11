package org.delcom.app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BarangTests {
    @Test
    @DisplayName("Membuat instance dari kelas Barang")
    void testMembuatInstanceBarang() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");

        // Barang dengan status READY
        {
            Barang barang = new Barang("Nike Air Max", "Sepatu", "Size 42, kondisi 90%", 
                                       LocalDateTime.now(), user);

            assert (barang.getNamaBarang().equals("Nike Air Max"));
            assert (barang.getKategori().equals("Sepatu"));
            assert (barang.getDeskripsi().equals("Size 42, kondisi 90%"));
            assert (barang.getTanggalMasuk() != null);
            assert (barang.getUser().equals(user));
            assert (barang.getStatus().equals("READY"));
        }

        // Barang dengan nilai default
        {
            Barang barang = new Barang();

            assert (barang.getId() == null);
            assert (barang.getNamaBarang() == null);
            assert (barang.getKategori() == null);
            assert (barang.getDeskripsi() == null);
            assert (barang.getTanggalMasuk() == null);
            assert (barang.getFoto() == null);
            assert (barang.getStatus() == null);
            assert (barang.getUser() == null);
        }

        // Barang dengan setNilai
        {
            Barang barang = new Barang();
            UUID generatedId = UUID.randomUUID();
            LocalDateTime tanggalMasuk = LocalDateTime.of(2024, 12, 10, 10, 0);
            
            barang.setId(generatedId);
            barang.setNamaBarang("Adidas Jacket");
            barang.setKategori("Jaket");
            barang.setDeskripsi("Size L, kondisi mulus");
            barang.setTanggalMasuk(tanggalMasuk);
            barang.setFoto("/uploads/jacket.jpg");
            barang.setStatus("SOLD");
            barang.setUser(user);
            barang.onCreate();
            barang.onUpdate();

            assert (barang.getId().equals(generatedId));
            assert (barang.getNamaBarang().equals("Adidas Jacket"));
            assert (barang.getKategori().equals("Jaket"));
            assert (barang.getDeskripsi().equals("Size L, kondisi mulus"));
            assert (barang.getTanggalMasuk().equals(tanggalMasuk));
            assert (barang.getFoto().equals("/uploads/jacket.jpg"));
            assert (barang.getStatus().equals("SOLD"));
            assert (barang.getUser().equals(user));
            assert (barang.getCreatedAt() != null);
            assert (barang.getUpdatedAt() != null);
        }

        // Barang dengan onCreate() - status default READY
        {
            Barang barang = new Barang();
            barang.setNamaBarang("Converse Classic");
            barang.setKategori("Sepatu");
            barang.setUser(user);
            barang.onCreate();

            assert (barang.getStatus().equals("READY"));
            assert (barang.getCreatedAt() != null);
            assert (barang.getUpdatedAt() != null);
        }

        // Test onUpdate() - updatedAt berubah
        {
            Barang barang = new Barang();
            barang.onCreate();
            LocalDateTime createdTime = barang.getCreatedAt();
            LocalDateTime firstUpdate = barang.getUpdatedAt();

            // Simulasi delay
            Thread.sleep(10);
            
            barang.onUpdate();
            LocalDateTime secondUpdate = barang.getUpdatedAt();

            assert (barang.getCreatedAt().equals(createdTime)); // createdAt tidak berubah
            assert (secondUpdate.isAfter(firstUpdate)); // updatedAt berubah
        }
    }
}