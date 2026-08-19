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
- **Git**:
  ```bash
  git --version
  ```

## 2. Clonar el Proyecto

```bash
git clone https://github.com/AsierCL/Gradox2.git
cd Gradox2
```

## 3. Entorno de desarrollo (recomendado)

El script `run.sh` vive en `gradox2/`. Levanta **PostgreSQL + MinIO + la app**
con hot-reload en un stack Docker:

```bash
cd gradox2
./run.sh dev        # levanta app + Postgres + MinIO
./run.sh dev-down   # detiene el entorno de desarrollo
```

Con esto la app queda disponible en `http://localhost:8080` y la consola de
**MinIO** en `http://localhost:9001` (usuario `minioadmin` / `minioadmin`). El
bucket usado en desarrollo es `gradox2-files` (se auto-crea al arrancar).

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
./mvnw test   # 117 tests
```

Más detalle en [TESTING.md](./TESTING.md).

## 8. Swagger (local)

En los perfiles de desarrollo (`local`) Swagger/OpenAPI está disponible en:

- UI: `http://localhost:8080/swagger-ui.html` (o `/swagger-ui/index.html`)
- JSON: `http://localhost:8080/v3/api-docs`

> En `prod` está desactivado a propósito.