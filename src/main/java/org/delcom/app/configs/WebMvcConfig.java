package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    // 1. PENTING: Konfigurasi agar folder 'uploads' bisa diakses browser
    // Ini wajib agar gambar tugas bisa muncul di HTML
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Artinya: Setiap URL yang diawali "/uploads/..." 
        // akan diarahkan untuk mengambil file di folder lokal "./uploads/"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
                
        // Tambahan: Pastikan static files (CSS/JS) default tetap jalan
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    // 2. Konfigurasi Interceptor (Keamanan)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // Terapkan keamanan hanya pada API endpoint
                .addPathPatterns("/api/**") 
                // Kecuali endpoint login/register
                .excludePathPatterns("/api/auth/**")
                // Dan biarkan folder uploads diakses tanpa token via interceptor ini
                // (Meskipun logic di AuthInterceptor sudah handle, exclude disini lebih aman)
                .excludePathPatterns("/uploads/**"); 
    }
}