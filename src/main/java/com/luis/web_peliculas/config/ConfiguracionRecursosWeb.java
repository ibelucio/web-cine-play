package com.luis.web_peliculas.config;

import com.luis.web_peliculas.servicios.AlmacenPeliculas;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionRecursosWeb implements WebMvcConfigurer {
    private final AlmacenPeliculas almacenPeliculas;

    public ConfiguracionRecursosWeb(AlmacenPeliculas almacenPeliculas) {
        this.almacenPeliculas = almacenPeliculas;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = almacenPeliculas.getDirectorio().toUri().toString();
        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }
        registry.addResourceHandler("/peliculas/**")
                .addResourceLocations(ubicacion);
    }
}
