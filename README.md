# Gradox 2

**Gradox 2** es la modernización de la antigua plataforma *Gradox*, un proyecto colaborativo que permite compartir apuntes, ejemplos de ejercicios, exámenes y material de estudio entre estudiantes.

El objetivo es crear una red **colaborativa, segura y democrática**, con un sistema de **votaciones, reputación y gobernanza distribuida** que garantice la calidad del contenido y la transparencia en la gestión, y que permita su mantenimiento en el tiempo.

Backend **Spring Boot (Java 21)** con **PostgreSQL gestionado (Supabase)** y almacenamiento de archivos en **S3 compatible (Cloudflare R2 en producción, MinIO en desarrollo)**.

---

## 📖 Documentación

- [Visión general y propuesta completa](./Documentation/OVERVIEW.md)
- [Arquitectura del sistema](./Documentation/ARCHITECTURE.md)
- [Instalación en desarrollo](./Documentation/INSTALL.md)
- [Despliegue y operación (DevOps)](./Documentation/DEVOPS.md)
- [Endpoints de la API](./Documentation/ENDPOINTS.md)
- [Sistema de reputación y votaciones](./Documentation/REPUTACION.md)
- [Testing](./Documentation/TESTING.md)
- [Guía de contribución](./Documentation/CONTRIBUTING.md)
- [Fallos de seguridad](./Documentation/SECURITY.md)
- [Licencia GPL-3.0](LICENSE)

---

## 🚀 Estado del proyecto

En fase de **base técnica y endurecimiento del entorno**. Funcionalidades completas:

- Autenticación de usuarios (registro, verificación por email, login, refresh token, logout, reset de contraseña).
- Subida de archivos mediante **votaciones** y publicación.
- Descarga de documentos servida desde **S3** (MinIO en dev / R2 en prod).
- Sistema de promoción/depromoción de roles mediante votaciones.
- Votación y **configuración de votaciones** (quórum y umbral por MASTERs).
- Reputación de usuarios.
- Moderación básica (ban/unban) y administración.

Siguiente paso: desarrollo de un **MVP** con hilos de discusión, herramientas avanzadas de moderación, notificaciones y métricas globales.

---

## 🚀 Arranque rápido

El script `run.sh` vive en `gradox2/`. Ejecuta desde esa carpeta:

1. **Entorno de desarrollo completo** (PostgreSQL + MinIO + app con hot-reload): `./run.sh dev`
2. Detener el entorno de desarrollo: `./run.sh dev-down`
3. **Probar**: `./run.sh test`
4. **Ejecutar solo la app** contra una BD local ya levantada y con MinIO accesible: `./run.sh run`
5. **Producción/VPS**: copia la plantilla de entorno y levanta el stack
   ```bash
   cp Docker/.env.example Docker/.env   # rellena Supabase + R2 + mail + JWT + MASTER
   cd gradox2 && ./run.sh docker-up
   ```
   > El entorno de desarrollo necesita **MinIO** (se levanta con `./run.sh dev`).
   > Sin él, la subida/descarga de archivos fallará.

Nota: `JWT_SECRET` debe tener al menos 32 bytes, y `S3_BUCKET_NAME` en producción
debe existir previamente en R2 (en MinIO se auto crea al arrancar).

---

## 🗄️ Persistencia

- **Base de datos**: PostgreSQL gestionado en **Supabase** (Flyway gestiona el schema; `ddl-auto=validate` en prod).
- **Documentos**: almacenados en **S3 compatible** (`S3StorageService`). En BD solo se guarda la `object_key`.
  - Desarrollo: **MinIO** (`http://minio:9000`, usuario `minioadmin`).
  - Producción: **Cloudflare R2**.

---

## 👥 Equipo de desarrollo

- **Asier CL** (@AsierCL)
- **Manuel Pereiro Conde** (@manu-pc)
