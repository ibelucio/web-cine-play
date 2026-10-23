package com.luis.web_peliculas.servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AlmacenPeliculas {
    private static final String PREFIJO_PUBLICO = "/peliculas/";

    private final Path directorio;

    public AlmacenPeliculas(@Value("${app.peliculas.directorio:/srv/peliculas}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public Path getDirectorio() {
        return directorio;
    }

    public String obtenerNombreArchivo(String rutaOArchivo) {
        if (rutaOArchivo == null) {
            return null;
        }
        String nombre = rutaOArchivo.trim();
        if (nombre.startsWith(PREFIJO_PUBLICO)) {
            nombre = nombre.substring(PREFIJO_PUBLICO.length());
        }
        if (nombre.isBlank() || nombre.equals(".") || nombre.equals("..")
                || nombre.contains("/") || nombre.contains("\\") || nombre.contains("%")
                || nombre.contains("?") || nombre.contains("#")) {
            return null;
        }
        return nombre;
    }

    public boolean existeArchivo(String nombre) {
        String nombreSeguro = obtenerNombreArchivo(nombre);
        if (nombreSeguro == null) {
            return false;
        }
        Path archivo = directorio.resolve(nombreSeguro).normalize();
        if (!archivo.startsWith(directorio) || !Files.isRegularFile(archivo)) {
            return false;
        }
        try {
            return archivo.toRealPath().startsWith(directorio.toRealPath());
        } catch (IOException e) {
            return false;
        }
    }

    public String obtenerRutaPublica(String nombre) {
        String nombreSeguro = obtenerNombreArchivo(nombre);
        return nombreSeguro == null ? null : PREFIJO_PUBLICO + nombreSeguro;
    }
}
