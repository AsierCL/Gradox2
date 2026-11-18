# 🛠️ Instalación de Gradox 2.0 (desarrollo)

Este documento describe cómo instalar y ejecutar **Gradox 2.0** en un entorno local de desarrollo.

> Para **producción/VPS** (Supabase + Cloudflare R2) consulta
> [DEVOPS.md](./DEVOPS.md).

---

## 1. Requisitos Previos

- **Java 21** (OpenJDK recomendado):
  ```bash
  java -version   # openjdk version "21..."
  ```
- **Docker & Docker Compose v2** (para PostgreSQL + MinIO):
  ```bash
  docker --version
  docker compose version
  ```
- **Node.js 22+ y npm** (para levantar el frontend por separado):
  ```bash
  node --version
  npm --version
  ```
- **Git**:
  ```bash
  git --version
  ```

## 2. Clonar el Proyecto

```bash
mkdir Gradox2-workspace
cd Gradox2-workspace
git clone https://github.com/Gradox2/gradox2-api.git
git clone https://github.com/Gradox2/gradox2.github.io.git
```

## 3. Entorno de desarrollo (recomendado)

El backend y el frontend se arrancan por separado. El script del backend vive en
`gradox2-api/gradox2/` y levanta **PostgreSQL + MinIO + la API**:

```bash
cd gradox2-api/gradox2
./run.sh dev        # API + Postgres + MinIO
./run.sh dev-down   # detiene estos servicios
```

En una segunda terminal, inicia la web desde el backend:

```bash
cd gradox2-api/gradox2
./run.sh web-install  # una vez, instala dependencias en el repo web hermano
./run.sh web          # arranca Vite por separado
```

También puedes ejecutar `npm ci && npm run dev` desde `gradox2.github.io/`.
Si los repositorios no están en carpetas hermanas, define `GRADOX_FRONTEND_DIR`
con la ruta absoluta del frontend antes de usar `./run.sh web`.

La web queda disponible en `http://localhost:5173`, la API en
`http://localhost:8080` y la consola de **MinIO** en `http://localhost:9001`
(usuario `minioadmin` / `minioadmin`). El bucket usado en desarrollo es
`gradox2-files` (se auto-crea al arrancar).

Los servicios se publican en `0.0.0.0`, así que la web también responde en la
IP de la máquina (`http://192.168.x.x:5173`). `run.sh` detecta esa IP y la usa
para la API y las URLs firmadas de MinIO. Si necesitas forzar otra, define
`GRADOX_PUBLIC_HOST`.

## 4. Ejecución local sin Docker

Si prefieres arrancar la app directamente desde Maven contra una BD y un MinIO ya
levantados:

```bash
cd gradox2
./run.sh run        # usa mvnw spring-boot:run con el perfil local
./run.sh test       # ejecuta la suite de tests (H2 + fake S3)
```

> `run` requiere PostgreSQL en `localhost:5432` y un **MinIO** accesible en
> `localhost:9000`; en caso contrario usa `./run.sh dev` para levantarlos.

## 5. Configuración manual (sin el script)

### 5.1 Postgres y MinIO con Docker

```bash
cd Docker
docker compose -f docker-compose.dev.yml up --build
```

Esto levanta:
- **PostgreSQL** en `localhost:5432` (usuario `gradox` / `gradox-local-password`, BD `gradoxdb`).
- **MinIO** en `localhost:9000` (S3) y consola en `localhost:9001` (usuario `minioadmin`).
- **La app** (Java 21, volúmenes del código fuente) en `http://localhost:8080`.

La web se inicia por separado siguiendo las instrucciones de la sección 3.

### 5.2 Variables de entorno

El perfil `local` lee esas variables desde el entorno. Para desarrollo los valores
que usan por defecto (sin necesidad de `.env`) son:

| Variable | Uso | Default en dev |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC | `jdbc:postgresql://db:5432/gradoxdb` (ver 5.1) |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | credenciales PG | `gradox` / `gradox-local-password` |
| `S3_ENDPOINT` | endpoint S3 | `http://minio:9000` |
| `S3_ACCESS_KEY` / `S3_SECRET_KEY` | credenciales | `minioadmin` |
| `S3_BUCKET_NAME` | bucket | `gradox2-files` |
| `SPRING_MAIL_HOST` / `PORT` | SMTP | `localhost` / `1025` (mailcatcher/smtp local) |
| `JWT_SECRET` | firma HS256 | `local-dev-jwt-secret-min-32-bytes-long` |

### 5.3 `gradox2/.env`

`run.sh` carga `gradox2/.env` (si existe) antes de arrancar la app. Spring Boot
no lee archivos `.env` por sí solo, así que ese archivo es la forma de fijar
credenciales locales (por ejemplo SMTP) sin exportarlas a mano en cada terminal.
No se versiona (está en `.gitignore`).

Puedes crearla desde la plantilla opcional con `cp gradox2/.env.example gradox2/.env`.
No reutilices `Docker/.env` aquí: ese archivo contiene configuración sensible
para el compose de producción/VPS.

## 6. Perfiles

| Perfil | Uso |
|---|---|
| `local` | desarrollo (Postgres local + MinIO, Swagger activo) |
| `test` | tests de integración (H2 + fake S3, Flyway off) |
| `prod` | producción (Supabase + R2, Swagger off) |

No existe un perfil `docker`: el entorno dev usa el perfil `local`.

## 7. Tests

```bash
cd gradox2
./mvnw test   # 213 tests
```

Más detalle en [TESTING.md](./TESTING.md).

## 8. Swagger (local)

En los perfiles de desarrollo (`local`) Swagger/OpenAPI está disponible en:

- UI: `http://localhost:8080/swagger-ui.html` (o `/swagger-ui/index.html`)
- JSON: `http://localhost:8080/v3/api-docs`

> En `prod` está desactivado a propósito.
