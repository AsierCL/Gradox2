# Guía de DevOps — Gradox 2.0

Documento de referencia para que un equipo de DevOps pueda desplegar y mantener
**Gradox 2.0** en producción. Cubre: arquitectura de despliegue, tecnologías y
versiones, estructura del repositorio, configuración de entornos y secretos,
build, puesta en marcha, verificación, operación (logs, backups, actualizaciones)
y problemas conocidos.

> Estado del despliegue: **pre-producción**. La infraestructura está lista para
> prod (usuario no-root, límites de recursos, rotación de logs, bootstrap de
> admin, Swagger oculto), pero **todavía sin HTTPS** (decisión del equipo, ver §7).

---

## 1. Resumen de arquitectura

Aplicación web monolítica **Spring Boot** (backend + API REST) que usa servicios
externos gestionados para la persistencia y el almacenamiento de archivos:

- **Base de datos**: **PostgreSQL gestionado en Supabase** (ya no se despliega un
  contenedor de BD en el stack de producción).
- **Almacenamiento de archivos**: **S3 compatible** (Cloudflare **R2** en
  producción, **MinIO** en desarrollo) vía AWS SDK v2.

```
                       ┌──────────────────────────────────────┐
   Cliente / navegador │  app  (gradox2_app)                   │
   (frontend, futuro)  │  - Spring Boot / Java 21              │
        │              │  - JWT + BCrypt                        │
        │  HTTP :8080  │  - Flyway (migraciones)                │
        └────────────▶ │  - S3StorageService (AWS SDK v2)       │
                       │  - Logs rotados /app/logs              │
                       └──────┬──────────────┬──────────────────┘
                              │              │
                    JDBC :5432 │              │ S3 (put/get/delete)
                    (SSL)      │              │ path-style
                              ▼              ▼
            ┌────────────────────────┐   ┌────────────────────────┐
            │  Supabase PostgreSQL   │   │  Cloudflare R2 (prod)  │
            │  (gestionado)          │   │  / MinIO (dev)         │
            │  schema: Flyway        │   │  bucket "gradox2-files"│
            └────────────────────────┘   └────────────────────────┘
```

- **Persistencia de documentos**: los ficheros subidos (apuntes, exámenes…) se
  guardan en **S3**; en PostgreSQL solo se persiste la **clave del objeto**
  (`object_key`) en las tablas `files` y `temp_files`. La base de datos deja de
  almacenar los binarios como large objects.
  > Excepciones aún en PostgreSQL como `oid`: `badges.icon` y
  > `users.profile_picture` (no migradas todavía).
- **Correo**: la app envía emails (verificación de cuenta, reseteo de contraseña)
  vía **Gmail SMTP** configurado por variables de entorno.
- **Sin frontend separado**: hoy solo existe la API REST (endpoints en §10).
  Swagger/OpenAPI está desactivado en producción.

---

## 2. Tecnologías y versiones

| Componente | Tecnología | Versión | Notas |
|---|---|---|---|
| Lenguaje | Java | 21 (JDK/JRE) | `java.version` en `pom.xml` |
| Framework | Spring Boot | 3.5.13 | starter parent |
| ORM | Spring Data JPA / Hibernate | incluida en Boot 3.5.13 | `ddl-auto=validate` en prod |
| Migraciones | Flyway | incluida en Boot | `flyway-core` + `flyway-database-postgresql` |
| Seguridad | Spring Security + JWT | jjwt 0.11.5 | HS256, BCrypt para passwords |
| **S3 / almacenamiento** | AWS SDK v2 (`s3`) | 2.50.2 | BOM `software.amazon.awssdk:bom:2.50.2` en `dependencyManagement` |
| Documentación API | springdoc-openapi | 2.8.6 | **desactivada en prod** |
| Monitorización | Spring Boot Actuator | incluida | `/actuator/health`, `/actuator/info` |
| Email | spring-boot-starter-mail | incluida | SMTP Gmail (STARTTLS :587) |
| Base de datos | PostgreSQL | 16+ | **Supabase gestionado** (SSL) |
| Object storage | Cloudflare R2 / MinIO | — | endpoint S3 compatible, path-style |
| Build | Maven (wrapper) | 3.x vía `./mvnw` | imagen base `eclipse-temurin:21` |
| Contenedores | Docker + Docker Compose | Compose v2 | perfil `full` |
| Test | JUnit 5 + MockMvc | Boot test | H2 en memoria, 59 tests |

