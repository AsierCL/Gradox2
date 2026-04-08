# Endpoints de la aplicacion

Este documento refleja el estado real de la API implementada en el código.
Cuando una ruta aparece marcada como no disponible, significa que está descrita como objetivo de producto, pero todavía no existe en los controladores actuales.

---
## 🔐 1. Autenticación y Seguridad
AuthController

| Método | Endpoint                           | Descripción                              | ✔️  | MVP |
| ------ | ---------------------------------- | ---------------------------------------- | --- | --- |
| POST   | `/api/auth/login`                  | Login con email y contraseña             | ✅   | ☑️  |
| POST   | `/api/auth/register`               | Registro con email institucional         | ✅   | ☑️  |
| GET    | `/api/auth/verify`                 | Verificar email con token                | ✅   | ☑️  |
| POST   | `/api/auth/token/refresh`          | Renovar token de acceso                  | ✅   | ☑️  |
| POST   | `/api/auth/logout`                 | Cierre de sesión                         | ✅   | ☑️  |
| POST   | `/api/auth/password/reset-request` | Solicitar reinicio de contraseña         | ✅   | ☑️  |
| POST   | `/api/auth/password/reset`         | Confirmar cambio de contraseña con token | ✅   | ☑️  |

---
## 👤 2. Usuarios
UserController

| Método | Endpoint                                                 | Descripción                        | ✔️  | MVP |
| ------ | ----------------------------------------------           | ---------------------------------- | --- | --- |
| GET    | `/users/me`                                              | Obtener perfil propio              | ✅   | ☑️  |
| PUT    | `/users/me`                                              | Editar perfil (nombre, alias, bio) | ✅   | ☑️  |
| GET    | `/users/{id}`                                            | Ver perfil público                 | ✅   | ☑️  |
| GET    | `/users/all`                                             | Todos los usuarios                 | ✅   | ☑️  |
| GET    | `/users/all?paged=true&page=0&size=5&sortBy=reputation`  | Todos los usuarios paginados       | ✅   | ☑️  |


---
## 🛡️ 3. Roles y Promociones
RolesController

| Método | Endpoint                       | Descripción                     | ✔️  | MVP |
| ------ | ------------------------------ | ------------------------------- | --- | --- |
| POST   | `/promoteProposal/request`     | Solicitar promoción a master    | ✅   | ☑️  |
| DELETE | `/promoteProposal/delete`      | Quitar mi solicitud a master    | ✅   | ☑️  |
| GET    | `/promoteProposal/pending`     | Ver promociones pendientes      | ✅   | ☑️  |
| GET    | `/promoteProposal/{id}`        | Ver datos por id                | ✅   | ☑️  |
| POST   | `/promoteProposal/demote/{id}` | Proponer expulsión de un master | ✅   | ☑️  |

---
## 📃 4. Propuestas de archivos
FileProposalController

| Método | Endpoint                        | Descripción                        | ✔️  | MVP |
| ------ | ------------------------------- | ---------------------------------- | --- | --- |
| POST   | `/uploadProposal/upload`        | Proponer nuevo archivo             | ✅   | ☑️  |
| GET    | `/uploadProposal/all`           | Listar archivos todos los archivos | ✅   | ☑️  |
| GET    | `/uploadProposal/{id}`          | Ver datos del archivo              | ✅   | ☑️  |
| GET    | `/uploadProposal/{id}/download` | Descargar archivo propuesto        | ✅   | ☑️  |
| DELETE | `/uploadProposal/{id}`          | Borrar propuesta de archivo        | ✅   | ☑️  |

---
## 📁 5. Archivos
FileController

| Método | Endpoint                    | Descripción                              | ✔️  | MVP |
| ------ | --------------------------- | ---------------------------------------- | --- | --- |
| GET    | `/files/all`                | Listar archivos todos los archivos       | ✅   | ☑️  |
| GET    | `/files/{id}`               | Ver datos del archivo                    | ✅   | ☑️  |
| GET    | `/files/{id}/download`      | Descargar archivo                        | ✅   | ☑️  |
| DELETE | `/files/{id}`               | Proponer eliminación con votación        | ✅   | ☑️  |
| POST   | `/files/upload`             | Proponer nuevo archivo                   | ✅   | ☑️  |
| POST   | `/files/{id}/vote/{upvote}` | Votar para puntuar un archivo            | ✅   | ☑️  |
| DELETE | `/files/{id}/vote`          | Quitar votacion de un archivo            | ✅   | ☑️  |


