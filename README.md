# Cine-Play
Web de Streaming de películas

## Configuración

La contraseña del administrador se configura mediante la variable de entorno `ADMIN_PASSWORD`; la aplicación no incluye una contraseña predeterminada.

```bash
export ADMIN_PASSWORD='elige-una-contraseña-segura'
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8081/cineplay`.
