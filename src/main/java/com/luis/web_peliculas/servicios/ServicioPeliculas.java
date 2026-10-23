package com.luis.web_peliculas.servicios;

import com.luis.web_peliculas.model.Pelicula;

import java.util.List;

public interface ServicioPeliculas {
    List<Pelicula> obtenerPeliculas(String busqueda, String genero, int pagina, int tamano);
    long contarPeliculas(String busqueda, String genero);
    List<String> obtenerGeneros();
    Pelicula obtenerPeliculaPorId(long id);
    Pelicula registrarPelicula(Pelicula pelicula);
    Pelicula guardarCambiosPelicula(long id, Pelicula pelicula);
    void borrarPelicula(long id);
}
