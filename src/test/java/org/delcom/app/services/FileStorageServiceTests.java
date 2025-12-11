package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTests {

    private FileStorageService service;

    @TempDir
    Path tempDir; // Folder sementara otomatis dari JUnit

    @BeforeEach
    void setUp() {
        service = new FileStorageService();
        service.setUploadDir(tempDir.toString());
    }

    // --- GROUP 1: TEST MENYIMPAN FILE (STORE) ---

    @Test
    @DisplayName("Harus membuat folder jika belum ada (Fixes Red Line 28)")
    void testStoreFile_CreatesDirectory() throws IOException {
        // Arahkan uploadDir ke subfolder yang BELUM ADA
        Path nonExistentDir = tempDir.resolve("folder_baru");
        service.setUploadDir(nonExistentDir.toString());

        // Setup mock file
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        // Action
        service.storeFile(mockFile, UUID.randomUUID());

        // Assert: Pastikan folder benar-benar dibuat
        assertTrue(Files.exists(nonExistentDir));
    }

    @Test
    @DisplayName("Filename ada ekstensi (Fixes Yellow Line 35 - Part A)")
    void testStoreFile_WithExtension() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("gambar.png");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("abc".getBytes()));

        String result = service.storeFile(mockFile, UUID.randomUUID());
        assertTrue(result.endsWith(".png"));
    }

    @Test
    @DisplayName("Filename tanpa ekstensi (Fixes Yellow Line 35 - Part B)")
    void testStoreFile_NoExtension() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("file_tanpa_titik"); // Tidak ada "."
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("abc".getBytes()));

        String result = service.storeFile(mockFile, UUID.randomUUID());
        // Harus tidak punya ekstensi, cuma "barang" + UUID
        assertFalse(result.contains(".")); 
    }

    @Test
    @DisplayName("Filename NULL (Fixes Yellow Line 35 - Part C)")
    void testStoreFile_NullFilename() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null); // Null
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("abc".getBytes()));

        String result = service.storeFile(mockFile, UUID.randomUUID());
        assertNotNull(result);
    }

    // --- GROUP 2: TEST MENGHAPUS FILE (DELETE) ---

    @Test
    @DisplayName("Delete normal")
    void testDeleteFile_Success() throws IOException {
        String filename = "hapus_aku.txt";
        Files.createFile(tempDir.resolve(filename));
        
        boolean result = service.deleteFile(filename);
        assertTrue(result);
    }

    @Test
    @DisplayName("Delete parameter invalid (Fixes Yellow Line 49)")
    void testDeleteFile_InvalidInput() {
        assertFalse(service.deleteFile(null));
        assertFalse(service.deleteFile(""));
    }

    @Test
    @DisplayName("Simulasi IOException saat delete (Fixes Red Line 54-55)")
    void testDeleteFile_ThrowsIOException() throws IOException {
        // 1. Kita pakai teknik SPY untuk memantau service asli
        FileStorageService spyService = spy(service);

        // 2. Kita paksa method internalDelete melempar error IOException
        doThrow(new IOException("Disk Error Simualtion"))
            .when(spyService).deleteFileInternal(any(Path.class));

        // 3. Panggil method deleteFile
        boolean result = spyService.deleteFile("file_apapun.txt");

        // 4. Assert: Harusnya return false karena masuk catch block
        assertFalse(result);
    }
}