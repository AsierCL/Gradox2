# Wave 1 — Hardening de seguridad y datos

Ocho puntos de la revisión ("Ola 1"): cerrar agujeros de autorización, sesión, rate limiting y
corrupción de datos antes de sumar funcionalidad. Sin features nuevas.

Deploy objetivo: backend en **Render** (proxy propio, una instancia, memoria acotada), frontend en
**GitHub Pages**. Los límites de Render condicionan S3 (proxy) y S4 (rate limiter en memoria).

## Specs

- **S1** (punto 1) La descarga de una propuesta pendiente exige pertenecer al grado del recurso y poder
  ver el contenido según su visibilidad. Se rechaza con `403 DEGREE_SCOPE_FORBIDDEN` a quien es de otro
  grado y con `404 NOT_FOUND` a quien no puede ver un contenido `PRIVATE` ajeno. El `ADMIN` de plataforma
  queda exento del ámbito de grado (supervisión), nunca del chequeo de existencia.
- **S2** (punto 2) Cambiar la contraseña invalida todas las sesiones del usuario: incrementa
  `token_version` (los access tokens previos dejan de ser válidos, `401 UNAUTHENTICATED_ACCESS`) y revoca
  los refresh tokens existentes (`401` al intentar refrescar). La contraseña nueva sigue funcionando.
- **S3** (punto 3, L2) La identidad usada por el rate limiting no se toma del primer valor de
  `X-Forwarded-For`. Se resuelve con un número explícito de saltos de proxy de confianza
  (`RATE_LIMIT_TRUSTED_PROXY_HOPS`): `0` ignora la cabecera por completo; `1` toma el último salto, que es
  el que agrega el proxy de Render. Un cliente que falsifica la cabecera no obtiene buckets nuevos.
- **S4** (punto 4, L2) El rate limiter en memoria está acotado (tamaño máximo y expiración por acceso) para
  no crecer sin techo en el contenedor de Render, y el bucket de votaciones no se comparte entre usuarios
  anónimos: sin autenticación no se aplica límite de voto (la petición termina en `401`).
- **S5** (punto 5, L2) CORS responde solo a orígenes configurados: por defecto `https://gradox2.github.io`;
  en desarrollo local, además `http://localhost:5173`, `http://localhost:4173`, `http://localhost:3000`.
  Un origen no listado no recibe `Access-Control-Allow-Origin` y no puede emitir peticiones con credenciales.
- **S6** (punto 6) Invariantes en base de datos: como máximo un `score` por `(file_id, voter_id)` y como
  máximo `max_pending_uploads` propuestas de subida pendientes por usuario, incluso con peticiones
  concurrentes. La migración deduplica filas existentes antes de crear el índice único.
- **S7** (punto 7) Existen índices para las consultas que la aplicación ya ejecuta (votos por propuesta,
  propuestas vencidas, propuestas por proponente/estado, archivos y temporales por asignatura/usuario,
  refresh tokens por usuario, auditoría por fecha).
- **S8** (punto 8) Ningún borrado de objeto en S3 ocurre dentro de la transacción de base de datos: se
  ejecuta solo después del commit, de modo que un rollback no destruye el archivo.

## Tasks

| ID | Specs | Ruta | Commit |
| --- | --- | --- | --- |
| T1 | S1 | inline | `fix(security): require degree scope to download pending proposals` |
| T2 | S2 | inline | `fix(auth): invalidate sessions on password change` |
| T3 | S3 | inline | `fix(security): resolve rate-limit client ip from trusted proxy hops` |
| T4 | S4 | inline | `fix(security): bound in-memory rate limiter and skip anonymous vote bucket` |
| T5 | S5 | inline | `fix(security): restrict cors to configured origins` |
| T6 | S6 | inline | `fix(data): enforce score uniqueness and serialize pending upload quota` |
| T7 | S7 | inline | `perf(db): add indexes for existing query paths` |
| T8 | S8 | inline | `fix(files): delete s3 objects after commit` |
| T9 | S3-S5 | inline | `docs(ops): document render proxy, cors and rate limit settings` |

## Log

- **L1** — "El proyecto ya está listo, subido y funcionando. Revisalo al completo, mira que se puede
  mejorar, que se puede optimizar, y como podemos subirlo aun mas de nivel."
- **L2** — "Vale. Para todo el tema de rate limit y http/https, recuerda. El deploy es el backend en
  Render, y el frontend en Github pages. Ten cuidado con los limits y todo eso.

  Puedes proceder a la Ola 1, los 8 primeros puntos. Si tengo que hacer alguna decision avisame"
- **L3** — Decisión de implementación (sin consulta al humano, dentro de L2): el rate limiting queda en
  memoria acotado en lugar de Redis porque Render free corre una sola instancia; se documenta el supuesto
  y el ítem de observabilidad de la Ola 2 lo revisa si se escala a más de una.
- **L4** — Decisión de implementación: `server.forward-headers-strategy` pasa a `none` en todos los
  perfiles y la resolución de IP del cliente queda en `ClientIpResolver`, para que la política de proxies
  de confianza tenga un solo dueño explícito y testeable.
- **E1** — Evidencia: `mvn test` antes de empezar → 182 tests, 0 fallos (2026-11-18).
