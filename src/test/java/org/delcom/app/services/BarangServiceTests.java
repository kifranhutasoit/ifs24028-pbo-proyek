package org.delcom.app.services;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Barang;
import org.delcom.app.repositories.BarangRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class BarangServiceTests {

    @Mock
    private BarangRepository barangRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private BarangService barangService;

    // ==========================================
    // 1. TEST CREATE BARANG
    // ==========================================

    @Test
    @DisplayName("Create: Sukses dengan File")
    void testCreateBarang_WithFile() throws IOException {
        Barang barang = new Barang();
        barang.setId(UUID.randomUUID());
        MultipartFile file = mock(MultipartFile.class);
        
        when(file.isEmpty()).thenReturn(false);
        when(barangRepository.save(any(Barang.class))).thenReturn(barang);
        when(fileStorageService.storeFile(file, barang.getId())).thenReturn("foto.jpg");

        barangService.createBarang(barang, file);

        verify(fileStorageService).storeFile(file, barang.getId());
    }

    @Test
    @DisplayName("Create: File Null (Skip Upload)")
    void testCreateBarang_FileNull() throws IOException {
        Barang barang = new Barang();
        when(barangRepository.save(any(Barang.class))).thenReturn(barang);
        
        barangService.createBarang(barang, null);
        
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("Create: File Empty (Skip Upload)")
    void testCreateBarang_FileEmpty() throws IOException {
        Barang barang = new Barang();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true); // File ada tapi kosong

        when(barangRepository.save(any(Barang.class))).thenReturn(barang);

        barangService.createBarang(barang, file);
        
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    // ==========================================
    // 2. TEST UPDATE BARANG
    // ==========================================

    @Test
    @DisplayName("Update: ID Tidak Ditemukan -> Return Null")
    void testUpdateBarang_NotFound() throws IOException {
        UUID id = UUID.randomUUID();
        when(barangRepository.findById(id)).thenReturn(Optional.empty());

        Barang result = barangService.updateBarang(id, new Barang(), null);
        
        assertNull(result); // Pastikan return null
    }

    @Test
    @DisplayName("Update: File Null (Tidak Ganti Foto)")
    void testUpdateBarang_FileNull() throws IOException {
        UUID id = UUID.randomUUID();
        Barang existing = new Barang(); 
        existing.setId(id);
        
        when(barangRepository.findById(id)).thenReturn(Optional.of(existing));
        when(barangRepository.save(any(Barang.class))).thenReturn(existing);

        barangService.updateBarang(id, new Barang(), null);

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("Update: File Empty (Tidak Ganti Foto)")
    void testUpdateBarang_FileEmpty() throws IOException {
        UUID id = UUID.randomUUID();
        Barang existing = new Barang(); existing.setId(id);
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        when(barangRepository.findById(id)).thenReturn(Optional.of(existing));
        when(barangRepository.save(any(Barang.class))).thenReturn(existing);

        barangService.updateBarang(id, new Barang(), file);

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("Update: Ganti Foto, TAPI Foto Lama Null (Cover Baris 71)")
    void testUpdateBarang_NewFile_OldPhotoNull() throws IOException {
        UUID id = UUID.randomUUID();
        Barang existing = new Barang(); 
        existing.setId(id);
        existing.setFoto(null); // Foto lama tidak ada

        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);

        when(barangRepository.findById(id)).thenReturn(Optional.of(existing));
        when(barangRepository.save(any(Barang.class))).thenReturn(existing);
        when(fileStorageService.storeFile(newFile, id)).thenReturn("baru.jpg");

        barangService.updateBarang(id, new Barang(), newFile);

        verify(fileStorageService, never()).deleteFile(any()); // Jangan hapus apa2
        verify(fileStorageService).storeFile(newFile, id);     // Tetap upload baru
    }

    @Test
    @DisplayName("Update: Ganti Foto Normal (Hapus Lama, Simpan Baru)")
    void testUpdateBarang_NewFile_WithOldPhoto() throws IOException {
        UUID id = UUID.randomUUID();
        Barang existing = new Barang(); 
        existing.setId(id);
        existing.setFoto("lama.jpg");

        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);

        when(barangRepository.findById(id)).thenReturn(Optional.of(existing));
        when(barangRepository.save(any(Barang.class))).thenReturn(existing);
        when(fileStorageService.storeFile(newFile, id)).thenReturn("baru.jpg");

        barangService.updateBarang(id, new Barang(), newFile);

        verify(fileStorageService).deleteFile("lama.jpg");
        verify(fileStorageService).storeFile(newFile, id);
    }

    // ==========================================
    // 3. TEST UPDATE STATUS (Fix Line 91)
    // ==========================================

    @Test
    @DisplayName("Update Status: Sukses")
    void testUpdateStatus_Success() {
        UUID id = UUID.randomUUID();
        Barang b = new Barang();
        when(barangRepository.findById(id)).thenReturn(Optional.of(b));
        when(barangRepository.save(b)).thenReturn(b);

        Barang result = barangService.updateStatus(id, "SOLD");
        assertEquals("SOLD", result.getStatus());
    }

    @Test
    @DisplayName("Update Status: ID Tidak Ditemukan -> Return Null (Cover Line 91)")
    void testUpdateStatus_NotFound() {
        UUID id = UUID.randomUUID();
        when(barangRepository.findById(id)).thenReturn(Optional.empty()); // Tidak ketemu

        Barang result = barangService.updateStatus(id, "SOLD");
        
        assertNull(result); // Cover return null di paling bawah method
    }

    // ==========================================
    // 4. TEST DELETE BARANG (Fix Line 98 & 100)
    // ==========================================

    @Test
    @DisplayName("Delete: ID Tidak Ditemukan (Cover Line 98)")
    void testDeleteBarang_NotFound() {
        UUID id = UUID.randomUUID();
        when(barangRepository.findById(id)).thenReturn(Optional.empty());

        barangService.deleteBarang(id);

        // Pastikan tidak ada delete yang dipanggil
        verify(barangRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete: Barang Ada, TAPI Foto Null (Cover Line 100)")
    void testDeleteBarang_NoPhoto() {
        UUID id = UUID.randomUUID();
        Barang b = new Barang();
        b.setFoto(null); // Tidak punya foto

        when(barangRepository.findById(id)).thenReturn(Optional.of(b));

        barangService.deleteBarang(id);

        verify(fileStorageService, never()).deleteFile(any()); // Jangan panggil service delete file
        verify(barangRepository).delete(b); // Tapi tetap hapus data DB
    }

    @Test
    @DisplayName("Delete: Barang Ada & Foto Ada")
    void testDeleteBarang_WithPhoto() {
        UUID id = UUID.randomUUID();
        Barang b = new Barang();
        b.setFoto("gambar.jpg");

        when(barangRepository.findById(id)).thenReturn(Optional.of(b));

        barangService.deleteBarang(id);

        verify(fileStorageService).deleteFile("gambar.jpg");
        verify(barangRepository).delete(b);
    }

    // ==========================================
    // 5. TEST GETTERS
    // ==========================================
    @Test
    @DisplayName("Get All & Get By Id")
    void testGetters() {
        UUID id = UUID.randomUUID();
        when(barangRepository.findById(id)).thenReturn(Optional.of(new Barang()));
        assertNotNull(barangService.getBarangById(id));

        barangService.getAllBarang(id);
        verify(barangRepository).findAllByUserIdOrderByTanggalMasukDesc(id);
    }
}