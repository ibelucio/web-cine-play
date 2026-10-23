package com.luis.web_peliculas.controladores;

import com.luis.web_peliculas.model.Pelicula;
import com.luis.web_peliculas.servicios.AlmacenPeliculas;
import com.luis.web_peliculas.servicios.ServicioPeliculas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculasREST {
    private final ServicioPeliculas servicioPeliculas;
    private final AlmacenPeliculas almacenPeliculas;

    public PeliculasREST(ServicioPeliculas servicioPeliculas, AlmacenPeliculas almacenPeliculas) {
        this.servicioPeliculas = servicioPeliculas;
        this.almacenPeliculas = almacenPeliculas;
    }

    @GetMapping
    public PaginaPeliculas obtenerPeliculas(
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "") String genero,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            HttpServletRequest request) {
        String contexto = request.getContextPath();
        int paginaSolicitada = Math.max(1, pagina);
        int tamanoSeguro = Math.max(1, Math.min(100, tamano));
        long total = servicioPeliculas.contarPeliculas(busqueda, genero);
        long totalPaginas = Math.max(1, total / tamanoSeguro + (total % tamanoSeguro == 0 ? 0 : 1));
        int paginaSegura = (int) Math.min(paginaSolicitada, totalPaginas);
        List<PeliculaResumen> peliculas = servicioPeliculas.obtenerPeliculas(busqueda, genero, paginaSegura, tamanoSeguro)
                .stream()
                .map(pelicula -> {
                    String rutaPublica = almacenPeliculas.obtenerRutaPublica(pelicula.getLink());
                    String link = rutaPublica == null ? null : contexto + rutaPublica;
                    String imagenUrl = pelicula.getImagenPortada() == null
                            ? contexto + "/assets/iconpel.png"
                            : contexto + "/api/peliculas/" + pelicula.getId() + "/portada";
                    return new PeliculaResumen(pelicula.getId(), pelicula.getTitulo(), pelicula.getGenero(),
                            pelicula.getAnio(), link, imagenUrl);
                })
                .toList();
        return new PaginaPeliculas(peliculas, total, paginaSegura, tamanoSeguro);
    }

    @GetMapping(value = "/{id}/portada")
    public ResponseEntity<byte[]> obtenerPortada(@PathVariable long id) {
        Pelicula pelicula = servicioPeliculas.obtenerPeliculaPorId(id);
        if (pelicula == null || pelicula.getImagenPortada() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = MediaType.parseMediaType(pelicula.getTipoImagen());
        return ResponseEntity.ok().contentType(tipo).body(pelicula.getImagenPortada());
    }

    public record PeliculaResumen(Integer id, String titulo, String genero, Integer anio, String link, String imagenUrl) {}
    public record PaginaPeliculas(List<PeliculaResumen> peliculas, long total, int pagina, int tamano) {}
}