### Versiones clave del stack Docker
- `eclipse-temurin:21-jdk` — stage de build del Dockerfile
- `eclipse-temurin:21-jre` — imagen final de la app (más ligera)
- `minio/minio` — object storage de desarrollo (solo `docker-compose.dev.yml`)

---

## 3. Estructura del repositorio

```
Gradox2/
├── Docker/
│   ├── .env                 # SECRETOS + configuración de producción (NO versionado, ver §4)
│   ├── .env.example         # Plantilla versionada con placeholders
│   ├── Dockerfile           # Build multi-stage de la app
│   ├── docker-compose.yml   # Stack de producción (solo app; BD = Supabase, archivos = R2)
│   └── docker-compose.dev.yml # Entorno de desarrollo (Postgres + MinIO + app hot-reload)
├── gradox2/                 # Proyecto Maven (Spring Boot)
│   ├── pom.xml
│   ├── run.sh               # Scripts: dev | dev-down | run | test | docker-up | docker-down
│   ├── mvnw, .mvn/          # Maven Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/example/gradox2/
│       │   │   ├── Gradox2Application.java
│       │   │   ├── init/          # DataLoader (local/demo) + MasterBootstrap (prod)
│       │   │   ├── config/        # S3Config, OpenApiConfig
│       │   │   ├── security/      # SecurityConfig, JwtAuthFilter, RateLimitFilter, InMemoryRateLimiter
│       │   │   ├── presentation/controller/   # API REST (ver §10)
│       │   │   ├── persistence/   # entidades, repos, enums
│       │   │   ├── service/       # lógica de negocio + S3StorageService
│       │   │   └── utils/
│       │   └── resources/
│       │       ├── application-local.properties
│       │       ├── application-prod.properties
│       │       ├── application-test.properties
│       │       ├── logback-spring.xml        # rotación de logs
│       │       └── db/migration/V1__init.sql # schema inicial (Flyway)
│       └── test/            # tests de integración + TestS3Config (fake S3 in-memory)
├── Documentation/           # documentación (OVERVIEW, ARCHITECTURE, ENDPOINTS, DEVOPS…)
├── .dockerignore
├── .gitignore
└── README.md
```

> No existe `application.properties` base: cada perfil define su configuración
> completa en `application-<perfil>.properties`. El perfil activo se elige con
> `SPRING_PROFILES_ACTIVE`.

---

## 4. Configuración por entorno y secretos

Los entornos se seleccionan con `SPRING_PROFILES_ACTIVE`.

| Perfil | Uso | BD | S3 | Flyway | Swagger | Appender log |
|---|---|---|---|---|---|---|
| `local` | desarrollo | PostgreSQL local/contenedor dev | **MinIO** (obligatorio) | sí | sí (por defecto) | solo consola |
| `test` | tests de integración | H2 en memoria | **fake in-memory** (`TestS3Config`) | no | — | solo consola |
| `prod` | **producción** | **Supabase** | **Cloudflare R2** | sí | **no** | consola + fichero rotado |

### Dónde viven los secretos
- **`Docker/.env`** — archivo de secretos y configuración de producción. Está en
  `.gitignore` (nunca se versiona). `docker-compose.yml` lo inyecta en el
  contenedor mediante `env_file` + sección `environment`.
- **`Docker/.env.example`** — plantilla versionada con placeholders para crear el
  `.env` (`cp Docker/.env.example Docker/.env`).
- Los valores son leídos por la app a través de `${VAR}` en
  `application-prod.properties` / `application-local.properties`.

