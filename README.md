# Gradox 2.0

**Gradox 2.0** es la modernización de la antigua plataforma *Gradox*, un proyecto colaborativo que permite compartir apuntes, ejemplos de ejercicios y exámenes y material de estudio entre estudiantes.

El objetivo es crear una red **colaborativa, segura y democrática**, con un sistema de **votaciones, reputación y gobernanza distribuida** que garantice la calidad del contenido y la transparencia en la gestión, y que permita su mantenimiento en el tiempo.

---

## 📖 Documentación

- [Visión general y propuesta completa](./Documentation/OVERVIEW.md)
- [Arquitectura del sistema](./Documentation/ARCHITECTURE.md)
- [Instalación y despliegue](./Documentation/INSTALL.md)
- [Endpoints de la API](./Documentation/ENDPOINTS.md)
- [Sistema de reputación y votaciones](./Documentation/REPUTACION.md)
- [Guía de contribución](./Documentation/CONTRIBUTING.md) _(pendiente)_
- [Licencia GPL-3.0](LICENSE)

---

## 🚀 Estado del proyecto

Actualmente en fase de **base técnica y endurecimiento del entorno**.
Las funcionalidades completas hasta el momento son:
- Autenticación de usuarios.
- Subida de archivos mediante votaciones.
- Sistema de promoción de roles mediante votaciones.
- Moderación básica.

El siguiente paso es el desarrollo de un MVP con:
- Sistema de hilos para poder subir diferentes soluciones a ejercicios y exámenes, y discutir sobre ellos.
- Herramientas avanzadas de moderación de usuarios.
- Sistema de notificación en la app y/o por correo.
- Métricas y estadísticas globales de la app para favorecer la transparencia.

## 🚀 Arranque rápido

1. Para desarrollo local con contenedor y código montado, usa `./run.sh`
2. Para detener el entorno de desarrollo, usa `./run.sh dev-down`
3. Para ejecución directa contra una base local ya levantada, usa `./run.sh run`
4. Para pruebas, usa `./run.sh test`
5. Para VPS o despliegue con `.env`, copia primero la plantilla: `cp Docker/.env.example Docker/.env`
6. Levanta el stack de VPS con `./run.sh docker-up`

Nota: `JWT_SECRET` debe tener al menos 32 bytes.

---

## 👥 Equipo de desarrollo

- **AsierCL** (@AsierCL)

