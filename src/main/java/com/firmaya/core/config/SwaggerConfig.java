package com.firmaya.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Documentación de la API (Swagger UI en /swagger-ui.html).
 * El botón "Authorize" recibe el token devuelto por POST /api/auth/login.
 */
@Configuration
public class SwaggerConfig {

    private static final String ESQUEMA_SESION = "sesion";

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FirmaYA API")
                        .description("Endpoints de los casos de uso CU-01, CU-02, CU-03, CU-05, CU-06, CU-15, CU-16, CU-18, CU-19 y CU-21. "
                                + "Las rutas /api/auth/** y /api/externo/** son públicas; "
                                + "el resto requiere el encabezado Authorization: Bearer {token}.")
                        .version("1.0"))
                .components(new Components().addSecuritySchemes(ESQUEMA_SESION, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description("Token de sesión obtenido en POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SESION));
    }
}
