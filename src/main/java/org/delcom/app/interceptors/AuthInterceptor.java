    package org.delcom.app.interceptors;

    import java.util.UUID;

    import org.delcom.app.configs.AuthContext;
    import org.delcom.app.entities.AuthToken;
    import org.delcom.app.entities.User;
    import org.delcom.app.services.AuthTokenService;
    import org.delcom.app.services.UserService;
    import org.delcom.app.utils.JwtUtil;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Component;
    import org.springframework.web.servlet.HandlerInterceptor;

    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;

    @Component
    public class AuthInterceptor implements HandlerInterceptor {

        @Autowired
        protected AuthContext authContext;

        @Autowired
        protected AuthTokenService authTokenService;

        @Autowired
        protected UserService userService;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            
            // 1. Lewati pengecekan untuk endpoint public (Login, Register, Error, Uploads, Static files)
            if (isPublicEndpoint(request)) {
                return true;
            }

            // 2. Ambil bearer token dari header
            String rawAuthToken = request.getHeader("Authorization");
            String token = extractToken(rawAuthToken);

            // 3. Validasi keberadaan token
            if (token == null || token.isEmpty()) {
                // Jika request bukan ke API (misal ke halaman HTML view), redirect ke login page
                // Logic ini opsional, tergantung apakah interceptor ini mencakup semua URL
                if (!isApiRequest(request)) {
                    // response.sendRedirect("/auth/login"); // Uncomment jika ingin auto redirect
                    // return false; 
                }
                
                sendErrorResponse(response, 401, "Token autentikasi tidak ditemukan");
                return false;
            }

            // 4. Validasi format token JWT
            if (!JwtUtil.validateToken(token, true)) {
                sendErrorResponse(response, 401, "Token autentikasi tidak valid");
                return false;
            }

            // 5. Ekstrak userId dari token
            UUID userId = JwtUtil.extractUserId(token);
            if (userId == null) {
                sendErrorResponse(response, 401, "Format token autentikasi tidak valid");
                return false;
            }

            // 6. Cari token di database (memastikan user belum logout)
            AuthToken authToken = authTokenService.findUserToken(userId, token);
            if (authToken == null) {
                sendErrorResponse(response, 401, "Token autentikasi sudah expired");
                return false;
            }

            // 7. Ambil data user
            User authUser = userService.getUserById(authToken.getUserId());
            if (authUser == null) {
                sendErrorResponse(response, 404, "User tidak ditemukan");
                return false;
            }

            // 8. Set user ke auth context agar bisa dipakai di Controller
            authContext.setAuthUser(authUser);
            return true;
        }

        private String extractToken(String rawAuthToken) {
            if (rawAuthToken != null && rawAuthToken.startsWith("Bearer ")) {
                return rawAuthToken.substring(7); // hapus "Bearer "
            }
            return null;
        }

        private boolean isPublicEndpoint(HttpServletRequest request) {
            String path = request.getRequestURI();
            
            // DAFTAR ENDPOINT YANG BISA DIAKSES TANPA LOGIN:
            return path.startsWith("/api/auth") ||   // Login & Register API
                path.startsWith("/auth") ||       // Halaman Login/Register View
                path.equals("/error") ||          // Halaman Error default Spring
                path.startsWith("/uploads") ||    // AKSES GAMBAR (Penting!)
                path.startsWith("/css") ||        // Style CSS (jika ada)
                path.startsWith("/js") ||         // Javascript (jika ada)
                path.startsWith("/images");       // Aset gambar statis
        }

        private boolean isApiRequest(HttpServletRequest request) {
            return request.getRequestURI().startsWith("/api/");
        }

        private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = String.format(
                    "{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}",
                    message);
            response.getWriter().write(jsonResponse);
        }
    }