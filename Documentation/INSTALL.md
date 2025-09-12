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

- Ve al directorio Docker
```bash
cd Docker
```
- Antes de levantar la app, debes crear un .env en este directorio, con los siguientes datos:
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
- Levanta el contenedor con Docker Compose
```bash
docker-compose --env-file .env --profile full up --build
```
- La aplicación está disponible en:
```bash
http://localhost:8080
```
