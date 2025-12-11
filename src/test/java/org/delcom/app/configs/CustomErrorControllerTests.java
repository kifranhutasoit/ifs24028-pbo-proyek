package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomErrorControllerTest {

        @Test
        @DisplayName("Mengembalikan response error dengan status 500")
        void testHandleErrorReturns500() throws Exception {
                Map<String, Object> errorMap = Map.of();

                ErrorAttributes errorAttributes = Mockito.mock(ErrorAttributes.class);

                Mockito.when(
                                errorAttributes.getErrorAttributes(
                                                any(ServletWebRequest.class),
                                                any(ErrorAttributeOptions.class)))
                                .thenReturn(errorMap);

                CustomErrorController controller = new CustomErrorController(errorAttributes);

                HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
                HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
                ServletWebRequest webRequest = new ServletWebRequest(request, response);

                ResponseEntity<Map<String, Object>> result = controller.handleError(webRequest);

                assertEquals(500, result.getStatusCode().value());
                assertEquals("error", result.getBody().get("status"));
                assertEquals("Unknown Error", result.getBody().get("error"));
                assertEquals("unknown", result.getBody().get("path"));
        }

        @Test
        @DisplayName("Mengembalikan response error dengan status 404")
        void testHandleErrorReturns404() throws Exception {
                Map<String, Object> errorMap = Map.of(
                                "status", 404,
                                "error", "Not Found",
                                "path", "/error404");

                ErrorAttributes errorAttributes = Mockito.mock(ErrorAttributes.class);

                Mockito.when(
                                errorAttributes.getErrorAttributes(
                                                any(ServletWebRequest.class),
                                                any(ErrorAttributeOptions.class)))
                                .thenReturn(errorMap);

                CustomErrorController controller = new CustomErrorController(errorAttributes);

                // buat dummy request/response
                HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
                HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
                ServletWebRequest webRequest = new ServletWebRequest(request, response);

                ResponseEntity<Map<String, Object>> result = controller.handleError(webRequest);

                assertEquals(404, result.getStatusCode().value());
                assertEquals("fail", result.getBody().get("status"));
                assertEquals("Not Found", result.getBody().get("error"));
                assertEquals("/error404", result.getBody().get("path"));
        }
}
