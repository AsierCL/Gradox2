# Multi-grado

Implementación del backend multi-grado de Gradox 2.0: tres grados de la Escuela de Ingeniería, grado
inmutable por usuario y ámbito de escritura por grado. **Cero cambios de frontend** en esta iteración.

Spec de referencia: `Documentation/GRADOS.md` (decisiones D1–D13, modelo, API, catálogo de 146 asignaturas).
Ese documento es la especificación; este es el plan de ejecución y su evidencia.

## Specs

Citas literales del pedido. Los `S#` son la referencia que se usa al delegar y al verificar.

- **S1** — Grado obligatorio en el registro: «Cuando te logueas, debes escoger que grado estas cursando. Las
  opciones son Ingeniería Informatica, Ingenieria Quimica y Inteligencia Artificial.» / «1- Evidentemente
  queria decir en registro.»
- **S2** — Grado inmutable y ligado al perfil: «Esto debe quedar asociado a tu perfil (ya que verá
  comprometido tu rol y tus permisos para siempre).»
- **S3** — Participación solo en el propio grado, lectura libre: «Los estudiantes de un grado, solo pueden
  participar en su grado, es decir, subir archivos a su grado, votar, proponer masters, o promocionar ellos
  a master. No pueden hacer nada de esto en otros grados, lo único que pueden es mirar archivos.»
- **S4** — Gobernanza independiente por grado, configuración global: «2- Si, las votaciones y los masters son
  independientes de cada grado. Lo único global son las configuraciones de votación.» / «3- Si, master por
  grado» / «5- No, esto lo quiero global.»
- **S5** — Asignaturas independientes entre grados: «Cada asignatura es COMPLETAMENTE INDEPENDIENTE. Varias
  van a tener el mismo nombre, pero Álgebra de Ingenieria informatica y Álgebra de IA deben ser
  independientes.»
- **S6** — Catálogo real desde las fuentes oficiales, sin "2ª edición" en los nombres: «En los nombres no
  pongas lo de segunda edición. Traete las asignaturas todas de estes enlaces.»
- **S7** — Lectura pública con la visibilidad existente: «4- La lectura es pública para todo el mundo ya. Lo
  único que cambia es que tienen lectura como un usuario logueado, es decir, pueden ver los detalles en caso
  de que el estudiante que subiera el archivo asi lo marque (visibilidad)»
- **S8** — Sin delegación de voto: «6- No quiero delegación de voto de momento, podemos dejarlo en un TODO»
- **S9** — Usuarios existentes a Informática: «Todos los usuarios que existen ahora pasan a Ingeniería
  informatica.» / «7- Si, confirmo, los existentes van a informatica.»
- **S10** — Comentar cuenta como participar: «Si, comentar cuenta como participar»
- **S11** — Configuración global propuesta por masters de cualquier grado y votada por todos: «Si, masters de
  cualquier grado y se vota»
- **S12** — El admin de plataforma no vota: «No»
- **S13** — Sin cambios de frontend, con documentación completa para ese equipo: «En esta iteración no quiero
  tocar NADA DE FRONTEND, eso se lo dejamos a otro equipo, pero si que necesito que documentes todos los
  cambios y como van a funcionar para que el equipo de frontend pueda trabajar y poner la web acorde al
  backend.»
- **S14** — Pantalla por defecto post-login, requisito **diferido** a FE: «En el frontend me gustaría que una
  vez que te logueas, salga como predeterminado la pantalla de tu grado.»

## Tareas

Ordenadas; cada una es un commit de unidad de trabajo. «Superficie» es el límite de edición si se delega.

