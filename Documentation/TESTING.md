# Testing actual del proyecto

Este documento resume los casos de prueba que están activos en el proyecto y la cobertura que aportan.

## Cómo se ejecutan

```bash
cd gradox2
./run.sh test        # equivalencia: ./mvnw test
```

Notas:
- Los tests de integración usan `@ActiveProfiles("test")`.
- En el perfil `test` la BD es **H2 en memoria** (con `create-drop`) y **Flyway está
  desactivado** (las migraciones son dialecto PostgreSQL y se validan por separado).
- El acceso a **S3 se simula con un fake en memoria** (`TestS3Config`): no se toca
  red ni se necesita MinIO/R2 para pasar los tests. Incluye un **signer fake**
  (`FileUrlSigner`) que devuelve URLs firmadas sin tocar red.
- Ejecución más reciente: **117 tests, 0 fallos, 0 errores**.

## Suite actual (14 clases)

### 1) Gradox2ApplicationTests — 1 caso
Archivo: [gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java](../gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java)

- `contextLoads`
  - Verifica que el contexto de Spring Boot arranca correctamente.

### 2) AuthRateLimitIntegrationTest — 3 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java)

- `loginShouldReturnTooManyRequestsAfterFiveFailuresFromSameIp`
  - 5 intentos de login fallidos desde la misma IP → `429 TOO_MANY_REQUESTS` (`RATE_LIMIT_EXCEEDED`).
- `loginRateLimitUsesFirstIpFromForwardedForHeader`
  - El rate limiter usa la **primera** IP de `X-Forwarded-For` (no `getRemoteAddr`).
- `distinctForwardedForIpsDoNotShareRateLimitBucket`
  - IPs distintas no comparten bucket de rate limit.

### 3) AuthRegistrationIntegrationTest — 6 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRegistrationIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AuthRegistrationIntegrationTest.java)

#### Registro y duplicados
- Rechaza `username` duplicado (`409`, `ALREADY_EXIST_ERROR`).
- Rechaza `email` duplicado (`409`, `ALREADY_EXIST_ERROR`).
- Usuario creado deshabilitado + token de verificación + enlace con `app.base-url` configurado.

#### Verificación de cuenta
- Token válido habilita la cuenta y se elimina el token.
- Token expirado se rechaza y se elimina; no habilita al usuario.
- Token inexistente se rechaza.

### 4) AuthSessionIntegrationTest — 10 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthSessionIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AuthSessionIntegrationTest.java)

#### Sesión y refresh token
- Login devuelve `token` y `refreshToken`, y persiste la sesión de refresh.
- El refresh rota el token e invalida el anterior.
- El logout revoca el refresh y bloquea su reutilización.

#### Reset de contraseña
- La solicitud de reset crea el token y envía el correo con el enlace configurado.
- La solicitud es silenciosa para emails inexistentes.
- El reset cambia la contraseña e invalida las sesiones previas.
- Tokens de reset inexistentes se rechazan.

#### Revocación por `token_version`
- El access token se rechaza tras un reset (el `ver` del JWT es menor que el actual).
- El access token se rechaza para usuarios deshabilitados aunque el `ver` coincida.
- Los tokens sin claim de versión (`legacy`) se rechazan.

### 5) AdminAndFileDeletionIntegrationTest — 7 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AdminAndFileDeletionIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AdminAndFileDeletionIntegrationTest.java)

#### Administración de usuarios
- Un master puede banear y rehabilitar usuarios; el baneado no puede autenticarse.
- Un usuario normal no puede acceder al ban.
- Banear a un usuario ya baneado → `409 CONFLICT`.
- Rehabilitar a un usuario no baneado → `409 CONFLICT`.

#### Eliminación de archivos por votación
- La propuesta de borrado expone metadatos y permanece pendiente por debajo del quorum.
- Al alcanzar quorum, la propuesta se aprueba y el archivo se elimina (objeto S3 + registro).
- No se puede crear una segunda propuesta de borrado mientras haya una activa (`400`, `INVALID_FILE_OPERATION`).

### 6) ApiIntegrationTest — 30 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/ApiIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/ApiIntegrationTest.java)

