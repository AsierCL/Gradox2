# 📂 Arquitectura del Proyecto Gradox 2.0

Este documento describe la **estructura de carpetas, clases y capas** del proyecto **Gradox 2.0**, así como las responsabilidades de cada componente y su relación dentro del sistema.

El proyecto sigue una **arquitectura en capas** inspirada en **Clean Architecture** y **Domain-Driven Design (DDD light)**:

- **Capa de Persistencia:** Entidades JPA, Repositorios y **almacenamiento externo (S3)**.
- **Capa de Negocio:** Lógica de aplicación, reglas de negocio y casos de uso.
- **Capa de Presentación:** API REST, controladores y DTOs.
- **Seguridad:** Autenticación, autorización y configuración.
- **Infraestructura:** Configuración de S3, OpenAPI y utilidades.

---

## 🗄️ Persistencia y almacenamiento

Gradox 2.0 separa los **datos relacionales** de los **binarios**:

| Dato | Dónde vive |
|---|---|
| Entidades relacionales (usuarios, propuestas, votos, configuración…) | **PostgreSQL (Supabase)** vía Flyway + JPA |
| Documentos aprobados (`files`) y propuestos (`temp_files`) | **S3 compatible** (R2 en prod, MinIO en dev); en BD solo la columna `object_key` |
| `badges.icon` y `users.profile_picture` | aún como large objects `oid` en PostgreSQL (pendiente de migrar) |

El acceso a S3 se concentra en un único servicio (ver `S3StorageService`), que
depende de un bean `S3Client` (AWS SDK v2) configurado en `S3Config`. En el perfil
`test` se sustituye por un **fake in-memory** (`TestS3Config`) para no tocar red.

```
                     ┌──────────────────────────────┐
   Cliente / navegador│  app  (gradox2_app)          │
        │             │  - Spring Boot / Java 21     │
        │  HTTP :8080 │  - JWT + BCrypt              │
        └────────────▶│  - Flyway (migraciones)      │
                      │  - S3StorageService          │
                      └──────┬──────────────┬────────┘
                             │ JDBC :5432   │ S3 (put/get/delete)
                             ▼              ▼
                  ┌─────────────────┐ ┌─────────────────┐
                  │  Supabase  PG   │ │ R2 (prod) /     │
                  │  (gestionado)   │ │ MinIO (dev)     │
                  └─────────────────┘ └─────────────────┘
```

---

## 🏗️ Estructura General

```plaintext
gradox2/
├── Gradox2Application.java          # Clase principal (Spring Boot)
│
├── persistence/                     # Capa de datos (modelo y acceso a BD)
│   ├── entities/                    # Entidades JPA
│   │   ├── User.java                # Usuario, roles, reputación, enabled
│   │   ├── File.java                # Archivos aprobados (objectKey → S3)
│   │   ├── TempFile.java            # Archivos en propuesta (objectKey → S3)
│   │   ├── FileProposal.java        # Propuesta de subida de archivo
│   │   ├── Proposal.java            # Base de propuestas (votables)
│   │   ├── PromotionProposal.java   # Propuestas de ascenso/descenso
│   │   ├── Vote.java                # Registro de votos
│   │   ├── Score.java               # Puntuación de archivos
│   │   ├── GlobalConfig.java        # Configuración de votaciones
│   │   ├── Course.java              # Curso académico
│   │   ├── Subject.java             # Asignaturas
│   │   ├── Delegation.java          # Delegación de voto
│   │   ├── Badge.java               # Insignias (icon en PG, oid)
│   │   ├── Notification.java        # Notificaciones internas
│   │   ├── AuditRecord.java         # Auditoría de acciones
│   │   ├── RefreshToken.java        # Sesiones de refresh
│   │   ├── VerificationToken.java   # Tokens de verificación de email
│   │   ├── PasswordResetToken.java  # Tokens de reset de contraseña
│   │   └── enums/                   # Tipos y constantes
│   │       ├── ActionType.java
│   │       ├── FileType.java
│   │       ├── FileVisibility.java
│   │       ├── ProposalStatus.java
│   │       └── UserRole.java
│   │
│   └── repository/                  # Interfaces para CRUD con JPA
│       ├── UserRepository.java
│       ├── FileRepository.java
│       ├── VoteRepository.java
│       └── ...
│
├── service/                         # Capa de negocio
│   ├── interfaces/                  # Contratos de servicio
│   │   ├── IUserService.java
│   │   ├── IFileService.java
│   │   ├── IAuthService.java
│   │   └── ...
│   │
│   ├── implementation/              # Implementación de casos de uso
│   │   ├── UserServiceImpl.java
│   │   ├── FileServiceImpl.java
│   │   ├── FileProposalServiceImpl.java
│   │   ├── AuthServiceImpl.java
│   │   ├── RoleServiceImpl.java
│   │   ├── VoteServiceImpl.java
│   │   ├── GlobalConfigService.java
│   │   └── S3StorageService.java    # PUT/GET/DELETE de objetos + bucket
│   │
│   └── exceptions/                  # Excepciones personalizadas
│       ├── NotFoundException.java
│       ├── AlreadyExistsException.java
│       ├── UnauthenticatedAccessException.java
│       ├── InternalServerErrorException.java
│       ├── InvalidFileOperation.java
│       ├── InvalidRoleOperationException.java
│       ├── ProposalClosedException.java
│       └── RateLimitExceededException.java
│
├── presentation/                    # Capa de presentación (API REST)
│   ├── controller/                  # Endpoints
│   │   ├── AuthController.java      # /api/auth/*
│   │   ├── UserController.java      # /users/*
│   │   ├── FileController.java      # /files/*
│   │   ├── FileProposalController.java  # /uploadProposal/*
│   │   ├── VoteController.java      # /vote/*
│   │   ├── VoteConfigController.java   # /vote-config
│   │   ├── RolesController.java     # /promoteProposal/*
│   │   ├── AdminController.java     # /admin/users/* (ban/unban)
│   │   ├── HealthController.java    # /health
│   │   └── GlobalExceptionHandler.java
│   │
│   └── dto/                         # Objetos para Requests/Responses
│       ├── auth/
│       ├── users/
│       └── files/
│
├── config/                          # Configuración de infraestructura
│   ├── S3Config.java                # Bean S3Client (perfil != test, path-style)
│   └── OpenApiConfig.java           # Springdoc/OpenAPI
│
├── security/                        # Configuración de seguridad
│   ├── SecurityConfig.java          # Configuración de Spring Security
│   ├── JwtAuthFilter.java           # Filtro JWT
│   ├── JwtUtils.java                # Utilidades JWT
│   ├── RateLimitFilter.java         # Rate limiting HTTP
│   └── InMemoryRateLimiter.java     # Backend in-memory del rate limiter
│
├── init/                            # Bootstrap
│   ├── DataLoader.java              # Datos demo (perfiles local/demo)
│   └── MasterBootstrap.java         # Primer MASTER vía env (perfil prod)
│
└── utils/                           # Helpers y herramientas reutilizables
```