---
## 📝 6. Hilos y Exámenes

| Método | Endpoint              | Descripción                     | ✔️  | MVP |
| ------ | --------------------- | ------------------------------- | --- | --- |
| POST   | `/threads`            | Crear hilo para un examen       |     |     |
| GET    | `/threads`            | Listar hilos por asignatura     |     |     |
| GET    | `/threads/{id}`       | Ver hilo y respuestas           |     |     |
| POST   | `/threads/{id}/reply` | Responder a hilo con resolución |     |     |
| POST   | `/threads/{id}/vote`  | Votar en respuestas             |     |     |

---
## 🚨 7. Moderación

| Método | Endpoint                           | Descripción                  | ✔️  | MVP |
| ------ | ---------------------------------- | ---------------------------- | --- | --- |
| POST   | `/moderation/report`               | Denunciar contenido          |     |     |
| GET    | `/moderation/reports`              | Ver denuncias (solo masters) |     |     |
| POST   | `/moderation/reports/{id}/resolve` | Resolver denuncia            |     |     |

---
## 🔔 8. Notificaciones

| Método | Endpoint              | Descripción                    | ✔️  | MVP |
| ------ | --------------------- | ------------------------------ | --- | --- |
| GET    | `/notifications`      | Ver notificaciones del usuario |     |     |
| POST   | `/notifications/read` | Marcar como leídas             |     |     |

---
## 📊 9. Métricas y Estadísticas

| Método | Endpoint             | Descripción              | ✔️  | MVP |
| ------ | -------------------- | ------------------------ | --- | --- |
| GET    | `/stats`             | Estadísticas generales   |     |     |
| GET    | `/stats/assignments` | Actividad por asignatura |     |     |
| GET    | `/stats/activity`    | Actividad por usuario    |     |     |

---
## 🗳️ 10. Sistema de Votaciones (Global)

| Método | Endpoint              | Descripción                           | ✔️  | MVP |
| ------ | --------------------- | ------------------------------------- | --- | --- |
| GET    | `/vote/{id}`          | Ver mi voto en una propusta           | ✅   | ☑️  |
| GET    | `/vote/{id}/results`  | Ver resultado actual de una propuesta | ✅   | ☑️  |
| POST   | `/vote/{id}/{upvote}` | Votar en una propuesta (true/false)   | ✅   | ☑️  |
| DELETE | `/vote/{id}`          | Borrar mi voto                        | ✅   | ☑️  |

---
## ⚙️ 11. Configuración de votaciones

| Método | Endpoint        | Descripción                        | ✔️  | MVP |
| ------ | --------------- | ---------------------------------- | --- | --- |
| GET    | `/vote-config`  | Ver configuración actual de votos  | ✅   | ☑️  |
| PUT    | `/vote-config`  | Ajustar quórum y umbral de votos   | ✅   | ☑️  |

---
## ⚙️ 12. Administración (Solo masters)

| Método | Endpoint                  | Descripción                       | ✔️  | MVP |
| ------ | ------------------------- | --------------------------------- | --- | --- |
| PUT    | `/admin/users/{id}/ban`   | Banear usuario                    | ✅   | ☑️  |
| PUT    | `/admin/users/{id}/unban` | Rehabilitar usuario               | ✅   | ☑️  |
| GET    | `/admin/logs`             | Ver logs de actividad del sistema |     |     |
| PUT    | `/admin/config`           | Ajustar parámetros del sistema    |     |     |

---
## ⚙️ 13. Health

| Método | Endpoint  | Descripción                                | ✔️  | MVP |
| ------ | --------- | ------------------------------------------ | --- | --- |
| GET    | `/health` | Responde 200 OK si la app esta funcionando | ✅   | ☑️  |

---
## ⏳ 14. Funcionalidades todavía no implementadas

Estas capacidades siguen en la documentación de producto, pero todavía no existen en los controladores actuales:

- Sistema de hilos y respuestas (`/threads`)
- Moderación y denuncias (`/moderation/*`)
- Notificaciones (`/notifications/*`)
- Métricas y estadísticas (`/stats/*`)
- Administración avanzada (`/admin/logs` y `/admin/config`)