#### Salud y autenticación
- `/health` público devuelve `200`.
- `/users/me` sin token devuelve `403`.
- Login correcto devuelve token y datos de usuario/rol.
- Login con contraseña incorrecta devuelve `401` y `UNAUTHENTICATED_ACCESS`.
- Payload inválido en login devuelve `400`.
- `/users/me` con token válido devuelve perfil esperado.

#### Validaciones y permisos
- `size > 100` en `/users/all` lanza validación (`ConstraintViolationException`).
- `/users/{id}` con `id <= 0` lanza validación.
- `PUT /vote-config` con usuario normal devuelve `403`.

#### Flujos complejos E2E de propuestas y votos
- Propuesta de subida → descarga del temporal → votación → publicación → detalle y
  descarga del archivo final; propuesta cerrada no se puede borrar.
- Propuesta pendiente: voto negativo, recuento, retracción y borrado por el dueño.
- Votación sobre archivo: cambio de voto y retractación con recalculo del score.

### 7) GovernanceRulesIntegrationTest — 16 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/GovernanceRulesIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/GovernanceRulesIntegrationTest.java)

#### Reglas de quorum y umbral
- Propuesta permanece pendiente por debajo del quorum.
- Propuesta permanece pendiente si no alcanza el porcentaje mínimo de aprobación.
- Propuesta se aprueba y publica cuando se cumplen quorum y umbral.

#### Configuración de voto (snapshot)
- Cada propuesta guarda snapshot de `quorumRequired` y `approvalThreshold` al crearse.
- Fallback: si no existe `VoteConfig`, se crea la configuración por defecto.
- Cada propuesta guarda snapshot de **pesos de voto** (`master_vote_weight`/`user_vote_weight`) en el primer voto y los usa para decidir el estado.

#### Cierre de propuestas expiradas
- Las propuestas expiradas se rechazan y registran auditoría con `actor = SYSTEM`.
- Las propuestas pendientes sin vencer no se tocan.
- Solo afecta a propuestas `PENDING` (no `APPROVED`).
- Se rechazan también las promociones expiradas.
- Expirar una propuesta de subida **penaliza la reputación** del uploader.

#### Despromoción gobernada
- La despromoción crea propuesta de tipo `EXPULSION` y aplica el downgrade al aprobarse.
- Rechazo cuando el candidato no es `MASTER`.
- `404 NOT_FOUND` para candidatos inexistentes.

### 8) BadgeCatalogIntegrationTest — 3 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/BadgeCatalogIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/BadgeCatalogIntegrationTest.java)

- El catálogo es **público** (no requiere autenticación) y devuelve las insignias.
- Las insignias se ordenan por nombre.
- `icon_url` solo se expone cuando la insignia tiene `icon_key` (S3) seteada; si no, el campo va `null`.

### 9) SubjectIntegrationTest — 6 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/SubjectIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/SubjectIntegrationTest.java)

- `GET /subjects` devuelve las asignaturas sembradas ordenadas por curso y código.
- `GET /subjects/{id}` devuelve el detalle.
- `GET /subjects/{id}` con id inexistente → `404 NOT_FOUND`.
- El catálogo de asignaturas se sirve desde **caché** en la segunda petición.
- Tras la **evicción de caché**, se re-consulta la BD.
- El detalle también se sirve desde caché en la segunda petición.

### 10) StatsIntegrationTest — 2 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/StatsIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/StatsIntegrationTest.java)

- `GET /stats` es **público** y devuelve la forma esperada (número de archivos, espacio, descargas…).
- Las descargas y subidas se ven reflejadas en las estadísticas.

### 11) ProfilePictureIntegrationTest — 10 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/ProfilePictureIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/ProfilePictureIntegrationTest.java)

- Subir foto persiste la `profile_picture_key` (S3) y devuelve una URL firmada.
- La foto subida aparece en `/users/me`.
- La imagen guardada es **WebP y cuadrada** (procesada por `ImageProcessingService`).
- Rechazo de imágenes que no son imagen (`400`).
- Rechazo de ficheros sobredimensionados.
- Rechazo de imágenes con dimensiones enormes (protección de memoria).
- Reemplazar la foto **elimina el objeto anterior** de S3.
- Borrar la foto limpia la clave y elimina el objeto.
- Subir/borrar foto sin autenticación → `403`.

### 12) ForumThreadIntegrationTest — 20 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/ForumThreadIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/ForumThreadIntegrationTest.java)

