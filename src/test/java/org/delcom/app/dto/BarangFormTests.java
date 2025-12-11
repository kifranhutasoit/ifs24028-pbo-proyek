package org.delcom.app.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class BarangFormTests {

    private BarangForm barangForm;
    private MultipartFile mockMultipartFile;
    private Validator validator;

    @BeforeEach
    void setup() {
        barangForm = new BarangForm();
        mockMultipartFile = mock(MultipartFile.class);
        
        // Setup validator untuk testing Jakarta Validation
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Constructor default membuat objek kosong dengan status READY")
    void constructor_default_membuat_objek_kosong_dengan_status_ready() {
        // Act
        BarangForm form = new BarangForm();

        // Assert
        assertNull(form.getId());
        assertNull(form.getNamaBarang());
        assertNull(form.getKategori());
        assertNull(form.getDeskripsi());
        assertNull(form.getTanggalMasuk());
        assertNull(form.getFileGambar());
        assertNull(form.getExistingFoto());
        assertEquals("READY", form.getStatus()); // Default status
    }

    @Test
    @DisplayName("Setter dan Getter untuk ID bekerja dengan benar")
    void setter_dan_getter_untuk_id_bekerja_dengan_benar() {
        // Arrange
        UUID expectedId = UUID.randomUUID();

        // Act
        barangForm.setId(expectedId);
        UUID actualId = barangForm.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    @DisplayName("Setter dan Getter untuk namaBarang bekerja dengan benar")
    void setter_dan_getter_untuk_namaBarang_bekerja_dengan_benar() {
        // Arrange
        String expectedNama = "Nike Air Max 97";

        // Act
        barangForm.setNamaBarang(expectedNama);
        String actualNama = barangForm.getNamaBarang();

        // Assert
        assertEquals(expectedNama, actualNama);
    }

    @Test
    @DisplayName("Setter dan Getter untuk kategori bekerja dengan benar")
    void setter_dan_getter_untuk_kategori_bekerja_dengan_benar() {
        // Arrange
        String expectedKategori = "Sepatu";

        // Act
        barangForm.setKategori(expectedKategori);
        String actualKategori = barangForm.getKategori();

        // Assert
        assertEquals(expectedKategori, actualKategori);
    }

    @Test
    @DisplayName("Setter dan Getter untuk deskripsi bekerja dengan benar")
    void setter_dan_getter_untuk_deskripsi_bekerja_dengan_benar() {
        // Arrange
        String expectedDeskripsi = "Size 42, kondisi 90%, warna hitam";

        // Act
        barangForm.setDeskripsi(expectedDeskripsi);
        String actualDeskripsi = barangForm.getDeskripsi();

        // Assert
        assertEquals(expectedDeskripsi, actualDeskripsi);
    }

    @Test
    @DisplayName("Setter dan Getter untuk tanggalMasuk bekerja dengan benar")
    void setter_dan_getter_untuk_tanggalMasuk_bekerja_dengan_benar() {
        // Arrange
        LocalDateTime expectedTanggal = LocalDateTime.of(2024, 12, 10, 10, 30);

        // Act
        barangForm.setTanggalMasuk(expectedTanggal);
        LocalDateTime actualTanggal = barangForm.getTanggalMasuk();

        // Assert
        assertEquals(expectedTanggal, actualTanggal);
    }

    @Test
    @DisplayName("Setter dan Getter untuk fileGambar bekerja dengan benar")
    void setter_dan_getter_untuk_fileGambar_bekerja_dengan_benar() {
        // Act
        barangForm.setFileGambar(mockMultipartFile);
        MultipartFile actualFile = barangForm.getFileGambar();

        // Assert
        assertEquals(mockMultipartFile, actualFile);
    }

    @Test
    @DisplayName("Setter dan Getter untuk existingFoto bekerja dengan benar")
    void setter_dan_getter_untuk_existingFoto_bekerja_dengan_benar() {
        // Arrange
        String expectedFoto = "old-photo.jpg";

        // Act
        barangForm.setExistingFoto(expectedFoto);
        String actualFoto = barangForm.getExistingFoto();

        // Assert
        assertEquals(expectedFoto, actualFoto);
    }

    @Test
    @DisplayName("Setter dan Getter untuk status bekerja dengan benar")
    void setter_dan_getter_untuk_status_bekerja_dengan_benar() {
        // Act
        barangForm.setStatus("SOLD");
        String actualStatus = barangForm.getStatus();

        // Assert
        assertEquals("SOLD", actualStatus);
    }

    @Test
    @DisplayName("hasImage return false ketika fileGambar null")
    void hasImage_return_false_ketika_fileGambar_null() {
        // Arrange
        barangForm.setFileGambar(null);

        // Act
        boolean result = barangForm.hasImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("hasImage return false ketika fileGambar empty")
    void hasImage_return_false_ketika_fileGambar_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.hasImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("hasImage return true ketika fileGambar tidak empty")
    void hasImage_return_true_ketika_fileGambar_tidak_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.hasImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImageFormat return true ketika tidak ada image")
    void isValidImageFormat_return_true_ketika_tidak_ada_image() {
        // Arrange
        barangForm.setFileGambar(null);

        // Act
        boolean result = barangForm.isValidImageFormat();

        // Assert
        assertTrue(result); // Return true karena tidak wajib upload
    }

    @Test
    @DisplayName("isValidImageFormat return false ketika contentType null")
    void isValidImageFormat_return_false_ketika_contentType_null() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn(null);
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.isValidImageFormat();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImageFormat return true untuk image/jpeg")
    void isValidImageFormat_return_true_untuk_image_jpeg() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.isValidImageFormat();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImageFormat return true untuk image/png")
    void isValidImageFormat_return_true_untuk_image_png() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.isValidImageFormat();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImageFormat return true untuk image/webp")
    void isValidImageFormat_return_true_untuk_image_webp() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/webp");
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.isValidImageFormat();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImageFormat return false untuk image/gif")
    void isValidImageFormat_return_false_untuk_image_gif() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        barangForm.setFileGambar(mockMultipartFile);

        // Act
        boolean result = barangForm.isValidImageFormat();

        // Assert
        assertFalse(result); // GIF tidak didukung di BarangForm
    }

    @Test
    @DisplayName("isValidImageFormat return false untuk content type non-image")
    void isValidImageFormat_return_false_untuk_content_type_non_image() {
        // Arrange
        String[] invalidContentTypes = {
                "text/plain",
                "application/pdf",
                "video/mp4",
                "audio/mpeg",
                "image/svg+xml",
                "image/bmp"
        };

        for (String contentType : invalidContentTypes) {
            when(mockMultipartFile.isEmpty()).thenReturn(false);
            when(mockMultipartFile.getContentType()).thenReturn(contentType);
            barangForm.setFileGambar(mockMultipartFile);

            // Act
            boolean result = barangForm.isValidImageFormat();

            // Assert
            assertFalse(result, "Should return false for content type: " + contentType);
        }
    }

    @Test
    @DisplayName("Validation error ketika namaBarang kosong")
    void validation_error_ketika_namaBarang_kosong() {
        // Arrange
        barangForm.setNamaBarang("");
        barangForm.setKategori("Sepatu");
        barangForm.setTanggalMasuk(LocalDateTime.now());

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Nama barang harus diisi")));
    }

    @Test
    @DisplayName("Validation error ketika namaBarang null")
    void validation_error_ketika_namaBarang_null() {
        // Arrange
        barangForm.setNamaBarang(null);
        barangForm.setKategori("Sepatu");
        barangForm.setTanggalMasuk(LocalDateTime.now());

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Nama barang harus diisi")));
    }

    @Test
    @DisplayName("Validation error ketika kategori kosong")
    void validation_error_ketika_kategori_kosong() {
        // Arrange
        barangForm.setNamaBarang("Nike Air Max");
        barangForm.setKategori("");
        barangForm.setTanggalMasuk(LocalDateTime.now());

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Kategori harus diisi")));
    }

    @Test
    @DisplayName("Validation error ketika tanggalMasuk null")
    void validation_error_ketika_tanggalMasuk_null() {
        // Arrange
        barangForm.setNamaBarang("Nike Air Max");
        barangForm.setKategori("Sepatu");
        barangForm.setTanggalMasuk(null);

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Tanggal restock harus diisi")));
    }

    @Test
    @DisplayName("Validation success untuk form yang lengkap dan valid")
    void validation_success_untuk_form_yang_lengkap_dan_valid() {
        // Arrange
        barangForm.setNamaBarang("Nike Air Max 97");
        barangForm.setKategori("Sepatu");
        barangForm.setDeskripsi("Size 42, kondisi 90%");
        barangForm.setTanggalMasuk(LocalDateTime.now());
        barangForm.setStatus("READY");

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Integration test - form create barang baru tanpa foto")
    void integration_test_form_create_barang_baru_tanpa_foto() {
        // Arrange
        barangForm.setNamaBarang("Adidas Ultraboost");
        barangForm.setKategori("Sepatu");
        barangForm.setDeskripsi("Size 43, kondisi 85%");
        barangForm.setTanggalMasuk(LocalDateTime.of(2024, 12, 10, 14, 0));

        // Act & Assert
        assertEquals("Adidas Ultraboost", barangForm.getNamaBarang());
        assertEquals("Sepatu", barangForm.getKategori());
        assertEquals("READY", barangForm.getStatus());
        assertFalse(barangForm.hasImage());
        assertTrue(barangForm.isValidImageFormat()); // Valid karena tidak wajib
        assertTrue(validator.validate(barangForm).isEmpty());
    }

    @Test
    @DisplayName("Integration test - form create barang baru dengan foto valid")
    void integration_test_form_create_barang_baru_dengan_foto_valid() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getOriginalFilename()).thenReturn("nike-airmax.jpg");

        barangForm.setNamaBarang("Nike Air Max 97");
        barangForm.setKategori("Sepatu");
        barangForm.setDeskripsi("Size 42, kondisi 95%, box original");
        barangForm.setTanggalMasuk(LocalDateTime.now());
        barangForm.setFileGambar(mockMultipartFile);

        // Act & Assert
        assertTrue(barangForm.hasImage());
        assertTrue(barangForm.isValidImageFormat());
        assertTrue(validator.validate(barangForm).isEmpty());
    }

    @Test
    @DisplayName("Integration test - form edit barang dengan ganti foto")
    void integration_test_form_edit_barang_dengan_ganti_foto() {
        // Arrange
        UUID barangId = UUID.randomUUID();
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/png");

        barangForm.setId(barangId);
        barangForm.setNamaBarang("Nike Air Max 97 Updated");
        barangForm.setKategori("Sepatu");
        barangForm.setDeskripsi("Size 42, kondisi 98%");
        barangForm.setTanggalMasuk(LocalDateTime.now());
        barangForm.setExistingFoto("old-photo.jpg");
        barangForm.setFileGambar(mockMultipartFile); // Foto baru

        // Act & Assert
        assertNotNull(barangForm.getId());
        assertEquals("old-photo.jpg", barangForm.getExistingFoto());
        assertTrue(barangForm.hasImage());
        assertTrue(barangForm.isValidImageFormat());
    }

    @Test
    @DisplayName("Integration test - form ubah status dari READY ke SOLD")
    void integration_test_form_ubah_status_dari_ready_ke_sold() {
        // Arrange
        barangForm.setId(UUID.randomUUID());
        barangForm.setNamaBarang("Converse Classic");
        barangForm.setKategori("Sepatu");
        barangForm.setTanggalMasuk(LocalDateTime.now());
        barangForm.setStatus("READY");

        // Act
        barangForm.setStatus("SOLD");

        // Assert
        assertEquals("SOLD", barangForm.getStatus());
    }

    @Test
    @DisplayName("Integration test - form dengan kategori Jaket")
    void integration_test_form_dengan_kategori_jaket() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/webp");

        barangForm.setNamaBarang("Jaket Bomber Army");
        barangForm.setKategori("Jaket");
        barangForm.setDeskripsi("Size L, kondisi 88%, warna hijau army");
        barangForm.setTanggalMasuk(LocalDateTime.of(2024, 12, 11, 9, 0));
        barangForm.setFileGambar(mockMultipartFile);

        // Act & Assert
        assertEquals("Jaket", barangForm.getKategori());
        assertTrue(barangForm.hasImage());
        assertTrue(barangForm.isValidImageFormat());
        assertTrue(validator.validate(barangForm).isEmpty());
    }

    @Test
    @DisplayName("Edge case - deskripsi null tetap valid")
    void edge_case_deskripsi_null_tetap_valid() {
        // Arrange
        barangForm.setNamaBarang("Nike Air Max");
        barangForm.setKategori("Sepatu");
        barangForm.setDeskripsi(null); // Deskripsi boleh null
        barangForm.setTanggalMasuk(LocalDateTime.now());

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertTrue(violations.isEmpty()); // Deskripsi tidak wajib
    }

    @Test
    @DisplayName("Edge case - multiple validation errors")
    void edge_case_multiple_validation_errors() {
        // Arrange
        barangForm.setNamaBarang(null);
        barangForm.setKategori("");
        barangForm.setTanggalMasuk(null);

        // Act
        Set<ConstraintViolation<BarangForm>> violations = validator.validate(barangForm);

        // Assert
        assertEquals(3, violations.size()); // 3 field error
    }
}