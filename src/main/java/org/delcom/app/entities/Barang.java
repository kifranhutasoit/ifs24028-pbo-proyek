package org.delcom.app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.*;

@Entity
@Table(name = "barang") // Nama tabel di database berubah jadi 'barang'
@JsonPropertyOrder({ "id", "namaBarang", "kategori", "tanggalMasuk", "status", "createdAt", "updatedAt" })
public class Barang { // Nama Class berubah dari Tugas menjadi Barang

    // ======= Attributes =======
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false  , nullable = false, columnDefinition = "uuid")
    private UUID id;

    // Dulunya 'judul' -> Kita ubah jadi 'nama_barang'
    @Column(name = "nama_barang", nullable = false)
    private String namaBarang;

    // Dulunya 'mataKuliah' -> Kita ubah jadi 'kategori' (Misal: Sepatu, Jaket)
    @Column(name = "kategori", nullable = false)
    private String kategori;

    // Dulunya 'deskripsi' -> Tetap deskripsi (Untuk detail kondisi/size)
    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    // Dulunya 'deadline' -> Kita ubah jadi 'tanggal_masuk'
    @Column(name = "tanggal_masuk")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") 
    private LocalDateTime tanggalMasuk;

    // Dulunya 'fotoBukti' -> Kita persingkat jadi 'foto'
    @Column(name = "foto")
    private String foto;

    @Column(name = "status", nullable = false)
    private String status; // Isi: "READY", "SOLD"

    // Relasi ke User (Admin yang upload barang)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ======= Constructors =======

    public Barang() {
    }

    public Barang(String namaBarang, String kategori, String deskripsi, LocalDateTime tanggalMasuk, User user) {
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.tanggalMasuk = tanggalMasuk;
        this.user = user;
        this.status = "READY"; // Default status saat barang baru diupload
    }

    // ======= Getters and Setters (Updated Names) =======

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

    public String getFoto() { // Ganti getFotoBukti
        return foto;
    }

    public void setFoto(String foto) { // Ganti setFotoBukti
        this.foto = foto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ======= Lifecycle Hooks =======
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "READY"; // Default status
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}