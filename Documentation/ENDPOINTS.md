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

| Método | Endpoint                | Descripción                        | ✔️  |
| ------ | ----------------------- | ---------------------------------- | --- |
| GET    | `/users/me`             | Obtener perfil propio              | ✅   |
| PUT    | `/users/me`             | Editar perfil (nombre, alias, bio) | ✅   |
| GET    | `/users/{id}`           | Ver perfil público                 | ✅   |
| GET    | `/users`                | Buscar usuarios                    | ✅   |
| GET    | `/users/reputation/top` | Ranking por reputación             |     |


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
## 📁 4. Repositorio de Archivos

| Método | Endpoint              | Descripción                                    | ✔️  |
| ------ | --------------------- | ---------------------------------------------- | --- |
| POST   | `/files/upload`       | Subir archivo (curso, asignatura, descripción) |     |
| GET    | `/files`              | Listar archivos                                |     |
| GET    | `/files/{id}`         | Ver/descargar archivo                          |     |
| DELETE | `/files/{id}`         | Proponer eliminación (requiere votación)       |     |
| POST   | `/files/{id}/vote`    | Votar sobre archivo (subida o eliminación)     |     |
| GET    | `/files/{id}/history` | Ver historial de cambios, votos y denuncias    |     |

---
## 📝 5. Hilos y Exámenes

| Método | Endpoint              | Descripción                     | ✔️  |
| ------ | --------------------- | ------------------------------- | --- |
| POST   | `/threads`            | Crear hilo para un examen       |     |
| GET    | `/threads`            | Listar hilos por asignatura     |     |
| GET    | `/threads/{id}`       | Ver hilo y respuestas           |     |
| POST   | `/threads/{id}/reply` | Responder a hilo con resolución |     |
| POST   | `/threads/{id}/vote`  | Votar en respuestas             |     |

---
## 🚨 6. Moderación

| Método | Endpoint                           | Descripción                  | ✔️  |
| ------ | ---------------------------------- | ---------------------------- | --- |
| POST   | `/moderation/report`               | Denunciar contenido          |     |
| GET    | `/moderation/reports`              | Ver denuncias (solo masters) |     |
| POST   | `/moderation/reports/{id}/resolve` | Resolver denuncia            |     |

---
## 🔔 7. Notificaciones

| Método | Endpoint              | Descripción                    | ✔️  |
| ------ | --------------------- | ------------------------------ | --- |
| GET    | `/notifications`      | Ver notificaciones del usuario |     |
| POST   | `/notifications/read` | Marcar como leídas             |     |

---
## 📊 8. Métricas y Estadísticas

| Método | Endpoint             | Descripción              | ✔️  |
| ------ | -------------------- | ------------------------ | --- |
| GET    | `/stats`             | Estadísticas generales   |     |
| GET    | `/stats/assignments` | Actividad por asignatura |     |
| GET    | `/stats/activity`    | Actividad por usuario    |     |

---
## 🗳️ 9. Sistema de Votaciones (Global)

| Método | Endpoint                     | Descripción                   | ✔️  |
| ------ | ---------------------------- | ----------------------------- | --- |
| GET    | `/votes/pending`             | Ver propuestas activas        |     |
| POST   | `/votes/{proposalId}`        | Emitir voto                   |     |
| GET    | `/votes/{proposalId}/result` | Ver resultado parcial o final |     |

---
## ⚙️ 10. Administración (Solo masters)

| Método | Endpoint                  | Descripción                       | ✔️  |
| ------ | ------------------------- | --------------------------------- | --- |
| PUT    | `/admin/users/{id}/ban`   | Banear usuario                    |     |
| PUT    | `/admin/users/{id}/unban` | Rehabilitar usuario               |     |
| GET    | `/admin/logs`             | Ver logs de actividad del sistema |     |
| PUT    | `/admin/config`           | Ajustar parámetros del sistema    |     |

---
## 🛡 Seguridad y Control de Acceso

- Todos los endpoints protegidos con Spring Security (roles y permisos).
- Tokens JWT con expiración y refresh.
- Filtros de seguridad para rutas públicas/privadas.
- Validación por dominio del correo institucional.
