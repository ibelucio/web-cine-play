let paginaActual = 1;
const TAMANO_PAGINA = 10;
let solicitudActual;
const rutaBase = document.querySelector("html").dataset.contextPath || "/";

document.addEventListener("DOMContentLoaded", () => {
    const btnAnterior = document.getElementById("btn-anterior");
    const btnSiguiente = document.getElementById("btn-siguiente");
    const buscador = document.getElementById("buscador");
    const btnBuscador = document.getElementById("btn-buscador");

    if (buscador) {
        buscador.value = new URLSearchParams(window.location.search).get("busqueda") || "";
    }

    cargarPeliculas();

    // Eventos de botones
    btnAnterior.addEventListener("click", () => {
        if (paginaActual > 1) {
            paginaActual--;
            cargarPeliculas();
        }
    });

    btnSiguiente.addEventListener("click", () => {
        paginaActual++;
        cargarPeliculas();
    });

    const buscar = () => {
        paginaActual = 1;
        cargarPeliculas();
    };

    btnBuscador?.addEventListener("click", buscar);

    if (buscador) {
        buscador.addEventListener("input", buscar);
        buscador.addEventListener("keydown", event => {
            if (event.key === "Enter") {
                buscar();
            }
        });
    }
});

async function cargarPeliculas() {
    const busqueda = document.getElementById("buscador")?.value || "";
    solicitudActual?.abort();
    solicitudActual = new AbortController();

    try {
        const response = await fetch(`${rutaBase}api/peliculas?busqueda=${encodeURIComponent(busqueda)}&pagina=${paginaActual}&tamano=${TAMANO_PAGINA}`, {
            signal: solicitudActual.signal
        });
        if (!response.ok) {
            throw new Error(`La solicitud falló (${response.status})`);
        }
        const pagina = await response.json();

        renderizarPeliculas(pagina.peliculas);
        actualizarPaginacion(pagina);
    } catch (error) {
        if (error.name === "AbortError") {
            return;
        }
        console.error("Error al obtener las películas:", error);
    }
}

function renderizarPeliculas(peliculas) {
    const contenedor = document.getElementById("contenedor");
    const plantilla = document.getElementById("plantilla-pelicula");

    contenedor.innerHTML = "";

    peliculas.forEach((pelicula, index) => {
        const clon = plantilla.content.cloneNode(true);

        const link = clon.querySelector(".pelicula-link");
        const img = clon.querySelector(".pelicula-img");
        const numero = clon.querySelector(".pelicula-numero");

        if (link) link.href = `${rutaBase}pelicula/${pelicula.id}`;
        if (img) {
            const imagenPorDefecto = `${rutaBase}assets/iconpel.png`;
            const imagenOriginal = (pelicula.imagenUrl || "").trim();

            img.alt = pelicula.titulo || "Película";

            img.onerror = function () {
                this.onerror = null;
                this.src = imagenPorDefecto;
            };

            img.src = imagenOriginal || imagenPorDefecto;
        }

        // Calcula la posición global según la página actual (1..10, 11..20, etc.)
        if (numero) {
            numero.textContent = (paginaActual - 1) * TAMANO_PAGINA + (index + 1);
        }
        const titulo = clon.querySelector(".pelicula-titulo");
        if (titulo) titulo.textContent = pelicula.titulo;
        const genero = clon.querySelector(".pelicula-genero");
        if (genero) genero.textContent = pelicula.genero;

        contenedor.appendChild(clon);
    });
}

function actualizarPaginacion(pagina) {
    const btnAnterior = document.getElementById("btn-anterior");
    const btnSiguiente = document.getElementById("btn-siguiente");
    const indicador = document.getElementById("pagina-indicador");

    const totalPaginas = Math.max(1, Math.ceil(pagina.total / TAMANO_PAGINA));
    indicador.textContent = `Página ${paginaActual} de ${totalPaginas}`;

    // Deshabilita el botón anterior si está en la página 1
    btnAnterior.disabled = paginaActual === 1;

    // Deshabilita el botón siguiente al llegar a la última página
    btnSiguiente.disabled = paginaActual >= totalPaginas;
}