# 📂 Arquitectura del Proyecto Gradox 2.0

Este documento describe la **estructura de carpetas, clases y capas** del proyecto **Gradox 2.0**, así como las responsabilidades de cada componente y su relación dentro del sistema.

El proyecto sigue una **arquitectura en capas** inspirada en **Clean Architecture** y **Domain-Driven Design (DDD light)**:
- **Capa de Persistencia:** Entidades JPA y Repositorios.
- **Capa de Negocio:** Lógica de aplicación, reglas de negocio y casos de uso.
- **Capa de Presentación:** API REST, controladores y DTOs.
- **Seguridad:** Autenticación, autorización y configuración.
- **Utilidades:** Mappers y helpers reutilizables.

---

## 🏗️ Estructura General

```plaintext
gradox2/
├── Gradox2Application.java          # Clase principal (Spring Boot)
│
├── persistence/                     # Capa de datos (modelo y acceso a BD)
│   ├── entities/                    # Entidades JPA
│   │   ├── User.java                # Usuario, roles, reputación
│   │   ├── File.java                # Archivos subidos
│   │   ├── ExamThread.java          # Hilos de exámenes
│   │   ├── ExamResponse.java        # Respuestas a hilos
│   │   ├── Vote.java                # Registro de votos
│   │   ├── PromotionProposal.java   # Propuestas de ascenso/descenso
│   │   ├── Notification.java        # Notificaciones internas
│   │   ├── Badge.java               # Insignias
│   │   ├── Course.java              # Curso académico
│   │   ├── Subject.java             # Asignaturas
│   │   ├── Delegation.java          # Delegación de voto
│   │   ├── AuditRecord.java         # Auditoría de acciones
│   │   └── enums/                   # Tipos y constantes
│   │       ├── ActionType.java
│   │       ├── FileType.java
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
│   │   ├── AuthServiceImpl.java
│   │   └── ...
│   │
│   └── exceptions/                  # Excepciones personalizadas
│       ├── UserNotFoundException.java
│       ├── UserAlreadyExistsException.java
│       └── UnauthenticatedAccessException.java
│
├── presentation/                    # Capa de presentación (API REST)
│   ├── controller/                  # Endpoints
│   │   ├── UserController.java
│   │   ├── AuthController.java
│   │   ├── FileController.java
│   │   └── GlobalExceptionHandler.java
│   │
│   └── dto/                         # Objetos para Requests/Responses
│       ├── auth/
│       ├── users/
│       └── files/
│
├── security/                        # Configuración de seguridad
│   ├── SecurityConfig.java          # Configuración de Spring Security
│   ├── JwtAuthFilter.java           # Filtro JWT
│   └── JwtUtils.java                # Utilidades JWT
│
└── utils/                           # Helpers y herramientas
    └── mapper/                      # MapStruct para conversión DTO/Entidad
        ├── UserMapper.java
        ├── FileMapper.java
        └── ...
