# Guía de Uso de la API — Gradox 2

## 1. El Contrato Oficial (OpenAPI)
El listado exhaustivo de todos los endpoints, parámetros, esquemas de datos y respuestas se encuentra en el archivo JSON exportado directamente del código fuente:

📄 **[openapi.json](./openapi.json)**

## 2. Explorador Interactivo (Swagger UI)
Para explorar, leer la documentación detallada de cada ruta y **probar la API en tiempo real**, debes levantar el entorno de desarrollo local (mediante `./run.sh dev` o `./mvnw spring-boot:run`).

Una vez levantado, abre tu navegador en:
🔗 **http://localhost:8080/swagger-ui.html**

> Nota: Por motivos de rendimiento, la interfaz de Swagger y el endpoint JSON `/v3/api-docs` están **desactivados en producción**.

## 3. Autenticación (JWT)
La API es *stateless*. Para acceder a las rutas protegidas, debes incluir tu token JWT en la cabecera HTTP `Authorization` con el prefijo `Bearer`.

**Flujo básico:**
1. Haz una petición `POST` a `/api/auth/login` con tus credenciales.
2. Extrae el campo `token` de la respuesta JSON.
3. Envíalo en las cabeceras de tus siguientes peticiones:

```bash
curl -X GET http://localhost:8080/users/me \
     -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsIn..."
```

## 4. Roadmap y Tareas Pendientes (MVP)

El core de la aplicación (Autenticación, Usuarios, Archivos, Asignaturas, Votaciones e Hilos) ya está 100% implementado y documentado en el archivo OpenAPI.

Las siguientes capacidades a nivel de producto siguen en el backlog y se añadirán al contrato OpenAPI conforme se vayan desarrollando:

- 🛡️ Moderación y denuncias (/moderation/*)
- 🔔 Notificaciones internas (/notifications/*)
- ⚙️ Administración avanzada (/admin/logs y /admin/config)