### Variables de entorno (`Docker/.env`)

| Variable | Descripción | Obligatoria | Ejemplo / notas |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL de Supabase | sí | `jdbc:postgresql://db.<ref>.supabase.co:5432/postgres?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuario JDBC | sí | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Password JDBC | sí | password de la BD en Supabase |
| `S3_ENDPOINT` | Endpoint S3 | sí | R2: `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`; dev: `http://minio:9000` |
| `S3_REGION` | Región S3 | sí | R2/MinIO: `auto` |
| `S3_ACCESS_KEY` | Access Key ID | sí | credencial R2 (o `minioadmin` en dev) |
| `S3_SECRET_KEY` | Secret Access Key | sí | credencial R2 (o `minioadmin` en dev) |
| `S3_BUCKET_NAME` | Bucket de objetos | sí | `gradox2-files` (debe existir en R2; en MinIO se crea en arranque) |
| `SPRING_MAIL_HOST` | Host SMTP | sí | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | Puerto SMTP | sí | `587` (STARTTLS) |
| `SPRING_MAIL_USERNAME` | Cuenta SMTP | sí | `gradox2App@gmail.com` |
| `SPRING_MAIL_PASSWORD` | App password SMTP | sí | contraseña de aplicación de Gmail |
| `SPRING_MAIL_SMTP_AUTH` | Autenticación SMTP | sí | `true` |
| `SPRING_MAIL_SMTP_STARTTLS` | STARTTLS | sí | `true` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | sí | **`prod`** |
| `JWT_SECRET` | Firma HS256 de JWT | sí | ≥ 32 bytes, aleatorio (`openssl rand -base64 32`) |
| `JWT_EXPIRATION` | Validez del token (ms) | no | `86400000` (24 h) |
| `APP_BASE_URL` | URL pública para enlaces de email | **sí (pendiente)** | ver §6 |
| `BOOTSTRAP_MASTER_USERNAME` | Primer MASTER (solo primer arranque) | **sí (pendiente)** | ver §6 |
| `BOOTSTRAP_MASTER_PASSWORD` | Password del primer MASTER | **sí (pendiente)** | ver §6 |
| `BOOTSTRAP_MASTER_EMAIL` | Email del primer MASTER | **sí (pendiente)** | ver §6 |

> Las antiguas variables `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB`
> **ya no son necesarias**: la BD es gestionada (Supabase). Solo quedan en el
> ejemplo como referencia heredada del desarrollo local.

> ⚠️ **Valores pendientes de rellenar antes del primer despliegue**: `APP_BASE_URL`,
> `BOOTSTRAP_MASTER_USERNAME`, `BOOTSTRAP_MASTER_PASSWORD`,
> `BOOTSTRAP_MASTER_EMAIL`. Si el bootstrap queda vacío, **no se creará ningún
> usuario MASTER** y, como el registro solo crea cuentas `USER` y el ascenso a
> MASTER requiere votación de otros MASTERs, **la plataforma quedaría sin administradores**.

---

## 5. Build de la imagen

El `Dockerfile` es **multi-stage**:

1. **Build** (`eclipse-temurin:21-jdk`): copia `pom.xml`, el Maven wrapper y el
   código, precarga dependencias (`./mvnw dependency:go-offline`) y empaqueta con
   `./mvnw package -DskipTests`. Artifacto: `gradox2-0.0.1-SNAPSHOT.jar`.
2. **Runtime** (`eclipse-temurin:21-jre`):
   - instala `curl` (para el healthcheck),
   - crea usuario no-root `appuser` (uid 999, `nologin`),
   - copia el jar como `app.jar`,
   - crea `/app/logs` con propietario `appuser`,
   - define `JAVA_OPTS` (heap limitado por cgroup: `-XX:MaxRAMPercentage=75.0
     -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError`),
   - `EXPOSE 8080`, `HEALTHCHECK` vía Actuator y `ENTRYPOINT` con `exec java`
     (Java como PID 1 para recibir señales de graceful shutdown).

