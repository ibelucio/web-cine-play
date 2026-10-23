package com.luis.web_peliculas.controladores;

import com.luis.web_peliculas.model.Pelicula;
import com.luis.web_peliculas.servicios.AlmacenPeliculas;
import com.luis.web_peliculas.servicios.ServicioPeliculas;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Controller
public class AdminController {
    private static final int TAMANO_PAGINA = 12;
    private static final String SESION_ADMIN = "adminAutenticado";
    private static final String SESION_CSRF = "tokenCsrf";

    private final ServicioPeliculas servicioPeliculas;
    private final AlmacenPeliculas almacenPeliculas;
    private final String usuarioAdmin;
    private final String claveAdmin;

    public AdminController(ServicioPeliculas servicioPeliculas,
                           AlmacenPeliculas almacenPeliculas,
                           @Value("${app.admin.username:admin}") String usuarioAdmin,
                           @Value("${app.admin.password}") String claveAdmin) {
        this.servicioPeliculas = servicioPeliculas;
        this.almacenPeliculas = almacenPeliculas;
        this.usuarioAdmin = usuarioAdmin;
        this.claveAdmin = claveAdmin;
    }

    @GetMapping("/admin")
    public String accederLogin(HttpSession session) {
        if (estaAutenticado(session)) {
            return "redirect:/admin/peliculas";
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/login")
    public String login(HttpSession session, Model model) {
        if (estaAutenticado(session)) {
            return "redirect:/admin/peliculas";
        }
        model.addAttribute("csrfToken", obtenerToken(session));
        return "login";
    }

    @PostMapping("/admin/login")
    public String autenticar(@RequestParam String username, @RequestParam String password,
                             @RequestParam("_csrf") String token,
                             HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (!tokenValido(session, token)) {
            return loginInvalido(session, model, "La solicitud expiró. Intenta iniciar sesión de nuevo.");
        }
        if (!comparar(username, usuarioAdmin) || !comparar(password, claveAdmin)) {
            return loginInvalido(session, model, "Usuario o contraseña incorrectos.");
        }
        request.changeSessionId();
        session = request.getSession();
        session.setAttribute(SESION_ADMIN, true);
        session.setAttribute(SESION_CSRF, UUID.randomUUID().toString());
        return "redirect:/admin/peliculas";
    }

    @GetMapping("/admin/peliculas")
    public String catalogo(@RequestParam(defaultValue = "") String busqueda,
                           @RequestParam(defaultValue = "") String genero,
                           @RequestParam(defaultValue = "1") int pagina,
                           HttpSession session, Model model) {
        if (!estaAutenticado(session)) {
            return "redirect:/admin";
        }
        cargarCatalogo(model, new Pelicula(), busqueda, genero, pagina, session);
        return "admin";
    }

    @GetMapping("/admin/peliculas/{id}/editar")
    public String editar(@PathVariable long id, @RequestParam(defaultValue = "") String busqueda,
                         @RequestParam(defaultValue = "") String genero,
                         @RequestParam(defaultValue = "1") int pagina,
                         HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!estaAutenticado(session)) {
            return "redirect:/admin";
        }
        Pelicula pelicula = servicioPeliculas.obtenerPeliculaPorId(id);
        if (pelicula == null) {
            redirectAttributes.addFlashAttribute("mensaje", "No se encontró la película.");
            return "redirect:/admin/peliculas";
        }
        pelicula.setLink(almacenPeliculas.obtenerNombreArchivo(pelicula.getLink()));
        cargarCatalogo(model, pelicula, busqueda, genero, pagina, session);
        return "admin";
    }

    @PostMapping("/admin/peliculas/guardar")
    public String guardar(@Valid @ModelAttribute("pelicula") Pelicula pelicula, BindingResult errores,
                          @RequestParam("_csrf") String token,
                          @RequestParam(defaultValue = "") String busqueda,
                          @RequestParam(name = "filtroGenero", defaultValue = "") String filtroGenero,
                          @RequestParam(defaultValue = "1") int pagina,
                          HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!estaAutenticado(session)) {
            return "redirect:/admin";
        }
        if (!tokenValido(session, token)) {
            redirectAttributes.addFlashAttribute("mensaje", "La sesión expiró. Vuelve a intentarlo.");
            return "redirect:/admin/peliculas";
        }
        String nombreArchivo = almacenPeliculas.obtenerNombreArchivo(pelicula.getLink());
        if (nombreArchivo == null || !almacenPeliculas.existeArchivo(nombreArchivo)) {
            errores.rejectValue("link", "pelicula.link.invalido",
                    "Escribe el nombre de un archivo existente en /srv/peliculas, por ejemplo mi-pelicula.mp4.");
        } else {
            pelicula.setLink(nombreArchivo);
        }
        if (pelicula.getImagenSubida() != null && !pelicula.getImagenSubida().isEmpty()) {
            String tipo = pelicula.getImagenSubida().getContentType();
            if (tipo == null || !tipo.toLowerCase().startsWith("image/")
                    || pelicula.getImagenSubida().getSize() > 5 * 1024 * 1024) {
                errores.rejectValue("imagenSubida", "pelicula.imagen.invalida",
                        "La portada debe ser una imagen de hasta 5 MB.");
            }
        }
        if (errores.hasErrors()) {
            cargarCatalogo(model, pelicula, busqueda, filtroGenero, pagina, session);
            model.addAttribute("mostrarFormulario", true);
            return "admin";
        }

        Pelicula guardada;
        if (pelicula.getId() == null) {
            guardada = servicioPeliculas.registrarPelicula(pelicula);
        } else {
            guardada = servicioPeliculas.guardarCambiosPelicula(pelicula.getId(), pelicula);
        }
        if (guardada == null) {
            redirectAttributes.addFlashAttribute("mensaje", "No se encontró la película que intentas editar.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Película guardada correctamente.");
        }
        return "redirect:/admin/peliculas";
    }

