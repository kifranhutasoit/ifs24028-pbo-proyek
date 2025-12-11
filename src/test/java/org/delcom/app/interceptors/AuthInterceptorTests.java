package org.delcom.app.interceptors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.stream.Stream;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthInterceptorTests {

    @Mock private AuthTokenService authTokenService;
    @Mock private UserService userService;
    @Mock private AuthContext authContext;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        // Gunakan lenient agar tidak error jika test case tertentu tidak memanggil getWriter
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // =========================================================================
    // 1. HAPPY PATH (SUKSES)
    // =========================================================================
    @Test
    @DisplayName("100% Path: Token Valid, User Found -> Return True")
    void testHappyPath() throws Exception {
        String tokenRaw = "Bearer token_valid_abc";
        String tokenExtracted = "token_valid_abc";
        UUID userId = UUID.randomUUID();
        
        AuthToken mockAuthToken = new AuthToken(); 
        mockAuthToken.setUserId(userId);
        User mockUser = new User();
        mockUser.setId(userId);

        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getHeader("Authorization")).thenReturn(tokenRaw);

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), anyBoolean())).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(userId);

            when(authTokenService.findUserToken(userId, tokenExtracted)).thenReturn(mockAuthToken);
            when(userService.getUserById(userId)).thenReturn(mockUser);

            boolean result = authInterceptor.preHandle(request, response, null);

            assertTrue(result);
            verify(authContext).setAuthUser(mockUser);
        }
    }

    // =========================================================================
    // 2. PUBLIC ENDPOINTS (Line 35 & 100-105)
    // =========================================================================
    static Stream<String> publicPathProvider() {
        return Stream.of(
            "/api/auth/login",
            "/auth/register",
            "/error", // equals exact check
            "/uploads/avatar.png",
            "/css/style.css",
            "/js/script.js",
            "/images/logo.svg"
        );
    }

    @ParameterizedTest
    @MethodSource("publicPathProvider")
    @DisplayName("Public Endpoints should return true directly")
    void testPublicEndpoints(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(path);
        boolean result = authInterceptor.preHandle(request, response, null);
        assertTrue(result);
    }

    // =========================================================================
    // 3. COVERAGE FOR TOKEN NULL / EMPTY (Line 44 & 47)
    // =========================================================================
    
    @Test
    @DisplayName("Token NULL (Header missing) on API Request")
    void testTokenNull_ApiRequest() throws Exception {
        // Case: Header Authorization null
        when(request.getRequestURI()).thenReturn("/api/data"); // API Request
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(401);
        assertTrue(responseWriter.toString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("Token NULL on NON-API Request (Coverage for Line 47 !isApiRequest)")
    void testTokenMissing_NonApiRequest() throws Exception {
        // Case: Request ke halaman HTML biasa (bukan /api/), token tidak ada
        // Ini akan masuk ke blok if (!isApiRequest(request)) di baris 47
        when(request.getRequestURI()).thenReturn("/dashboard"); 
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    @DisplayName("Token EMPTY STRING (Coverage for Line 44 token.isEmpty())")
    void testTokenEmptyString() throws Exception {
        // Case: Header "Bearer " tanpa token di belakangnya.
        // substring(7) akan menghasilkan string kosong "".
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer "); 

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(401);
        assertTrue(responseWriter.toString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("Header Format Wrong (No Bearer)")
    void testInvalidHeaderFormat() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Basic 12345"); 

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(401);
    }

    // =========================================================================
    // 4. JWT VALIDATION & EXTRACTION (Line 57 & 65)
    // =========================================================================
    @Test
    @DisplayName("JwtUtil.validateToken returns false")
    void testInvalidJwtSignature() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_rusak");

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), anyBoolean())).thenReturn(false);

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            verify(response).setStatus(401);
            assertTrue(responseWriter.toString().contains("Token autentikasi tidak valid"));
        }
    }

    @Test
    @DisplayName("JwtUtil.extractUserId returns null")
    void testExtractUserIdNull() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valid");

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), anyBoolean())).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            verify(response).setStatus(401);
            assertTrue(responseWriter.toString().contains("Format token autentikasi tidak valid"));
        }
    }

    // =========================================================================
    // 5. DB CHECKS (Line 72 & 79)
    // =========================================================================
    @Test
    @DisplayName("Token not found in Database")
    void testTokenNotInDB() throws Exception {
        UUID uid = UUID.randomUUID();
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valid");

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), anyBoolean())).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(uid);
            
            when(authTokenService.findUserToken(any(UUID.class), anyString())).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertTrue(responseWriter.toString().contains("Token autentikasi sudah expired"));
        }
    }

    @Test
    @DisplayName("User not found in Database")
    void testUserNotFound() throws Exception {
        UUID uid = UUID.randomUUID();
        AuthToken mockAuthToken = new AuthToken();
        mockAuthToken.setUserId(uid);

        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_valid");

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(anyString(), anyBoolean())).thenReturn(true);
            mockedJwt.when(() -> JwtUtil.extractUserId(anyString())).thenReturn(uid);
            
            when(authTokenService.findUserToken(any(UUID.class), anyString())).thenReturn(mockAuthToken);
            when(userService.getUserById(uid)).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            verify(response).setStatus(404);
            assertTrue(responseWriter.toString().contains("User tidak ditemukan"));
        }
    }
}