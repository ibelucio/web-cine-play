package com.luis.web_peliculas.serviciosImpl;

import com.luis.web_peliculas.model.Pelicula;
import com.luis.web_peliculas.servicios.ServicioPeliculas;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ServiciosPeliculaImplJPA implements ServicioPeliculas {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Pelicula> obtenerPeliculas(String busqueda, String genero, int pagina, int tamano) {
        String termino = normalizarFiltro(busqueda);
        String filtroGenero = normalizarFiltro(genero);

        StringBuilder consulta = new StringBuilder("select p from Pelicula p where 1 = 1");
        if (termino != null) {
            consulta.append(" and lower(p.titulo) like :patron");
        }
        if (filtroGenero != null) {
            consulta.append(" and lower(p.genero) = :genero");
        }
        consulta.append(" order by p.titulo");

        var query = entityManager.createQuery(consulta.toString(), Pelicula.class);

        if (termino != null) {
            query.setParameter("patron", "%" + termino + "%");
        }
        if (filtroGenero != null) {
            query.setParameter("genero", filtroGenero);
        }

        int paginaSegura = Math.max(1, pagina);
        int tamanoSeguro = Math.max(1, Math.min(100, tamano));
        int primerResultado = (int) Math.min((long) (paginaSegura - 1) * tamanoSeguro, Integer.MAX_VALUE);
        query.setFirstResult(primerResultado);
        query.setMaxResults(tamanoSeguro);

        return query.getResultList();
    }

    @Override
    public long contarPeliculas(String busqueda, String genero) {
        String termino = normalizarFiltro(busqueda);
        String filtroGenero = normalizarFiltro(genero);
        StringBuilder consulta = new StringBuilder("select count(p) from Pelicula p where 1 = 1");
        if (termino != null) {
            consulta.append(" and lower(p.titulo) like :patron");
        }
        if (filtroGenero != null) {
            consulta.append(" and lower(p.genero) = :genero");
        }

        var query = entityManager.createQuery(consulta.toString(), Long.class);
        if (termino != null) {
            query.setParameter("patron", "%" + termino + "%");
        }
        if (filtroGenero != null) {
            query.setParameter("genero", filtroGenero);
        }
        return query.getSingleResult();
    }

    @Override
    public List<String> obtenerGeneros() {
        return entityManager.createQuery("select distinct p.genero from Pelicula p order by p.genero", String.class)
                .getResultList();
    }

    @Override
    public Pelicula obtenerPeliculaPorId(long id) {
        if (id < 1 || id > Integer.MAX_VALUE) {
            return null;
        }
        return entityManager.find(Pelicula.class, (int) id);
    }

    @Override
    public Pelicula registrarPelicula(Pelicula pelicula) {
        guardarImagen(pelicula);
        entityManager.persist(pelicula);
        return pelicula;
    }

    @Override
    public Pelicula guardarCambiosPelicula(long id, Pelicula pelicula) {
        Pelicula existente = obtenerPeliculaPorId(id);
        if (existente == null) {
            return null;
        }
        existente.setTitulo(pelicula.getTitulo());
        existente.setGenero(pelicula.getGenero());
        existente.setAnio(pelicula.getAnio());
        existente.setLink(pelicula.getLink());
        if (pelicula.getImagenSubida() != null && !pelicula.getImagenSubida().isEmpty()) {
            guardarImagen(pelicula);
            existente.setImagenPortada(pelicula.getImagenPortada());
            existente.setTipoImagen(pelicula.getTipoImagen());
        }
        return existente;
    }

    @Override
    public void borrarPelicula(long id) {
        Pelicula pelicula = obtenerPeliculaPorId(id);
        if (pelicula != null) {
            entityManager.remove(pelicula);
        }
    }

    private void guardarImagen(Pelicula pelicula) {
        MultipartFile archivo = pelicula.getImagenSubida();
        if (archivo == null || archivo.isEmpty()) {
            return;
        }
        if (archivo.getContentType() == null || !archivo.getContentType().toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("La portada debe ser un archivo de imagen");
        }
        try {
            pelicula.setImagenPortada(archivo.getBytes());
            pelicula.setTipoImagen(archivo.getContentType());
        } catch (IOException exception) {
            throw new UncheckedIOException("No se pudo leer la imagen de portada", exception);
        }
    }

    private String normalizarFiltro(String filtro) {
        return filtro == null || filtro.isBlank() ? null : filtro.trim().toLowerCase(Locale.ROOT);
    }
}