> Docker build context = **raíz del repositorio** (`..` en el compose). El
> `.dockerignore` excluye `.git`, `target/`, logs, `.md` y los `.env`.

### Construir la imagen
```bash
cd Gradox2
docker build --network=host -t gradox2-app:latest -f Docker/Dockerfile .
```
> ℹ️ En algunos entornos el bridge por defecto de Docker falla (`operation not
> supported` al crear la veth). Si ocurre, usa `--network=host` en el build.

### Build manual (sin Docker), para validar tests
```bash
cd Gradox2/gradox2
./mvnw test          # 59 tests (H2 + fake S3)
./mvnw package -DskipTests
```

---

## 6. Puesta en marcha en producción

### Prerrequisitos del host
- Docker Engine con Docker Compose v2 (`docker compose`, no `docker-compose`).
- Puerto libre: **8080** (app).
- ~1-2 GB RAM y ~1 CPU para el stack (límite del compose: app 1024m/768m).
- Red de salida a **Supabase** (puerto 5432, requiere **IPv6 o IPv4 habilitado**
  según el proyecto Supabase) y al **SMTP de Gmail** (587) y al **endpoint de R2**
  (HTTPS).

### Paso 1 — Crear los recursos externos
1. **Supabase**: crear el proyecto y obtener la conexión
   `postgresql://postgres:<password>@db.<ref>.supabase.co:5432/postgres`.
   > Algunos proyectos Supabase nuevos son **IPv6-only** por defecto. Si el host
   > no tiene IPv6, activa IPv4 en el proyecto o usa el **connection pooler**
   > (puerto 6543) según la configuración de Supabase.
2. **R2**: crear un **bucket** con el nombre elegido (p. ej. `gradox2-files`) y
   generar un **API Token** (Access Key + Secret). La app **no crea el bucket en
   R2**: debe existir previamente. (En MinIO dev sí se auto-crea al arrancar.)

### Paso 2 — Crear el `.env`
```bash
cd Gradox2
cp Docker/.env.example Docker/.env
# Editar Docker/.env y rellenar como mínimo:
#   - SPRING_DATASOURCE_URL/USERNAME/PASSWORD  → credenciales de Supabase
#   - S3_ENDPOINT/S3_ACCESS_KEY/S3_SECRET_KEY/S3_BUCKET_NAME → credenciales de R2
#   - SPRING_MAIL_*  (credenciales de la cuenta Gmail)
#   - JWT_SECRET  (>= 32 bytes)
#   - APP_BASE_URL  → http://<IP-o-dominio>:8080   (sin HTTPS por ahora)
#   - BOOTSTRAP_MASTER_*  → credenciales del primer administrador
#   - SPRING_PROFILES_ACTIVE=prod
```

### Paso 3 — Levantar el stack
```bash
cd Gradox2
docker compose --env-file Docker/.env -f Docker/docker-compose.yml --profile full up -d --build
```
> El compose de producción **no despliega la BD**: el servicio `app` es el único
> contenedor. El perfil `full` queda por compatibilidad con `run.sh`.

En el **primer arranque**:
1. Flyway aplica `V1__init.sql` contra la BD de Supabase (creación de schema).
2. La app arranca con `ddl-auto=validate` (comprueba que el schema coincide con
   las entidades JPA; si hay mismatch, **falla el arranque** a propósito).
3. `MasterBootstrap` crea el primer usuario **MASTER** a partir de
   `BOOTSTRAP_MASTER_*` (solo si no existe ya; es idempotente y nunca loguea la
   contraseña).