    @PostMapping("/admin/peliculas/{id}/borrar")
    public String borrar(@PathVariable long id, @RequestParam("_csrf") String token,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        if (!estaAutenticado(session)) {
            return "redirect:/admin";
        }
        if (!tokenValido(session, token)) {
            redirectAttributes.addFlashAttribute("mensaje", "La sesión expiró. Vuelve a intentarlo.");
            return "redirect:/admin/peliculas";
        }
        servicioPeliculas.borrarPelicula(id);
        redirectAttributes.addFlashAttribute("mensaje", "Película eliminada.");
        return "redirect:/admin/peliculas";
    }

    @PostMapping("/admin/logout")
    public String cerrarSesion(@RequestParam("_csrf") String token, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (estaAutenticado(session) && tokenValido(session, token)) {
            session.invalidate();
        }
        return "redirect:/admin";
    }

    private String loginInvalido(HttpSession session, Model model, String mensaje) {
        model.addAttribute("csrfToken", obtenerToken(session));
        model.addAttribute("error", mensaje);
        return "login";
    }

    private void cargarCatalogo(Model model, Pelicula pelicula, String busqueda, String genero,
                                int pagina, HttpSession session) {
        long total = servicioPeliculas.contarPeliculas(busqueda, genero);
        int paginas = Math.max(1, (int) Math.ceil((double) total / TAMANO_PAGINA));
        int paginaSegura = Math.max(1, Math.min(pagina, paginas));
        model.addAttribute("peliculas", servicioPeliculas.obtenerPeliculas(busqueda, genero, paginaSegura, TAMANO_PAGINA));
        model.addAttribute("generos", servicioPeliculas.obtenerGeneros());
        model.addAttribute("pelicula", pelicula);
        model.addAttribute("mostrarFormulario", pelicula.getId() != null);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("generoSeleccionado", genero);
        model.addAttribute("pagina", paginaSegura);
        model.addAttribute("totalPaginas", paginas);
        model.addAttribute("csrfToken", obtenerToken(session));
    }

    private boolean estaAutenticado(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(SESION_ADMIN));
    }

    private String obtenerToken(HttpSession session) {
        Object token = session.getAttribute(SESION_CSRF);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SESION_CSRF, token);
        }
        return token.toString();
    }

    private boolean tokenValido(HttpSession session, String token) {
        if (session == null || token == null) {
            return false;
        }
        Object esperado = session.getAttribute(SESION_CSRF);
        return esperado instanceof String && comparar(token, (String) esperado);
    }

    private boolean comparar(String recibido, String esperado) {
        if (recibido == null || esperado == null) {
            return false;
        }
        return MessageDigest.isEqual(recibido.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8));
    }

}
