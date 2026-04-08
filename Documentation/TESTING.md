# Testing actual del proyecto

Este documento resume los casos de prueba que están activos en el proyecto y la cobertura que aportan.

## Cómo se ejecutan

Comando principal:

```bash
cd gradox2
./run.sh test
```

Qué hace internamente:

```bash
./mvnw test
```

Notas:
- Los tests de integración usan `@ActiveProfiles("test")`, por lo que se ejecutan contra la configuración de test.
- La ejecución más reciente de Maven reporta: **40 tests, 0 fallos, 0 errores**.
- El conjunto de suites incluye ahora cobertura explícita para sesión de auth, administración de usuarios y eliminación de archivos mediante propuesta.

## Suite actual

### 1) Gradox2ApplicationTests (1 caso)
Archivo: [gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java](gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java)

- `contextLoads`
  - Verifica que el contexto de Spring Boot arranca correctamente.

### 2) AuthRateLimitIntegrationTest (1 caso)
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java)

- `loginShouldReturnTooManyRequestsAfterFiveFailuresFromSameIp`
  - Simula 5 intentos de login fallidos desde la misma IP.
  - En el sexto intento espera `429 TOO_MANY_REQUESTS` con `errorCode = RATE_LIMIT_EXCEEDED`.

### 3) ApiIntegrationTest (12 casos)
Archivo: [gradox2/src/test/java/com/example/gradox2/ApiIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/ApiIntegrationTest.java)

#### Salud y autenticación
- `healthEndpointShouldBePublic`
  - `/health` público devuelve `200`.

- `usersMeShouldRequireAuthentication`
  - `/users/me` sin token devuelve `403`.

- `loginShouldReturnTokenForEnabledUser`
  - Login correcto devuelve token y datos de usuario/rol.

- `loginShouldFailForWrongPassword`
  - Login con contraseña incorrecta devuelve `401` y `UNAUTHENTICATED_ACCESS`.

- `loginShouldValidatePayload`
  - Payload inválido en login devuelve `400`.

- `usersMeShouldReturnProfileWhenAuthenticated`
  - `/users/me` con token válido devuelve perfil esperado.

#### Validaciones y permisos
- `usersAllShouldRejectInvalidPageSize`
  - `size > 100` en `/users/all` lanza validación (`ConstraintViolationException`).

- `userProfileShouldRejectNonPositiveId`
  - `/users/{id}` con `id <= 0` lanza validación (`ConstraintViolationException`).

- `voteConfigPutShouldRequireMasterRole`
  - `PUT /vote-config` con usuario normal devuelve `403`.

#### Flujos complejos E2E de propuestas y votos
- `uploadProposalApproveAndPublishFileShouldWorkEndToEnd`
  - Crea propuesta de subida con multipart.
  - Descarga del temporal de propuesta.
  - Votación positiva y consulta de resultados.
  - Verifica publicación en `/files/all`, detalle y descarga del archivo final.
  - Verifica que la propuesta cerrada no se puede borrar (`PROPOSAL_CLOSED`).

- `pendingProposalShouldAllowDownvoteRetractAndDeleteByOwner`
  - Crea propuesta pendiente.
  - Voto negativo y verificación de recuento.
  - Retracción de voto y nuevo recuento.
  - Borrado de propuesta por su propietario.
  - Verifica `404 NOT_FOUND` al consultarla después.

- `fileVoteShouldAllowChangeAndRetractFlow`
  - Aprueba propuesta para materializar archivo.
  - Vota archivo positivo, cambia a negativo y valida score (`-1.0`).
  - Retracta voto y valida score final (`0.0`).

### 4) AuthRegistrationIntegrationTest (6 casos)
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRegistrationIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/AuthRegistrationIntegrationTest.java)

#### Registro y duplicados
- `registerShouldRejectDuplicateUsername`
  - Rechaza registro cuando el `username` ya existe.
  - Verifica `409 CONFLICT` con `errorCode = ALREADY_EXIST_ERROR`.

- `registerShouldRejectDuplicateEmail`
  - Rechaza registro cuando el `email` ya existe.
  - Verifica `409 CONFLICT` con `errorCode = ALREADY_EXIST_ERROR`.

- `registerShouldPersistDisabledUserAndSendVerificationWithConfiguredBaseUrl`
  - Verifica que el usuario se crea deshabilitado (`enabled = false`).
  - Verifica que se crea token de verificación asociado al usuario.
  - Verifica que el enlace enviado por correo usa `app.base-url` configurado en test (`https://gradox.test`).

#### Verificación de cuenta
- `verifyShouldEnableUserForValidToken`
  - Verifica que un token válido habilita la cuenta.
  - Verifica eliminación del token tras verificación exitosa.

- `verifyShouldRejectExpiredTokenAndDeleteIt`
  - Verifica rechazo de token expirado.
  - Verifica que no habilita al usuario y elimina el token expirado.

- `verifyShouldRejectUnknownToken`
  - Verifica rechazo de token inexistente.

### 5) GovernanceRulesIntegrationTest (8 casos)
Archivo: [gradox2/src/test/java/com/example/gradox2/GovernanceRulesIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/GovernanceRulesIntegrationTest.java)

#### Reglas de quorum y umbral
- `fileProposalShouldRemainPendingBelowQuorum`
  - Verifica que una propuesta no se aprueba si no alcanza quorum, aunque todos los votos emitidos sean positivos.

- `fileProposalShouldRemainPendingWhenApprovalRatioIsInsufficient`
  - Verifica que una propuesta no se aprueba si alcanza quorum pero no el porcentaje mínimo de aprobación.