### Paso 4 — Verificar el despliegue
```bash
# Estado de los contenedores
docker compose --env-file Docker/.env -f Docker/docker-compose.yml ps

# Health de la app (comprueba app + PostgreSQL + disco)
curl -s http://localhost:8080/actuator/health
# → {"status":"UP","components":{"db":{...UP...},"diskSpace":{...}},...}

# Liveness / readiness
curl -s http://localhost:8080/actuator/health/liveness
curl -s http://localhost:8080/actuator/health/readiness

# Versión desplegada
curl -s http://localhost:8080/actuator/info

# Login del MASTER bootstrap
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"<BOOTSTRAP_MASTER_USERNAME>","password":"<BOOTSTRAP_MASTER_PASSWORD>"}'
# → {"token":"...","refreshToken":"...","username":"...","role":"MASTER"}

# Logs de arranque (debe aparecer el mensaje del bootstrap)
docker logs gradox2_app | grep -i master
```

### Paso 5 — Registro de usuarios
El registro (`POST /api/auth/register`) crea usuarios `USER` deshabilitados que
deben **verificar su email** (se envía un correo con el enlace que usa
`APP_BASE_URL`). Hasta que `APP_BASE_URL` apunte a una URL accesible, los enlaces
de verificación serán inutilizables para el usuario final.

---

## 7. Seguridad (estado actual y pendientes)

### Aplicado / endurecido
| Medida | Estado |
|---|---|
| Contenedor app con usuario no-root (`appuser`, uid 999) | ✅ |
| Imagen runtime ligera (`eclipse-temurin:21-jre`, sin SDK) | ✅ |
| Contraseñas en BD con **BCrypt** | ✅ |
| JWT firmado HS256 con secret por env (≥ 32 bytes) | ✅ |
| Tokens de refresh almacenados en BD (no en claro) | ✅ |
| Controles de acceso por rol (MASTER/USER/GUEST) vía `@EnableMethodSecurity` | ✅ |
| **Visibilidad de archivos en descarga**: PUBLIC→todos, RESTRICTED→autenticados, PRIVATE→uploader/MASTER (404 si no autorizado) | ✅ |
| **Binarios fuera de la BD**: documentos en S3 (R2/MinIO), solo `object_key` en PG | ✅ |
| **Credenciales S3 por entorno** (`S3_ACCESS_KEY`/`S3_SECRET_KEY`) | ✅ |
| **Bootstrap del primer MASTER** vía env (`BOOTSTRAP_MASTER_*`), sin logs de credenciales | ✅ |
| **Swagger/OpenAPI desactivado en prod** | ✅ |
| Rate limiting en login/register (in-memory por IP remota) | ✅ (parcial, ver abajo) |
| Rotación de logs (tamaño/fecha) | ✅ |
| Límites de memoria/CPU por contenedor | ✅ |
| Actuator expuesto solo `health` e `info` | ✅ |
| Config de votaciones autocreada con valores por defecto si falta | ✅ |
| Datos demo (`DataLoader`) **solo** en perfiles `local`/`demo` | ✅ |
| CSRF desactivado (API stateless con JWT) | ✅ |

### Pendientes / riesgos conocidos (por orden de prioridad)
1. **Sin TLS/HTTPS** — decisión del equipo para esta fase. El tráfico (incluido
   login) va en claro. **Antes de exponer credenciales reales o datos reales,
   poner un proxy TLS** (Caddy, nginx, Traefik) delante del puerto 8080.
2. **CORS totalmente permisivo** (`*`, con credentials). Pensado para un frontend
   aún inexistente; restringir `AllowedOriginPatterns` cuando exista frontend fijo.
3. **Rate limiter in-memory basado en `request.getRemoteAddr()`** — detrás de un
   proxy/túnel, todos los clientes comparten la IP. Si se introduce TLS/proxy,
   usar el header `X-Forwarded-For`.
4. **JWT sin revocación** — los tokens son válidos hasta `JWT_EXPIRATION`
   (24 h). Los refresh tokens sí son revocables (BD).
5. **Backups** — en Supabase la BD se respalda por el proveedor (ver §8); el
   bucket R2 se gestiona desde el dashboard. Si se usara Postgres/MInIO local,
   habría que planificar dump + copia del bucket.
6. **Tokens de verificación/reset enviados por email en claro** — mitigable con
   HTTPS y rotación corta (por defecto expiran).
