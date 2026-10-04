package com.firmaya.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SesionInterceptor sesionInterceptor;

    public WebConfig(SesionInterceptor sesionInterceptor) {
        this.sesionInterceptor = sesionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rutas públicas: autenticación (CU-19, CU-21) y acceso externo por token (CU-04, CU-08)
        registry.addInterceptor(sesionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/externo/**");
    }

    // Las contraseñas se guardan como hash BCrypt en USUARIO.contrasenia_hash
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
