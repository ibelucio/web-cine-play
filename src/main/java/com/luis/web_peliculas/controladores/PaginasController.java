package com.luis.web_peliculas.controladores;

import com.luis.web_peliculas.model.Pelicula;
import com.luis.web_peliculas.servicios.AlmacenPeliculas;
import com.luis.web_peliculas.servicios.ServicioPeliculas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Locale;

@Controller
public class PaginasController {
    private final ServicioPeliculas servicioPeliculas;
    private final AlmacenPeliculas almacenPeliculas;

    public PaginasController(ServicioPeliculas servicioPeliculas, AlmacenPeliculas almacenPeliculas) {
        this.servicioPeliculas = servicioPeliculas;
        this.almacenPeliculas = almacenPeliculas;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/pelicula/{id}")
    public String reproducir(@PathVariable long id, Model model, HttpServletRequest request) {
        Pelicula pelicula = servicioPeliculas.obtenerPeliculaPorId(id);
        if (pelicula == null) {
            return "redirect:/";
        }
        pelicula.setLink(request.getContextPath() + almacenPeliculas.obtenerRutaPublica(pelicula.getLink()));
        model.addAttribute("pelicula", pelicula);
        model.addAttribute("tipoVideo", tipoVideo(pelicula.getLink()));
        return "reproductor";
    }

    private String tipoVideo(String ruta) {
        String extension = ruta.substring(ruta.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "webm" -> "video/webm";
            case "ogv", "ogg" -> "video/ogg";
            default -> "video/mp4";
        };
    }
}
