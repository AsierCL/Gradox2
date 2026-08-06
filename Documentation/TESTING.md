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
  red ni se necesita MinIO/R2 para pasar los tests.
- Ejecución más reciente: **59 tests, 0 fallos, 0 errores**.

## Suite actual

### 1) Gradox2ApplicationTests — 1 caso
Archivo: [gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java](../gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java)

- `contextLoads`
  - Verifica que el contexto de Spring Boot arranca correctamente.

### 2) AuthRateLimitIntegrationTest — 2 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java)

- Simula intentos de login fallidos desde la misma IP.
- Verifica `429 TOO_MANY_REQUESTS` con `errorCode = RATE_LIMIT_EXCEEDED` al superar el límite.

### 3) ApiIntegrationTest — 30 casos
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

### 4) AuthRegistrationIntegrationTest — 6 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRegistrationIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AuthRegistrationIntegrationTest.java)

#### Registro y duplicados
- Rechaza `username` duplicado (`409`, `ALREADY_EXIST_ERROR`).
- Rechaza `email` duplicado (`409`, `ALREADY_EXIST_ERROR`).
- Usuario creado deshabilitado + token de verificación + enlace con `app.base-url` configurado.

#### Verificación de cuenta
- Token válido habilita la cuenta y se elimina el token.
- Token expirado se rechaza y se elimina; no habilita al usuario.
- Token inexistente se rechaza.

### 5) GovernanceRulesIntegrationTest — 8 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/GovernanceRulesIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/GovernanceRulesIntegrationTest.java)

#### Reglas de quorum y umbral
- Propuesta permanece pendiente por debajo del quorum.
- Propuesta permanece pendiente si no alcanza el porcentaje mínimo de aprobación.
- Propuesta se aprueba y publica cuando se cumplen quorum y umbral.

#### Configuración de voto
- Cada propuesta guarda snapshot de `quorumRequired` y `approvalThreshold` al crearse.
- Fallback: si no existe `VoteConfig`, se crea la configuración por defecto.

#### Despromoción gobernada
- La despromoción crea propuesta de tipo `EXPULSION` y aplica el downgrade al aprobarse.
- Rechazo cuando el candidato no es `MASTER`.
- `404 NOT_FOUND` para candidatos inexistentes.

### 6) AuthSessionIntegrationTest — 7 casos
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

### 7) AdminAndFileDeletionIntegrationTest — 5 casos
Archivo: [gradox2/src/test/java/com/example/gradox2/AdminAndFileDeletionIntegrationTest.java](../gradox2/src/test/java/com/example/gradox2/AdminAndFileDeletionIntegrationTest.java)

#### Administración de usuarios
- Un master puede banear y rehabilitar usuarios; el baneado no puede autenticarse.
- Un usuario normal no puede acceder al ban.

#### Eliminación de archivos por votación
- La propuesta de borrado expone metadatos y permanece pendiente por debajo del quorum.
- Al alcanzar quorum, la propuesta se aprueba y el archivo se elimina (objeto S3 + registro).
- No se puede crear una segunda propuesta de borrado mientras haya una activa (`400`, `INVALID_FILE_OPERATION`).

---

## Resumen de cobertura actual

- **Smoke**: arranque de contexto Spring Boot.
- **Seguridad de acceso**: endpoints protegidos, login válido/inválido, rate limiting de login.
- **Gestión de sesión**: refresh persistente, rotación, logout, invalidación al cambiar la contraseña.
- **Reset de contraseña**: solicitud, correo, confirmación y rechazo de tokens inválidos.
- **Validaciones de entrada**: payloads inválidos, límites de paginación, IDs no válidos.
- **Auth y verificación**: duplicados de registro, habilitación por token, rechazo de tokens expirados/inexistentes.
- **Gobernanza de propuestas**: quorum, umbral, snapshot de configuración, fallback.
- **Roles y administración**: despromoción con propuesta de expulsión, ban/unban por master.
- **E2E funcional**: propuesta → voto → publicación → votación sobre archivo → retractación.
- **Almacenamiento S3**: subida/descarga/borrado ejercitados contra el **fake S3 in-memory** (`TestS3Config`).

## Observación

- Si cambian los tests o se añaden ejecuciones dinámicas/parametrizadas, refresca
  este documento junto con el último `./mvnw test` para mantener alineado el total
  reportado por Maven (hoy **59**).