| ID | S# | Tarea | Superficie | Checks | Commit |
|---|---|---|---|---|---|
| **T1** | S13 | Rama `feat/multi-degree` desde `main` y primer commit con la spec | `Documentation/GRADOS.md`, `README.md` | doc renderiza, links OK | `docs: add multi-degree specification and subject catalog` |
| **T2** | S2,S9 | `V10`: tabla `degrees` + 3 filas; `degree_id` en `users`/`courses`/`subjects`; `UNIQUE (degree_id, code)`; metadatos de asignatura (D13); backfill de todo a Informática; `NOT NULL` | `gradox2/src/main/resources/db/migration/V10__*.sql` | migración aplica sobre BD con datos; 0 usuarios sin grado; constraints existen | `feat(db): add degrees schema and backfill existing users` |
| **T3** | S5,S6 | `V11`: 12 cursos y 146 asignaturas; renombrar códigos de Informática a los oficiales USC **sobre las mismas filas**; actualizar `DataLoader` y fixtures de test al catálogo nuevo | `V11__*.sql`, `init/DataLoader.java`, `src/test/java/**/support/**` | 49/47/50 por grado; `files.subject_id` sin cambios; `FMAT1` ya no existe | `feat(db): seed degree catalog with 146 subjects` |
| **T4** | S2,S5 | Entidad `Degree`, `User.degree`, `Course.degree`, `Subject.degree`+metadatos, `DegreeRepository`, mappers; `MasterBootstrap` compila con grado | `persistence/entities/**`, `persistence/repository/**`, `utils/mapper/**`, `init/MasterBootstrap.java` | compila; contexto Spring levanta; tests existentes verdes | `feat(model): add Degree entity and degree scoping to domain` |
| **T5** | S1,S2 | `degreeId` obligatorio en registro + `DEGREE_REQUIRED`/`DEGREE_NOT_FOUND`; `PUT /users/me` no permite cambiarlo (`DEGREE_IMMUTABLE`) | `dto/auth/RegisterRequest.java`, `AuthServiceImpl.java`, `AuthController.java`, `UserServiceImpl.java`, `GlobalExceptionHandler.java` | V1,V2,V3 + regresión de registro | `feat(auth): require degree at registration` |
| **T6** | S4,S12 | Rol `ADMIN` de plataforma; `/admin/**` pasa a `hasRole('ADMIN')`; `degree*` e `isPlatformAdmin` en `AuthResponse` y `/users/me` | `entities/enums/UserRole.java`, `security/**`, `controller/Admin*.java`, `dto/auth/AuthResponse.java`, `dto/users/MyProfileResponse.java` | V15; `MASTER` no entra a `/admin/**`; contrato de login aditivo | `feat(security): split platform admin from degree master` |
| **T7** | S2 | `PATCH /admin/users/{id}/degree`: solo `ADMIN`, motivo obligatorio, `AuditRecord`, `tokenVersion++` | `controller/AdminController.java`, `service/implementation/AdminServiceImpl`, `service/implementation/AuditServiceImpl.java` | auditoría registrada; tokens del usuario afectado invalidados; `403` para `MASTER` | `feat(admin): allow audited degree override` |
| **T8** | S3 | `DegreeAccessGuard` + `DEGREE_SCOPE_FORBIDDEN` en el envelope de error; derivación única del grado del recurso (§2.3) | `security/DegreeAccessGuard.java`, `service/exceptions/**`, `GlobalExceptionHandler.java` | unit test de cada derivación; `403` con código estable | `feat(security): add degree access guard` |
| **T9** | S3,S7 | Guard en las escrituras de archivos: propuesta de subida, propuesta de borrado, puntuación, cambio de visibilidad | `service/implementation/FileProposalServiceImpl.java`, `FileServiceImpl.java` | V7,V8,V10; sin `TempFile` ni cuota consumida en el rechazo | `fix(files): enforce degree scope on file writes` |
| **T10** | S3,S4,S7,S11 | Votaciones por grado: elegibilidad del votante, quórum por grado, conteo de masters por grado; config global con todos los masters (D11) y `ADMIN` excluido (D12) | `service/implementation/VoteServiceImpl.java`, `persistence/repository/{User,Vote,*Proposal}Repository.java` | V9,V13 + config global vota con masters de los tres grados | `fix(votes): scope voting and quorum per degree` |
| **T11** | S3,S4 | Promoción y expulsión por grado; guard «último master» por grado; `ADMIN` no elegible | `service/implementation/RoleServiceImpl.java`, `dto/promotionProposal/**` | V11,V12; `409 DEGREE_LAST_MASTER` por grado | `fix(roles): scope promotions and last-master guard per degree` |
| **T12** | S10 | Comentar exige estar en el grado del archivo | `service/implementation/ForumServiceImpl.java` | comentario cross-grado `403` sin persistir; propio `201` | `fix(forum): block comments across degrees` |
| **T13** | S3,S5,S7 | `/degrees`, `/degrees/{id}`, `/degrees/{id}/courses`; `degree*`+`subjectId`/`subjectCode` en DTOs; filtros `?degreeId=`; metadatos de asignatura | `controller/{Degree,Course,Subject,File}Controller.java`, `dto/**`, `service/implementation/{Degree,Course,Subject,File}ServiceImpl.java` | V5,V6,V14,V16; `GET /courses` sigue respondiendo sin filtro | `feat(api): expose degrees and degree metadata` |
| **T14** | S4 | Bootstrap operativo: `MasterBootstrap` asigna grado al master de arranque (compilación) y se documenta el SQL de promoción a `ADMIN` y de nombrado del primer master por grado. **Sin endpoint** (A1 resuelto: se hace por acceso directo a base de datos) | `init/MasterBootstrap.java`, `Documentation/GRADOS.md` (§6.4) | `BOOTSTRAP_MASTER_*` crea el usuario con grado; SQL documentado y probado contra Postgres | `chore(bootstrap): assign degree to bootstrap master` |
| **T15** | S13 | Actualizar `OpenApiContractTest` con `/degrees`, regenerar contrato y dejar constancia del breaking change | `src/test/java/**/OpenApiContractTest.java`, `Documentation/ENDPOINTS.md` | suite completa verde; `/degrees` en el spec | `test(openapi): update contract for multi-degree` |
| **T16** | S13 | Docs: `OVERVIEW`, `ARCHITECTURE`, `REPUTACION`, `README`; marcar `GRADOS.md` como implementado | `Documentation/*.md`, `README.md` | sin afirmaciones desactualizadas (§13 de la spec) | `docs: document multi-degree implementation` |

