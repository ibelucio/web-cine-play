package com.luis.web_peliculas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.luis.web_peliculas.model.Pelicula;
import com.luis.web_peliculas.servicios.ServicioPeliculas;
import org.springframework.http.MediaType;

import java.util.Objects;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterAll;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:peliculas-test;DB_CLOSE_DELAY=-1",
		"app.admin.password=test-admin-password"
})
@AutoConfigureMockMvc
@Transactional
class WebPeliculasApplicationTests {
	private static final Path DIRECTORIO_PELICULAS_TEST = crearDirectorioPeliculasTest();

	@DynamicPropertySource
	static void configurarDirectorioPeliculas(DynamicPropertyRegistry registry) {
		registry.add("app.peliculas.directorio", () -> DIRECTORIO_PELICULAS_TEST.toString());
	}

	@AfterAll
	static void borrarDirectorioPeliculasTest() throws IOException {
		Files.deleteIfExists(DIRECTORIO_PELICULAS_TEST.resolve("Recuerdo-2017.mp4"));
		Files.deleteIfExists(DIRECTORIO_PELICULAS_TEST);
	}

	private static Path crearDirectorioPeliculasTest() {
		try {
			Path directorio = Files.createTempDirectory("cineplay-peliculas-test");
			Files.write(directorio.resolve("Recuerdo-2017.mp4"), new byte[32]);
			return directorio;
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Value("${app.admin.password}")
	private String claveAdmin;

	@Autowired
	private ServicioPeliculas servicioPeliculas;

	@Test
	void contextLoads() {
	}

	@Test
	void adminLoginAndCatalogRoutesRender() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/login"));

		MvcResult loginPage = mockMvc.perform(get("/admin/login"))
				.andExpect(status().isOk())
				.andReturn();
		MockHttpSession session = (MockHttpSession) Objects.requireNonNull(loginPage.getRequest().getSession(false));
		String csrfToken = Objects.requireNonNull((String) session.getAttribute("tokenCsrf"));

		mockMvc.perform(get("/admin/peliculas"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin"));

		mockMvc.perform(post("/admin/login")
						.session(session)
						.param("username", "admin")
						.param("password", claveAdmin)
						.param("_csrf", csrfToken))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/peliculas"));

		mockMvc.perform(get("/admin/peliculas").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("id=\"formulario\" class=\"mt-10 rounded-2xl border border-[#171a18]/10 bg-white p-5 shadow-sm sm:p-7  hidden\"")));

		String tokenAdmin = Objects.requireNonNull((String) session.getAttribute("tokenCsrf"));
		mockMvc.perform(post("/admin/peliculas/guardar")
						.session(session)
						.param("_csrf", tokenAdmin)
						.param("titulo", "Película de prueba")
						.param("genero", "Drama")
						.param("anio", "2020")
						.param("link", "Recuerdo-2017.mp4"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/peliculas"));

		mockMvc.perform(get("/peliculas/Recuerdo-2017.mp4"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/peliculas/Recuerdo-2017.mp4")
						.header("Range", "bytes=0-10"))
				.andExpect(status().isPartialContent());

		mockMvc.perform(get("/admin/peliculas").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Película de prueba")));

		long id = servicioPeliculas.obtenerPeliculas("Película de prueba", "", 1, 10).getFirst().getId();
		assertEquals("Recuerdo-2017.mp4", servicioPeliculas.obtenerPeliculaPorId(id).getLink());
		mockMvc.perform(get("/api/peliculas"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.peliculas[0].titulo", is("Película de prueba")))
				.andExpect(jsonPath("$.peliculas[0].imagenUrl", is("/assets/iconpel.png")))
				.andExpect(jsonPath("$.peliculas[0].link", is("/peliculas/Recuerdo-2017.mp4")));
		mockMvc.perform(get("/pelicula/{id}", id))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("/peliculas/Recuerdo-2017.mp4")));
		mockMvc.perform(get("/admin/peliculas/{id}/editar", id).session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Editar película")))
				.andExpect(content().string(containsString("value=\"Recuerdo-2017.mp4\"")));

		mockMvc.perform(post("/admin/peliculas/guardar")
						.session(session)
						.param("_csrf", tokenAdmin)
						.param("id", String.valueOf(id))
						.param("titulo", "Película editada")
						.param("genero", "Drama")
						.param("anio", "2020")
						.param("link", "Recuerdo-2017.mp4"))
				.andExpect(status().is3xxRedirection());
		assertEquals("Recuerdo-2017.mp4", servicioPeliculas.obtenerPeliculaPorId(id).getLink());

		mockMvc.perform(post("/admin/peliculas/{id}/borrar", id)
						.session(session)
						.param("_csrf", tokenAdmin))
				.andExpect(status().is3xxRedirection());
		assertEquals(0, servicioPeliculas.contarPeliculas("Película de prueba", ""));
	}

	@Test
	void routesIncludeContextPathWhenDeployedUnderSubpath() throws Exception {
		Pelicula nuevaPelicula = new Pelicula();
		nuevaPelicula.setTitulo("Película con contexto");
		nuevaPelicula.setGenero("Drama");
		nuevaPelicula.setAnio(2020);
		nuevaPelicula.setLink("Recuerdo-2017.mp4");
		Pelicula pelicula = servicioPeliculas.registrarPelicula(nuevaPelicula);
		long id = pelicula.getId();

		mockMvc.perform(get("/cineplay/api/peliculas").contextPath("/cineplay").servletPath("/api/peliculas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.peliculas[0].link", is("/cineplay/peliculas/Recuerdo-2017.mp4")))
				.andExpect(jsonPath("$.peliculas[0].imagenUrl", is("/cineplay/assets/iconpel.png")));

		mockMvc.perform(get("/cineplay/pelicula/{id}", id).contextPath("/cineplay").servletPath("/pelicula/" + id))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("/cineplay/peliculas/Recuerdo-2017.mp4")));

		mockMvc.perform(get("/cineplay/").contextPath("/cineplay"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("data-context-path=\"/cineplay/\"")))
				.andExpect(content().string(containsString("src=\"/cineplay/js/ObtenerPeliculas.js\"")));

		mockMvc.perform(get("/cineplay/peliculas/Recuerdo-2017.mp4")
						.contextPath("/cineplay")
						.servletPath("/peliculas/Recuerdo-2017.mp4"))
				.andExpect(status().isOk());

		MockHttpSession adminSession = new MockHttpSession();
		adminSession.setAttribute("adminAutenticado", true);
		adminSession.setAttribute("tokenCsrf", "test-token");
		mockMvc.perform(get("/cineplay/admin/peliculas")
						.contextPath("/cineplay")
						.servletPath("/admin/peliculas")
						.session(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("action=\"/cineplay/admin/logout\"")))
				.andExpect(content().string(containsString("src=\"/cineplay/assets/iconpel.png\"")));
	}
}