---

## 🔧 S3 (objetos) en detalle

### `S3Config` (`config/S3Config.java`)
- Crea el bean `S3Client` (AWS SDK v2) a partir de `s3.*` del perfil activo.
- **`@Profile("!test")`** — en el perfil `test` no se crea (se usa el fake).
- `pathStyleAccessEnabled(true)` — necesario para **MinIO** y **Cloudflare R2**.
- `Region.of(s3.region)` con `s3.region=auto` (R2/MinIO no usan regiones AWS).

### `S3StorageService` (`service/implementation/S3StorageService.java`)
- Inyecta `S3Client` + `s3.bucket-name`.
- `put(byte[]) → String`: sube un objeto con key `UUID.randomUUID()` y devuelve la key.
- `get(String) → byte[]`: descarga el objeto por key.
- `delete(String)`: elimina el objeto (no-op si key nula/vacía).
- `ensureBucketExists()` (`@PostConstruct`): en arranque, comprueba el bucket con
  `headBucket` y lo crea si falta (**best-effort**: si el bucket no es accesible,
  no aborta el arranque; en R2 el bucket debe existir previamente).
- Lanza `InternalServerErrorException` ante errores de S3.

### Flujo de los documentos
1. **Propuesta**: `FileProposalServiceImpl.uploadFileProposal()` sube los bytes a S3 y guarda la `objectKey` en `TempFile`.
2. **Aprobación**: `VoteServiceImpl.applyFileProposal()` copia la `objectKey` del `TempFile` al `File` (no se re-suben bytes).
3. **Descarga**: `FileServiceImpl.downloadFile()` y `FileProposalController.downloadProposalFile()` leen desde S3 con `get(objectKey)`.
4. **Borrado**: tanto al rechazar una propuesta como al eliminar un archivo aprobado, se borra el objeto de S3 antes del borrado lógico/JPA.

### Tests
En `src/test/java/.../config/TestS3Config.java` existe un `S3Client` **en memoria**
(`@Primary` bajo `@Profile("test")`) que imita `putObject`/`getObject`/`deleteObject`
sobre un `ConcurrentHashMap`, para que los tests de integración no dependan de red.

---

## 🔐 Seguridad

- **Spring Security** stateless con **JWT** (HS256) y contraseñas **BCrypt**.
- Roles: `GUEST`, `USER`, `MASTER` con `@EnableMethodSecurity`.
- Refresh tokens revocables persistidos en BD; rate limiting en login/register.
- `JwtAuthFilter` extrae y valida el token; `RateLimitFilter` limita por IP.
- OpenAPI (Swagger) desactivado en `prod`.
