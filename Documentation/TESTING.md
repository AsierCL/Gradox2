# Testing actual del proyecto

Este documento resume **todos los casos de prueba que se están ejecutando actualmente** en el proyecto.

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
- Los tests de integración usan `@ActiveProfiles("test")`, por lo que se ejecutan contra configuración de test.
- El reporte actual en `target/surefire-reports` indica: **14 tests, 0 fallos, 0 errores**.

## Suite actual

### 1) Gradox2ApplicationTests (1 caso)
Archivo: [gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java](gradox2/src/test/java/com/example/gradox2/Gradox2ApplicationTests.java)

- `contextLoads`
  - Verifica que el contexto de Spring Boot arranca correctamente.

### 2) AuthRateLimitIntegrationTest (1 caso)
Archivo: [gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java](gradox2/src/test/java/com/example/gradox2/AuthRateLimitIntegrationTest.java)

- `loginShouldReturnTooManyRequestsAfterFiveFailuresFromSameIp`
  - Simula 5 intentos de login fallidos desde la misma IP (espera `401`).
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
  - Verifica que propuesta cerrada no se puede borrar (`PROPOSAL_CLOSED`).

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
