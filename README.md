# 🎬 Cine-Play

### Aplicación web de streaming y gestión de películas

Cine-Play es una aplicación web desarrollada con **Java, Spring Boot, HTML, Tailwind CSS y JavaScript**, que utiliza una base de datos H2 para almacenar la información de las películas.

El proyecto permite visualizar un catálogo de películas desde una interfaz web y cuenta con un panel de administración protegido mediante inicio de sesión, desde el que se pueden añadir, editar y eliminar películas.

🌐 **Prueba la aplicación:** [cineplay.freepage.cc](https://cineplay.freepage.cc)

---

## ✨ Funcionalidades

### 🎥 Catálogo de películas

- Visualización de las películas registradas en la base de datos.
- Presentación de las películas mediante su título, género, año y portada.
- Acceso a los vídeos almacenados en el servidor.
- Interfaz web desarrollada con HTML, Tailwind CSS y JavaScript.

### 🔐 Panel de administración

- Acceso a través de la ruta `/admin`.
- Inicio de sesión mediante usuario y contraseña.
- Acceso al panel de administración después de autenticarse.
- Gestión completa del catálogo de películas.

### 🗂️ Gestión del catálogo

Desde el panel de administración es posible añadir, editar y eliminar películas.

Cada película contiene los siguientes campos:

| Campo               | Descripción                                               |
| ------------------- | --------------------------------------------------------- |
| Título              | Nombre de la película.                                    |
| Género              | Género cinematográfico.                                   |
| Año                 | Año de estreno.                                           |
| Ruta de la película | Nombre del archivo de vídeo, por ejemplo, `pelicula.mp4`. |
| Portada             | Imagen que representa la película en el catálogo.         |

La información se almacena en la base de datos H2, mientras que los archivos de vídeo se guardan por separado en el sistema de archivos.

---

## 🛠️ Tecnologías utilizadas

| Tecnología               | Uso                                                |
| ------------------------ | -------------------------------------------------- |
| Java 21                  | Lenguaje de programación del backend.              |
| Spring Boot 4.1.1        | Framework principal de la aplicación.              |
| Spring WebMVC            | Gestión de las peticiones web.                     |
| Spring Data JPA          | Acceso y persistencia de datos.                    |
| Thymeleaf                | Integración de plantillas HTML con el backend.     |
| HTML5                    | Estructura de las páginas web.                     |
| Tailwind CSS             | Estilos y diseño de la interfaz.                   |
| JavaScript               | Interactividad del frontend.                       |
| H2 Database              | Almacenamiento de la información de las películas. |
| Jakarta Validation 3.1.1 | Validación de datos.                               |
| Maven                    | Gestión de dependencias y compilación.             |

---

## 📄 Estructura de la aplicación

### Página principal: `index.html`

Es la página de inicio de Cine-Play. Carga y muestra las películas registradas en la base de datos para que puedan consultarse desde el catálogo.

### Inicio de sesión de administrador

Se accede añadiendo `/admin` a la dirección base de la aplicación.

El formulario solicita las credenciales del administrador. El nombre de usuario predeterminado es `admin`, mientras que la contraseña debe establecerse mediante la variable de entorno `ADMIN_PASSWORD`.

### Panel de administración: `admin.html`

Después de iniciar sesión, el usuario accede al panel de administración, donde puede añadir nuevas películas, modificar sus datos o eliminarlas del catálogo.

---

## ⚙️ Requisitos previos

Para ejecutar el proyecto necesitas:

- Java JDK 21.
- Git.
- Maven o el Maven Wrapper incluido en el repositorio.

No es necesario instalar una base de datos H2 independiente, ya que se incluye como dependencia del proyecto.

---

## 🚀 Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd web_peliculas
```

Sustituye `<URL_DEL_REPOSITORIO>` por la dirección real del repositorio de GitHub.

### 2. Configurar la contraseña del administrador

La aplicación no incluye una contraseña predeterminada. Cada usuario debe configurar la suya mediante la variable de entorno `ADMIN_PASSWORD`.

En Linux o macOS:

```bash
export ADMIN_PASSWORD='elige-una-contraseña-segura'
```

Esta variable estará disponible en la terminal actual y en los procesos iniciados desde ella.

En Windows PowerShell:

```powershell
$env:ADMIN_PASSWORD = "elige-una-contraseña-segura"
```

**Importante:** utiliza una contraseña robusta y no la publiques en el repositorio.

### 3. Preparar el directorio de películas

Por defecto, Cine-Play busca los archivos de vídeo en:

```text
/srv/peliculas
```

En Linux, puedes crear el directorio con:

```bash
sudo mkdir -p /srv/peliculas
```

Asegúrate de que el usuario que ejecuta la aplicación tenga permisos para acceder a los archivos.

Ejemplo de organización:

```text
/srv/peliculas/
├── pelicula.mp4
├── pelicula2.mp4
└── pelicula3.mp4
```

Cuando registres una película desde el panel de administración, introduce únicamente el nombre del archivo, por ejemplo:

```text
pelicula.mp4
```

No es necesario introducir la ruta completa si la aplicación ya utiliza el directorio configurado.

#### Utilizar otro directorio

Si deseas almacenar los vídeos en otra ubicación, puedes configurar la variable de entorno `APP_PELICULAS_DIR`.

Ejemplo en Linux o macOS:

```bash
export APP_PELICULAS_DIR='/ruta/a/mis/peliculas'
```

Si no defines esta variable, se utilizará `/srv/peliculas`.

### 4. Iniciar la aplicación

Desde la carpeta raíz del proyecto, ejecuta:

```bash
./mvnw spring-boot:run
```

Si el archivo `mvnw` no tiene permisos de ejecución en Linux o macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación se iniciará en el puerto `8081`.

---

## 🌐 Acceso a Cine-Play

Una vez iniciada la aplicación, podrás acceder a las siguientes rutas:

- **Página principal:** [http://localhost:8081/cineplay](http://localhost:8081/cineplay)
- **Inicio de sesión del administrador:** [http://localhost:8081/cineplay/admin](http://localhost:8081/cineplay/admin)

Para acceder al panel de administración, utiliza:

| Credencial | Valor                                            |
| ---------- | ------------------------------------------------ |
| Usuario    | `admin`                                          |
| Contraseña | La que hayas definido mediante `ADMIN_PASSWORD`. |

Si accedes desde otro dispositivo de tu red local, sustituye `localhost` por la dirección IP del servidor.

---

## 🗄️ Base de datos y persistencia

Cine-Play utiliza H2 con almacenamiento en archivos.

La configuración predeterminada es:

```properties
spring.datasource.url=jdbc:h2:file:./data/BD_peliculas
spring.jpa.hibernate.ddl-auto=update
```

La base de datos se crea en el directorio `data`, relativo al directorio de trabajo desde el que se ejecuta la aplicación.

La opción `ddl-auto=update` permite a Hibernate actualizar el esquema de la base de datos según las entidades de la aplicación.

Los archivos de vídeo se almacenan de forma independiente en `/srv/peliculas`, o en el directorio que se defina mediante `APP_PELICULAS_DIR`.

Para realizar una copia de seguridad completa, conviene guardar:

- El directorio `data` con los archivos de la base de datos H2.
- El directorio que contiene los archivos de vídeo.
- Cualquier archivo de portada que se almacene fuera de la base de datos.

Antes de copiar los archivos de H2, es recomendable detener la aplicación para evitar inconsistencias.

---

## 🔒 Seguridad

- La contraseña del administrador se configura mediante `ADMIN_PASSWORD`.
- No se incluye una contraseña predeterminada en el código.
- La consola web de H2 está deshabilitada por defecto.
- No se deben subir contraseñas reales ni otros secretos a GitHub.
- Para publicar la aplicación en Internet, se recomienda utilizar HTTPS y mantener protegidos los accesos de administración.

---

## ⚙️ Configuración de referencia

Las principales propiedades de la aplicación son:

| Propiedad                     | Valor predeterminado               | Descripción                                              |
| ----------------------------- | ---------------------------------- | -------------------------------------------------------- |
| `server.port`                 | `8081`                             | Puerto de la aplicación.                                 |
| `server.servlet.context-path` | `/cineplay`                        | Ruta base de la aplicación.                              |
| `app.admin.username`          | `admin`                            | Usuario administrador.                                   |
| `app.admin.password`          | `${ADMIN_PASSWORD}`                | Contraseña configurada mediante una variable de entorno. |
| `app.peliculas.directorio`    | `/srv/peliculas`                   | Directorio de archivos de vídeo.                         |
| `spring.datasource.url`       | `jdbc:h2:file:./data/BD_peliculas` | Ubicación de la base de datos H2.                        |
| `spring.h2.console.enabled`   | `false`                            | Consola web de H2 deshabilitada.                         |

---

## 📌 Información del proyecto

- **Nombre:** Cine-Play.
- **Tipo:** Aplicación web de streaming y administración de películas.
- **Backend:** Java y Spring Boot.
- **Frontend:** HTML, Tailwind CSS y JavaScript.
- **Base de datos:** H2.
- **Java:** 21.
- **Spring Boot:** 4.1.1.
- **Gestión de dependencias:** Maven.

---

## 👨‍💻 Autor: Luis Augusto ([https://github.com/ibelucio](https://github.com/ibelucio))

Proyecto personal desarrollado para poner en práctica tecnologías de desarrollo web frontend y backend, persistencia de datos y despliegue de aplicaciones en un servidor propio.