7. **`GET /files/**` es `permitAll`** — el acceso al contenido se protege a nivel
   de servicio (404 si no autorizado) y a la metadata vía `IdentityVisibility`,
   pero conviene revisar el mapeo al añadir más endpoints.
8. **`/admin/**` mapeado a `hasRole("MASTER")`** — solo existen `ban`/`unban`;
   `logs` y `config` siguen pendientes.
9. **`badges.icon` y `users.profile_picture`** siguen en PostgreSQL como `oid`
   (large objects) — candidatos a migrar a S3 en una iteración futura.

---

## 8. Operación diaria

### Logs
- `docker logs gradox2_app` — salida estándar (siempre disponible).
- Ficheros rotados dentro del contenedor en `/app/logs/`:
  - `gradox2.log` (activo)
  - `gradox2.YYYY-MM-DD.N.log` (rotados)
- Política de rotación (`logback-spring.xml`): **10 MB** por fichero,
  **30 días** de historial, tope **500 MB** en total. El directorio `LOG_DIR` se
  fija a `/app/logs` desde el compose.

> Para inspeccionar logs persistidos fuera del contenedor conviene montar
> `/app/logs` como volumen (pendiente de decidir; hoy es efímero).

### Backup / restauración
**La BD es gestionada (Supabase)** → el proveedor aplica copias automáticas
(ver dashboard de Supabase: *Backups*). Para un dump manual portable:
```bash
pg_dump "postgresql://postgres:<PASSWORD>@db.<ref>.supabase.co:5432/postgres?sslmode=require" \
  > supabase-$(date +%F).sql
```
> La app usa `ddl-auto=validate` y Flyway: si restauras a una BD vacía, Flyway
> re-aplicará `V1__init.sql` automáticamente en el arranque.

**Los objetos (archivos) viven en R2** → no se incluyen en `pg_dump`. Asegúrate
de que el bucket tiene el plan de backup/versionado que el equipo decida (R2
ofrece lifecycle/versionado desde el dashboard).

### Actualizaciones (deploy de una versión nueva)
```bash
cd Gradox2
git pull                       # o desplegar el nuevo tag/build
docker compose --env-file Docker/.env -f Docker/docker-compose.yml --profile full up -d --build --force-recreate
```
- Si la nueva versión añade migraciones Flyway (nuevos `V*.sql` bajo
  `gradox2/src/main/resources/db/migration/`), se aplicarán automáticamente en
  el arranque, en orden, dentro de una transacción por migración.
- **No modificar migraciones ya aplicadas** (Flyway valida el checksum; un cambio
  en `V1__init.sql` romperá el arranque en BDs existentes).
- `ddl-auto=validate` fallará el arranque si el schema no coincide con las
  entidades: la migración debe actualizar el schema antes de desplegar el código.

### Reinicio / apagado
```bash
docker compose --env-file Docker/.env -f Docker/docker-compose.yml restart   # reinicia
docker compose --env-file Docker/.env -f Docker/docker-compose.yml down      # apaga
```

### Escalado y recursos
- Límites actuales (compose): `app` 1024m/768m y 1 CPU.
- La JVM usa `-XX:MaxRAMPercentage=75.0` + `-XX:+ExitOnOutOfMemoryError`: si la
  app no cabe en el límite, **sale con error en lugar de degradarse**.
- Ajusta `mem_limit` del servicio `app` según el tráfico (la app ya no guarda los
  binarios en RAM de la BD, pero sigue sirviendo descargas a través de S3).

---

## 9. Base de datos

- Motor: **PostgreSQL 16+ gestionado en Supabase** (conexión por puerto 5432 con
  SSL). Nombre/usr/pass definidos por `SPRING_DATASOURCE_*` del `.env`.
- **Migraciones Flyway**: `V1__init.sql` (schema completo: usuarios, cursos,
  asignaturas, archivos, propuestas, votos, delegaciones, badges, auditoría,
  tokens de refresh/verificación/reset).