#### Comentarios y threads (hilo implícito por archivo)
- Comentar crea el thread implícitamente y devuelve el comentario.
- Comentar sin autenticación → rechazado.
- Comentario con contenido vacío → rechazado (`400`).
- Responder a un comentario del mismo thread → permitido.
- Responder a un comentario de otro thread → rechazado.
- Responder a un comentario desconocido → rechazado.

#### Referencias a archivos
- Comentar referenciando un archivo visible devuelve la tarjeta de referencia.
- Comentar referenciando un archivo inaccesible o desconocido → rechazado.

#### Edición y borrado
- Editar comentario solo permite al autor.
- Borrar comentario solo permite al autor o a un MASTER.
- Borrar el último comentario elimina el thread.
- Respuestas anidadas permitidas a cualquier profundidad.
- Borrar un comentario padre elimina todo su subárbol (cascade BD + servicio).
- Borrar un comentario intermedio conserva ancestros y hermanos.

#### Bloqueo de hilo
- Bloquear el thread bloquea nuevos comentarios; desbloquear los restaura.
- Bloquear solo está permitido al uploader del archivo o a un MASTER.

#### Visibilidad
- Interpolación de `PRIVATE` oculta (se menciona como privado, no se expone).
- El thread `RESTRICTED` solo es visible para usuarios autenticados.
- Paginación limita `size` y pagina por `page`.

#### Gobernanza
- La eliminación de un archivo aprobada por votación elimina thread y comentarios.

### 13) GlobalJourneyIntegrationTest — 1 caso
Archivo: [gradox2/src/test/java/com/example/gradox2/GlobalJourneyIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/GlobalJourneyIntegrationTest.java)

- `fullUserJourneyFromRegistrationToLogout`
  - Un único test E2E que recorre el ciclo completo de un usuario: registro →
    verificación → login → operaciones → refresh → logout.

### 14) EmailServiceAsyncTest — 2 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/EmailServiceAsyncTest.java](../gradox2/src/test/java/com/example/gradox2/EmailServiceAsyncTest.java)

- `sendEmailRunsOnBackgroundThread`
  - El envío de correo se ejecuta en un **hilo de fondo** (no bloquea al caller).
- `sendEmailFailureDoesNotPropagateToCaller`
  - Un fallo en el envío no se propaga a quien lo invocó (best-effort).

---

## Resumen de cobertura actual

- **Smoke**: arranque de contexto Spring Boot.
- **Seguridad de acceso**: endpoints protegidos, login válido/inválido, rate limiting de login.
- **Gestión de sesión**: refresh persistente, rotación, logout, invalidación al cambiar la contraseña.
- **Revocación JWT**: `token_version` — tokens antiguos, de usuarios deshabilitados y legacy rechazados.
- **Reset de contraseña**: solicitud, correo, confirmación y rechazo de tokens inválidos.
- **Validaciones de entrada**: payloads inválidos, límites de paginación, IDs no válidos.
- **Auth y verificación**: duplicados de registro, habilitación por token, rechazo de tokens expirados/inexistentes.
- **Gobernanza de propuestas**: quorum, umbral, snapshot de config y de pesos de voto, cierre de expiradas (con penalización de reputación), fallback.
- **Roles y administración**: ban/unban con conflictos, despromoción con propuesta de expulsión.
- **Catálogo público**: insignias ordenadas, asignaturas con caché.
- **Estadísticas**: endpoint público y reflejo de descargas/subidas.
- **Imágenes**: foto de perfil validada + procesada a WebP cuadrado, reemplazo/borrado con limpieza S3.
- **Foro**: comentarios con hilo implícito, respuestas anidadas, edición/borrado con permisos, bloqueo, visibilidad, cascada.
- **Experiencia completa**: journey E2E registro→logout.
- **Correo asíncrono**: envío en background sin propagar fallos.
- **E2E funcional**: propuesta → voto → publicación → votación sobre archivo → retractación.
- **Almacenamiento S3**: subida/descarga/borrado + URLs firmadas ejercitados contra el **fake S3 in-memory** (`TestS3Config`).

## Observación

- Si cambian los tests o se añaden ejecuciones dinámicas/parametrizadas, refresca
  este documento junto con el último `./mvnw test` para mantener alineado el total
  reportado por Maven (hoy **117**).