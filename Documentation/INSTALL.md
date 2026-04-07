# 🛠️ Instalación de Gradox 2.0

Este documento describe cómo instalar y ejecutar **Gradox 2.0** en un entorno local de desarrollo.

---

## 1. Requisitos Previos

Antes de comenzar, asegúrate de tener instalado lo siguiente:

- **Java 21**
  Se recomienda usar **OpenJDK 21**.
  ```bash
  java -version
  ```
  Debe mostrar algo similar a:
  ```bash
  openjdk version "21.0.1"
  ```

- **Docker & Docker Compose**
  Para levantar la base de datos PostgreSQL.
  ```bash
  docker --version
  docker-compose --version
  ```

- **Git**
  Para clonar el repositorio.
  ```bash
  git --version
  ```

## 2. Clonar el Proyecto

Clona este repositorio de GitHub:
```bash
git clone https://github.com/AsierCL/Gradox2.git
cd Gradox2
```
## 3. Configuración de la Base de Datos

Gradox 2.0 usa PostgreSQL y está preparada para usar Docker.

Si solo quieres desarrollar en local con Docker, no necesitas preparar `.env` todavía: el comando `./run.sh dev` usa valores locales por defecto.

Si quieres levantar el stack orientado a VPS o producción, sigue estos pasos:

- Ve al directorio Docker
```bash
cd Docker
```
- Copia la plantilla de entorno y ajusta los valores reales:
```bash
cp .env.example .env
```
- Antes de levantar la app, revisa que `.env` tenga los datos correctos:
```
# Base de datos
POSTGRES_USER=xxxxxxxx
POSTGRES_PASSWORD=xxxxxxxx
POSTGRES_DB=xxxxxxxx

# Spring DataSource
SPRING_DATASOURCE_URL=jdbc:postgresql://xxxxxxxx
SPRING_DATASOURCE_USERNAME=xxxxxxxx
SPRING_DATASOURCE_PASSWORD=xxxxxxxx

# Mail
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=xxxxxxxx
SPRING_MAIL_PASSWORD=xxxxxxxx

# Spring profile
SPRING_PROFILES_ACTIVE=docker
```

> Nota: `JWT_SECRET` debe tener al menos 32 bytes para HS256.

- Levanta el contenedor con Docker Compose
```bash
docker compose --env-file .env --profile full up --build
```
- La aplicación está disponible en:
```bash
http://localhost:8080
```

## 4. Ejecución local sin Docker

Si prefieres arrancar la aplicación desde el proyecto Maven, usa el script incluido:
```bash
./run.sh run
```

Para ejecutar la suite de pruebas:
```bash
./run.sh test
```

## 5. Ejecución local con Docker

Si quieres desarrollar con la base de datos en contenedor y la app montada desde el código fuente:
```bash
./run.sh
```

Este modo levanta la base de datos y la aplicación en un stack único para desarrollo.

Para producción o VPS, usa el stack de Docker con `.env`:
```bash
./run.sh docker-up
```
