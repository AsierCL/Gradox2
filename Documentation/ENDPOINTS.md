# Endpoints de la aplicacion

---
## 🔐 1. Autenticación y Seguridad

| Método | Endpoint                       | Descripción                              | ✔️  |
| ------ | ------------------------------ | ---------------------------------------- | --- |
| POST   | `/auth/register`               | Registro con email institucional         | ✅   |
| POST   | `/auth/verify-email`           | Verificar email con token                |     |
| POST   | `/auth/login`                  | Login con email y contraseña             | ✅   |
| POST   | `/auth/token/refresh`          | Renovar token de acceso                  |     |
| POST   | `/auth/logout`                 | Cierre de sesión                         |     |
| POST   | `/auth/password/reset-request` | Solicitar reinicio de contraseña         |     |
| POST   | `/auth/password/reset`         | Confirmar cambio de contraseña con token |     |

---
## 👤 2. Usuarios

| Método | Endpoint                                       | Descripción                        | ✔️  |
| ------ | ---------------------------------------------- | ---------------------------------- | --- |
| GET    | `/users/me`                                    | Obtener perfil propio              | ✅   |
| PUT    | `/users/me`                                    | Editar perfil (nombre, alias, bio) | ✅   |
| GET    | `/users/{id}`                                  | Ver perfil público                 | ✅   |
| GET    | `/users/all`                                   | Todos los usuarios                 | ✅   |
| GET    | `/users/paged?page=0&size=5&sortBy=reputation` | Todos los usuarios paginados       | ✅   |


---
## 🛡️ 3. Roles y Promociones

| Método | Endpoint                 | Descripción                     | ✔️  |
| ------ | ------------------------ | ------------------------------- | --- |
| POST   | `/roles/promote-request` | Solicitar promoción a master    |     |
| GET    | `/roles/promote/pending` | Ver promociones pendientes      |     |
| POST   | `/roles/promote/vote`    | Votar para promover a master    |     |
| POST   | `/roles/demote-request`  | Proponer expulsión de un master |     |
| POST   | `/roles/demote/vote`     | Votar expulsión de master       |     |

---
## ➕ 4. Propuestas de archivos

| Método | Endpoint                        | Descripción                        | ✔️  |
| ------ | ------------------------------- | ---------------------------------- | --- |
| POST   | `/uploadProposal/upload`        | Proponer nuevo archivo             | ✅   |
| GET    | `/uploadProposal/all`           | Listar archivos todos los archivos | ✅   |
| GET    | `/uploadProposal/{id}`          | Ver datos del archivo              | ✅   |
| GET    | `/uploadProposal/{id}/download` | Descargar archivo                  | ✅   |
| DELETE | `/uploadProposal/{id}`          | Borrar propuesta de archivo        | ✅   |

---
## 📁 5. Archivos

| Método | Endpoint                    | Descripción                              | ✔️  |
| ------ | --------------------------- | ---------------------------------------- | --- |
| GET    | `/files/all`                | Listar archivos todos los archivos       | ✅   |
| GET    | `/files/{id}`               | Ver datos del archivo                    | ✅   |
| GET    | `/files/{id}/download`      | Descargar archivo                        | ✅   |
| DELETE | `/files/{id}`               | Proponer eliminación (requiere votación) |     |
| POST   | `/files/{id}/vote/{upvote}` | Votar para puntuar un archivo            |     |
| DELETE | `/files/{id}/vote`<br>      | Quitar votacion de un archivo            |     |


---
## 📝 6. Hilos y Exámenes

| Método | Endpoint              | Descripción                     | ✔️  |
| ------ | --------------------- | ------------------------------- | --- |
| POST   | `/threads`            | Crear hilo para un examen       |     |
| GET    | `/threads`            | Listar hilos por asignatura     |     |
| GET    | `/threads/{id}`       | Ver hilo y respuestas           |     |
| POST   | `/threads/{id}/reply` | Responder a hilo con resolución |     |
| POST   | `/threads/{id}/vote`  | Votar en respuestas             |     |

---
## 🚨 7. Moderación

| Método | Endpoint                           | Descripción                  | ✔️  |
| ------ | ---------------------------------- | ---------------------------- | --- |
| POST   | `/moderation/report`               | Denunciar contenido          |     |
| GET    | `/moderation/reports`              | Ver denuncias (solo masters) |     |
| POST   | `/moderation/reports/{id}/resolve` | Resolver denuncia            |     |

---
## 🔔 8. Notificaciones

| Método | Endpoint              | Descripción                    | ✔️  |
| ------ | --------------------- | ------------------------------ | --- |
| GET    | `/notifications`      | Ver notificaciones del usuario |     |
| POST   | `/notifications/read` | Marcar como leídas             |     |

---
## 📊 9. Métricas y Estadísticas

| Método | Endpoint             | Descripción              | ✔️  |
| ------ | -------------------- | ------------------------ | --- |
| GET    | `/stats`             | Estadísticas generales   |     |
| GET    | `/stats/assignments` | Actividad por asignatura |     |
| GET    | `/stats/activity`    | Actividad por usuario    |     |

---
## 🗳️ 10. Sistema de Votaciones (Global)

| Método | Endpoint              | Descripción                           | ✔️  |
| ------ | --------------------- | ------------------------------------- | --- |
| GET    | `/vote/{id}`          | Ver mi voto en una propusta           | ✅   |
| GET    | `/vote/{id}/results`  | Ver resultado actual de una propuesta | ✅   |
| POST   | `/vote/{id}/{upvote}` | Votar en una propuesta (true/false)   | ✅   |
| DELETE | `/vote/{id}`          | Borrar mi voto                        | ✅   |

---
## ⚙️ 11. Administración (Solo masters)

| Método | Endpoint                  | Descripción                       | ✔️  |
| ------ | ------------------------- | --------------------------------- | --- |
| PUT    | `/admin/users/{id}/ban`   | Banear usuario                    |     |
| PUT    | `/admin/users/{id}/unban` | Rehabilitar usuario               |     |
| GET    | `/admin/logs`             | Ver logs de actividad del sistema |     |
| PUT    | `/admin/config`           | Ajustar parámetros del sistema    |     |

---