- `fileProposalShouldApproveWhenQuorumAndThresholdAreMet`
  - Verifica aprobación y publicación del archivo cuando se cumplen quorum y umbral.

#### Configuración de voto
- `proposalShouldKeepSnapshotOfVoteConfigAtCreationTime`
  - Verifica que cada propuesta guarda snapshot de `quorumRequired` y `approvalThreshold` al crearse.
  - Verifica que cambios posteriores de `VoteConfig` no alteran propuestas ya existentes.

- `uploadProposalShouldCreateDefaultVoteConfigWhenMissing`
  - Verifica fallback: si no existe `VoteConfig`, el sistema crea configuración por defecto sin devolver `500`.

#### Despromoción gobernada
- `demoteShouldCreateExpulsionProposalAndApplyRoleDowngradeWhenApproved`
  - Verifica que la despromoción crea propuesta de tipo `EXPULSION`.
  - Verifica cambio de rol final a `USER` cuando la propuesta se aprueba.

- `demoteShouldRejectWhenCandidateIsNotMaster`
  - Verifica rechazo cuando el candidato no tiene rol `MASTER`.

- `demoteShouldReturnNotFoundWhenCandidateDoesNotExist`
  - Verifica respuesta `404 NOT_FOUND` para candidatos inexistentes.

### 6) AuthSessionIntegrationTest (7 casos)
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthSessionIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/AuthSessionIntegrationTest.java)

#### Sesión y refresh token
- `loginShouldReturnAccessAndRefreshTokensAndPersistRefreshSession`
  - Verifica que el login devuelve `token` y `refreshToken`.
  - Verifica que el refresh token se persiste y no queda revocado.

- `refreshShouldRotateRefreshTokenAndInvalidatePreviousOne`
  - Verifica rotación del refresh token.
  - Verifica que el token antiguo queda revocado y ya no sirve para renovar.

- `logoutShouldRevokeRefreshTokenAndBlockReuse`
  - Verifica que el logout revoca el refresh token.
  - Verifica que el mismo token no puede reutilizarse.

#### Reset de contraseña
- `requestPasswordResetShouldStoreTokenAndSendEmail`
  - Verifica que se crea el token de reset para el usuario correcto.
  - Verifica que se envía el correo con el enlace configurado por `app.base-url`.

- `requestPasswordResetShouldBeSilentForUnknownEmail`
  - Verifica respuesta silenciosa para emails inexistentes.
  - Verifica que no se crean tokens ni se envían correos.

- `resetPasswordShouldChangePasswordAndInvalidateSessions`
  - Verifica que la contraseña cambia correctamente.
  - Verifica que se invalidan refresh tokens previos.
  - Verifica que el usuario ya no puede iniciar sesión con la contraseña antigua.

- `resetPasswordShouldRejectUnknownToken`
  - Verifica rechazo de tokens de reset inexistentes.

### 7) AdminAndFileDeletionIntegrationTest (5 casos)
Archivo: [gradox2/src/test/java/com/example/gradox2/AdminAndFileDeletionIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/AdminAndFileDeletionIntegrationTest.java)

#### Administración de usuarios
- `masterShouldBeAbleToBanAndUnbanAUser`
  - Verifica que un master puede banear y rehabilitar usuarios.
  - Verifica que el usuario baneado no puede autenticarse.

- `nonMasterShouldNotBeAbleToBanUsers`
  - Verifica que un usuario normal no puede acceder a la acción de ban.

#### Eliminación de archivos por votación
- `fileDeletionProposalShouldExposeMetadataAndRemainPendingBelowQuorum`
  - Verifica que la propuesta de borrado expone metadatos correctos.
  - Verifica que permanece pendiente cuando no se alcanza el quorum.

- `fileDeletionProposalShouldApproveAndRemoveFileWhenQuorumIsMet`
  - Verifica que al alcanzar quorum la propuesta se aprueba.
  - Verifica que el archivo se elimina del repositorio.

- `duplicateFileDeletionProposalShouldBeRejectedWhilePending`
  - Verifica que no se puede crear una segunda propuesta de borrado mientras la primera siga activa.
  - Verifica respuesta `400` con `errorCode = INVALID_FILE_OPERATION`.

---

## Resumen de cobertura actual

- **Smoke**: arranque de contexto Spring Boot.
- **Seguridad de acceso**: endpoints protegidos, login con credenciales válidas e inválidas, rate limiting de login.
- **Gestión de sesión**: login con refresh token persistente, rotación de refresh, logout, invalidación de sesiones al cambiar la contraseña.
- **Reset de contraseña**: solicitud, correo, confirmación y rechazo de tokens inválidos.
- **Validaciones de entrada**: payloads inválidos, límites de paginación, IDs no válidos.
- **Auth y verificación**: duplicados de registro, habilitación por token válido, rechazo de token expirado e inexistente.
- **Gobernanza de propuestas**: quorum, umbral, snapshot de configuración, fallback de configuración.
- **Roles y administración**: despromoción con propuesta de expulsión y ban/unban de usuarios por master.
- **E2E funcional**: ciclo completo propuesta -> voto -> publicación -> votación sobre archivo -> retractación.
- **Borrado de archivos**: propuesta de eliminación, aprobación por quorum y bloqueo de propuestas duplicadas mientras están activas.

## Observación

- Si cambian los tests de integración o se añaden ejecuciones dinámicas/parametrizadas, conviene refrescar este documento junto con el último `./run.sh test` para mantener alineado el total reportado por Maven.
