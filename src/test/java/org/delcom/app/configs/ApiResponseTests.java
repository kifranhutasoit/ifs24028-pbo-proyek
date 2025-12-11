package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ApiResponseTests {
    @Test
    @DisplayName("Menggunakan konstruktor ApiResponse dengan benar")
    void testMenggunakanKonstruktorApiResponse() throws Exception {
        ApiResponse<String> response = new ApiResponse<>("success", "Operasi berhasil", "Data hasil");

        assert (response.getStatus().equals("success"));
        assert (response.getMessage().equals("Operasi berhasil"));
        assert (response.getData().equals("Data hasil"));
    }
}
