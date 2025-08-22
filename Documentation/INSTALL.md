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
- Levanta el contenedor con Docker Compose
```bash
docker compose up -d
```
- Verifica que la base de datos está corriendo.
```bash
docker ps
```

## 4. Compilar y Ejecutar la Aplicación

- Entra en el directorio raiz de la aplicación:
```bash
cd ../gradox2
```
- Compila con Maven
```bash
./mvnw clean install
```
- Ejecuta Spring Boot
```bash
./mvnw spring-boot:run
```
- La aplicación está disponible en:
```bash
http://localhost:8080
```