### Dependencias

```plaintext
T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8 → { T9, T10, T11, T12 } → T13 → T14 → T15 → T16
```

* T9–T12 pueden paralelizarse **solo** si las superficies no se solapan; en la práctica T10 y T11 comparten
  `VoteServiceImpl`/`RoleServiceImpl` y van en serie.
* T14 depende de T6: el rol `ADMIN` tiene que existir en el enum para poder asignarlo por SQL.
* T13 depende de T8: no se expone un DTO nuevo antes de que exista la regla que lo hace cumplir.

### Riesgos del plan

| Riesgo | Dónde | Mitigación |
|---|---|---|
| Migración de datos irreversible sobre Supabase | T2, T3 | backup previo; `UPDATE` sobre filas existentes, nunca `delete`+`insert`; verificar `files.subject_id` |
| El despliegue queda sin administrador | T6, T14 | Operativo: al desplegar T6 se promueve el usuario de arranque a `ADMIN` por SQL (`UPDATE users SET role='ADMIN'`), documentado en §6.4 |
| Se agrega una escritura y falta el guard | T9–T12 | guard único + test negativo por cada punto de escritura |
| Romper el frontend actual | T5, T6, T13 | cambios aditivos salvo `register`; breaking changes documentados en §7.4 de la spec |
| Contrato OpenAPI desactualizado | T15 | el test falla a propósito al agregar endpoints |

### Fuera de alcance

* Frontend (S13, S14): solo documentación.
* Delegación de voto (S8): queda en TODO; si se retoma, debe ser por grado.
* Estadísticas por grado.
* Cambios de esquema en `vote_config` y `delegations`.

## Log

**L1** — Pedido original:

> Ha surgido un cambio imprevisto en este proyecto. Como ves, el gradox2 ahora mismo esta planteado para una
> carrera, en la que todos suban apuntes. Pero vanos a hacer un cambio bastante sustancial (de hecho nos vamos
> a otra rama).
>
> El problema es que toda la escuela de ingeniería quiere usar la aplicación, y somos varios grados. Entonces,
> quiero hacer los siguientes cambios:
> - Cuando te logueas, debes escoger que grado estas cursando. Las opciones son Ingeniería Informatica,
>   Ingenieria Quimica y Inteligencia Artificial. Esto debe quedar asociado a tu perfil (ya que verá
>   comprometido tu rol y tus permisos para siempre).
> - Los estudiantes de un grado, solo pueden participar en su grado, es decir, subir archivos a su grado,
>   votar, proponer masters, o promocionar ellos a master. No pueden hacer nada de esto en otros grados, lo
>   único que pueden es mirar archivos.
>
> Todo esto es de momento una propuesta, quiero escuchar tus ideas y recomendaciones.
>
> A mayores, en el frontend me gustaría que una vez que te logueas, salga como predeterminado la pantalla de tu
> grado. En esta iteración no quiero tocar NADA DE FRONTEND, eso se lo dejamos a otro equipo, pero si que
> necesito que documentes todos los cambios y como van a funcionar para que el equipo de frontend pueda
> trabajar y poner la web acorde al backend.
>
> De momento, solo revisa todo y dime que opinas

**L2** — Decisiones sobre la propuesta: grado en registro (no en login); masters y votaciones independientes
por grado, configuración de votación global; master por grado; lectura pública con la visibilidad existente;
sin delegación de voto (TODO); usuarios existentes a Ingeniería Informática.

**L3** — Aclaración de dominio: cada asignatura es completamente independiente entre grados (mismo nombre, filas
distintas), y el catálogo se extrae de las tres fichas oficiales de la USC sin la mención a "2ª edición".

**L4** — Cierre de pendientes: comentar cuenta como participar; la configuración global la proponen masters de
cualquier grado y la votan todos; `ADMIN` no vota; persistir créditos/semestre/tipo.

**L6** — A1 resuelto: el `ADMIN` y el primer master de cada grado se asignan por acceso directo a base de
datos, **sin** implementar endpoint ni cambio en el bootstrap. T14 se reduce a la asignación de grado en
`MasterBootstrap` y a documentar el SQL operativo.

**L5** — Evidencia T1/T2 de la spec: catálogo extraído el 2025-11-16 y verificado cruzando la sección «por
curso» con la «por módulo» de cada ficha (49 / 47 / 50 códigos idénticos); ECTS por curso verificados;
`V8` coincide 1:1 con el plan oficial de Informática, lo que permite renombrar códigos sin repuntar `files`.
