# Guía de Uso de la API — Gradox 2

## 1. El Contrato Oficial (OpenAPI)
El listado exhaustivo de todos los endpoints, parámetros, esquemas de datos y respuestas se encuentra en el archivo JSON exportado directamente del código fuente:

📄 **Contrato OpenAPI**: se genera en CI (job `openapi`) y se descarga como artefacto `openapi` de la última corrida.
Para generarlo en local: `cd gradox2 && ./mvnw test -Dtest=OpenApiContractTest -Dopenapi.export=true` → `gradox2/target/openapi.json`.

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

## 4. Multi-grado: catálogo de grados

El grado de un usuario es permanente y define en qué comunidad puede **escribir**; la lectura es pública.
La especificación completa está en [GRADOS.md](GRADOS.md).

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/degrees` | público | Grados ordenados por código oficial (`G4012`, `G4022`, `G4121`) |
| `GET` | `/degrees/{id}` | público | Detalle de un grado |
| `GET` | `/degrees/{id}/courses` | público | Cursos del grado con sus asignaturas (pantalla por defecto tras el login) |
| `GET` | `/courses?degreeId=` | público | Filtro opcional por grado (sin filtro: todos) |
| `GET` | `/subjects?degreeId=` | público | Filtro opcional por grado (sin filtro: todos) |
| `PATCH` | `/admin/users/{id}/degree` | `ADMIN` | Override auditado del grado; invalida las sesiones del usuario |

`POST /api/auth/register` exige `degreeId`. El login y `/users/me` devuelven `degreeId`, `degreeCode`,
`degreeName` e `isPlatformAdmin`.

### Códigos de error del ámbito de grado

| Código | HTTP | Cuándo |
|---|---|---|
| `DEGREE_REQUIRED` | `400` | Registro sin grado, o escritura de un usuario sin grado |
| `DEGREE_NOT_FOUND` | `400` | `degreeId` inexistente en el registro |
| `DEGREE_SCOPE_FORBIDDEN` | `403` | Escritura sobre un recurso de otro grado |
| `INVALID_ROLE_OPERATION` | `400` | Expulsión del último master activo de un grado (código compartido con otras operaciones de rol) |

---

## 5. Roadmap y Tareas Pendientes (MVP)

El core de la aplicación (Autenticación, Usuarios, Archivos, Asignaturas, Votaciones e Hilos) ya está 100% implementado y documentado en el archivo OpenAPI.

Las siguientes capacidades a nivel de producto siguen en el backlog y se añadirán al contrato OpenAPI conforme se vayan desarrollando:

- 🛡️ Moderación y denuncias (/moderation/*)
- 🔔 Notificaciones internas (/notifications/*)
- ⚙️ Administración avanzada (/admin/logs y /admin/config)