- En prod `spring.jpa.hibernate.ddl-auto=validate` (nunca `update` ni `create`).
- **Binarios**: los documentos aprobados/propuestos se guardan en **S3**; en BD
  solo la columna `object_key` (varchar 512) en `files` y `temp_files`. Aún como
  `oid`: `badges.icon` y `users.profile_picture`.

---

## 10. API (resumen operativo)

Base URL: `http://<host>:8080` (sin HTTPS por ahora). Lista detallada en
[ENDPOINTS.md](ENDPOINTS.md).

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | público | registro (usuario deshabilitado hasta verificar email) |
| POST | `/api/auth/login` | público | login → `{token, refreshToken}` |
| POST | `/api/auth/token/refresh` | público | renovar access token con refresh |
| POST | `/api/auth/logout` | autenticado | revoca refresh token |
| GET | `/api/auth/verify?token=...` | público | verificación de email |
| POST | `/api/auth/password/reset-request` | público | envía enlace de reset |
| POST | `/api/auth/password/reset` | público | cambia la contraseña |
| GET/PUT | `/users/me` | autenticado | perfil propio |
| GET | `/users/{id}` · `/users/all` | autenticado | consultar usuarios |
| POST/GET/DELETE | `/uploadProposal/...` | autenticado | proponer archivo |
| GET | `/uploadProposal/{id}/download` | autenticado | descargar propuesta (desde S3) |
| GET | `/files/all` | autenticado | listar archivos aprobados |
| GET | `/files/{id}` | público (metadata con visibilidad) | metadata de archivo |
| GET | `/files/{id}/download` | público con visibilidad (404 si no autorizado) | descargar archivo (desde S3) |
| DELETE | `/files/{id}` | autenticado | propuesta de eliminación con votación |
| POST/DELETE | `/files/{id}/vote[/{upvote}]` | autenticado | votar/retirar voto de archivo |
| PUT | `/files/{id}/visibility` | owner/MASTER | cambiar visibilidad |
| GET/POST/DELETE | `/vote/{id}...` | autenticado | votar propuestas |
| GET/POST | `/promoteProposal/...` | autenticado | promociones/despromociones |
| GET/PUT | `/vote-config` | MASTER | configuración de votaciones |
| PUT | `/admin/users/{id}/ban` · `/unban` | MASTER | moderación de usuarios |
| GET | `/health` · `/actuator/health/**` · `/actuator/info` | público | healthcheck / info |
| — | `/swagger-ui/**`, `/v3/api-docs/**` | **desactivado en prod** | solo disponible en local/dev |

---

## 11. Flujo de gobernanza (resumen funcional)

- **Roles**: `GUEST`, `USER`, `MASTER`.
- Los archivos se suben como **propuestas** que requieren votación (quórum y
  umbral configurables en `/vote-config`, gestionados por MASTERs) antes de
  publicarse.
- **Visibilidad** de archivos: `PUBLIC`, `RESTRICTED`, `PRIVATE`.
- La **reputación** de los usuarios sube con votos positivos y pondera el peso
  de sus votos (MASTER pesa más).
- El **primer MASTER** solo se crea vía `BOOTSTRAP_MASTER_*` en el primer arranque
  de prod.

---

## 12. Checklist pre-despliegue

