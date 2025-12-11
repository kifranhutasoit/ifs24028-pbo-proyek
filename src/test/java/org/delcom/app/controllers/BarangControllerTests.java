package org.delcom.app.controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Barang;
import org.delcom.app.entities.User;
import org.delcom.app.services.BarangService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class BarangControllerTests {

    @Mock
    private BarangService barangService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private BarangController barangController;

    private User user;
    private MultipartFile file;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        file = mock(MultipartFile.class);
        uuid = UUID.randomUUID();
    }

    // ========================================================================
    // 1. GET ALL BARANG
    // ========================================================================
    
    @Test
    @DisplayName("Get All: Unauthorized (Cover Baris 47)")
    void testGetAll_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<List<Barang>>> response = barangController.getAllBarang();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Get All: Success")
    void testGetAll_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(barangService.getAllBarang(any())).thenReturn(List.of(new Barang()));

        ResponseEntity<ApiResponse<List<Barang>>> response = barangController.getAllBarang();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================================================
    // 2. CREATE BARANG (POST)
    // ========================================================================

    @Test
    @DisplayName("Create: Unauthorized (Cover Baris 63)")
    void testCreate_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        var response = barangController.createBarang("N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Create: IOException / Gagal Upload (Cover Baris 76-77)")
    void testCreate_IOException() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        
        // Simulasi Service melempar error IO
        when(barangService.createBarang(any(Barang.class), any()))
            .thenThrow(new IOException("Disk Full"));

        var response = barangController.createBarang("N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Create: Format Tanggal Salah (Cover Baris 78-79)")
    void testCreate_DateError() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);

        // Kirim tanggal ngawur
        var response = barangController.createBarang("N", "K", "D", "BUKAN-TANGGAL", file);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Create: Exception Umum (Cover Baris 80-81)")
    void testCreate_GeneralException() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);

        // Simulasi error tak terduga (bukan IO, bukan Date)
        when(barangService.createBarang(any(Barang.class), any()))
            .thenThrow(new RuntimeException("Database Down"));

        var response = barangController.createBarang("N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Create: Success")
    void testCreate_Success() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(user);
        when(barangService.createBarang(any(Barang.class), any())).thenReturn(new Barang());

        var response = barangController.createBarang("N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================================================
    // 3. UPDATE BARANG (PUT)
    // ========================================================================

    @Test
    @DisplayName("Update: Unauthorized (Cover Baris 95)")
    void testUpdate_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        var response = barangController.updateBarang(uuid, "N", "K", "D", "2024-01-01", file);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Update: Barang Tidak Ditemukan / Null (Cover Baris 111-112)")
    void testUpdate_NotFound_NullReturn() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);

        // Service return null
        when(barangService.updateBarang(any(UUID.class), any(Barang.class), any()))
            .thenReturn(null);

        var response = barangController.updateBarang(uuid, "N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update: IOException (Cover Baris 117-118)")
    void testUpdate_IOException() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);
        
        when(barangService.updateBarang(any(UUID.class), any(Barang.class), any()))
            .thenThrow(new IOException("IO Error"));

        var response = barangController.updateBarang(uuid, "N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Update: Date Error (Cover Baris 119-120)")
    void testUpdate_DateError() {
        when(authContext.isAuthenticated()).thenReturn(true);
        var response = barangController.updateBarang(uuid, "N", "K", "D", "SALAH", file);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Update: Runtime Exception (Cover Baris 121-122)")
    void testUpdate_RuntimeException() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);
        
        // Simulasi error "Barang tidak ditemukan" via Exception
        when(barangService.updateBarang(any(UUID.class), any(Barang.class), any()))
            .thenThrow(new RuntimeException("Not Found"));

        var response = barangController.updateBarang(uuid, "N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update: Success")
    void testUpdate_Success() throws IOException {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(barangService.updateBarang(any(UUID.class), any(Barang.class), any()))
            .thenReturn(new Barang());

        var response = barangController.updateBarang(uuid, "N", "K", "D", "2024-01-01T10:00:00", file);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================================================
    // 4. UPDATE STATUS (PATCH)
    // ========================================================================

    @Test
    @DisplayName("Status: Unauthorized (Cover Baris 132)")
    void testStatus_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        var response = barangController.updateStatus(uuid, "SOLD");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Status: Input Null (Cover Baris 137-138)")
    void testStatus_NullInput() {
        when(authContext.isAuthenticated()).thenReturn(true);
        var response = barangController.updateStatus(uuid, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Status: Runtime Exception / Not Found (Cover Baris 146-147)")
    void testStatus_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        
        when(barangService.updateStatus(any(UUID.class), anyString()))
            .thenThrow(new RuntimeException("Not Found"));

        var response = barangController.updateStatus(uuid, "SOLD");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Status: Success")
    void testStatus_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(barangService.updateStatus(any(UUID.class), anyString())).thenReturn(new Barang());
        
        var response = barangController.updateStatus(uuid, "\"SOLD\"");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================================================
    // 5. DELETE BARANG
    // ========================================================================

    @Test
    @DisplayName("Delete: Unauthorized (Cover Baris 154)")
    void testDelete_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        var response = barangController.deleteBarang(uuid);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Delete: Exception (Cover Baris 161-163)")
    void testDelete_Exception() {
        when(authContext.isAuthenticated()).thenReturn(true);
        
        // Paksa delete throw error (misal foreign key constraint)
        doThrow(new RuntimeException("DB Error")).when(barangService).deleteBarang(uuid);

        var response = barangController.deleteBarang(uuid);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Delete: Success")
    void testDelete_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        var response = barangController.deleteBarang(uuid);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}