- [ ] `Docker/.env` creado desde `.env.example` con valores reales.
- [ ] `SPRING_DATASOURCE_URL` apunta a **Supabase** con password correcto.
- [ ] Bucket **R2** creado con el nombre de `S3_BUCKET_NAME` y credenciales válidas.
- [ ] `JWT_SECRET` aleatorio ≥ 32 bytes.
- [ ] `SPRING_PROFILES_ACTIVE=prod`.
- [ ] `APP_BASE_URL` apunta a la URL pública real.
- [ ] `BOOTSTRAP_MASTER_USERNAME/PASSWORD/EMAIL` rellenos (si no, no habrá admin).
- [ ] Credenciales SMTP de Gmail correctas y con permiso para enviar.
- [ ] Build: `docker build --network=host -f Docker/Dockerfile .` OK.
- [ ] Tests: `./mvnw test` → 59/59 OK (en el directorio `gradox2/`).
- [ ] `docker compose --env-file Docker/.env -f Docker/docker-compose.yml config --quiet` no da errores.
- [ ] Primer arranque verificado: health UP, login del MASTER funciona, bootstrap idempotente.
- [ ] Verificar subida+descarga de un archivo (S3 end-to-end).
- [ ] Backups de Supabase y del bucket R2 revisados antes de poblar datos reales.
- [ ] Decidido el plan de **TLS** antes de exponer credenciales/datos reales.

---

## 13. Troubleshooting común

| Síntoma | Causa probable | Solución |
|---|---|---|
| `docker build` falla con `operation not supported` (veth) | bridge Docker no disponible en el host | usar `--network=host` en el build |
| La app no conecta a Supabase (`Connection refused`/timeout) | host sin IPv6 o IPv4 no habilitado en Supabase | activar IPv4 en Supabase o usar el pooler (6543) |
| La app no arranca: error de FK/constraint al crear tablas | Flyway ejecutando en BD con datos previos de otro schema | usar una BD/proyecto Supabase vacío para el primer arranque |
| La app falla con `Schema-validation` | `ddl-auto=validate` detecta mismatch entidad↔tabla | añadir migración Flyway, no cambiar `V1__init.sql` ya aplicado |
| Flyway: checksum mismatch | `V1__init.sql` modificado tras aplicarse | NO editar migraciones aplicadas; corregir o `flyway repair` (con cuidado) |
| Subida/descarga de archivos falla con `S3Exception` | credenciales R2 incorrectas, bucket inexistente o endpoint mal configurado | revisar `S3_*` del `.env`, que el bucket exista en R2 y el API token tenga permisos |
| `The authorization header is malformed` / 403 en R2 | región distinta de `auto` o secret incorrecto | `S3_REGION=auto` + regenerar el token |
| Descargas con error pero la app levanta | `S3StorageService` no pudo crear el bucket (best-effort) | en MinIO dev el bucket se auto-crea; en R2 debe existir antes |
| Login 429 / rate limited a pesar de IPs distintas | rate limiter in-memory por `getRemoteAddr()` | si hay proxy, ver §7.3 |
| Enlaces de verificación apuntan a `localhost` | `APP_BASE_URL` sin cambiar | fijar URL pública en `Docker/.env` |
| No hay forma de crear el primer admin | `BOOTSTRAP_MASTER_*` vacíos | rellenar y reiniciar la app (es idempotente) |
| Swagger/OpenAPI no responde en prod | desactivado a propósito | esperado; no re-activar en prod |
| Contraseña de Gmail rechazada | SMTP necesita "contraseña de aplicación" | generar app password en la cuenta de Google |
| App se detiene de golpe con OOM | `-XX:+ExitOnOutOfMemoryError` + límite pequeño | subir `mem_limit` del servicio `app` |

---

## 14. Pendientes del equipo DevOps (hoja de ruta)

1. **TLS/HTTPS** (Caddy/nginx + ajustar `APP_BASE_URL` a `https://...`).
2. Revisar **IPv6/IPv4** y el uso del **connection pooler** de Supabase según el host.
3. Definir **backup del bucket R2** (lifecycle/versionado) y revisar los backups
   automáticos de Supabase.
4. Montar `/app/logs` como volumen persistente (hoy efímero).
5. Revisar rate limiter para funcionamiento tras proxy (`X-Forwarded-For`).
6. Restringir CORS al frontend real cuando exista.
7. CI/CD: actualmente no hay pipeline; el build es manual (`docker build` +
   `docker compose up`). Considerar GitHub Actions (build de imagen + push a
   registry + deploy por SSH).
8. Migrar `badges.icon` y `users.profile_picture` (aún `oid` en PostgreSQL) a S3.
