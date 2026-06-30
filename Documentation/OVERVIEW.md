# 📖 Visión General — Gradox 2.0

## Introducción

Esta propuesta define los principios funcionales, organizativos, técnicos y legales de **Gradox 2.0**, una nueva versión de la antigua plataforma *Gradox*, creada originalmente por un estudiante de la USC hace más de 10 años.

La plataforma original ha servido como repositorio colaborativo de **apuntes, exámenes, ejercicios resueltos y materiales de estudio** entre estudiantes del Grado en Ingeniería Informática.

**Gradox 2.0** busca:
- Modernizar la infraestructura.
- Resolver los cuellos de botella existentes.
- Proteger a los usuarios.
- Fomentar la participación activa.
- Establecer un sistema de gobernanza distribuido y justo.

---

## 1. Objetivos del Proyecto

- Mejorar la **disponibilidad, escalabilidad y facilidad de uso** de la plataforma.
- Permitir la **participación directa** de los estudiantes en la subida, revisión y organización de materiales.
- Implementar **mecanismos democráticos y automáticos** para la gestión de contenido.
- Limitar el acceso a **miembros verificados** de la Universidad de Santiago de Compostela.
- Minimizar riesgos **legales** asociados a la publicación de material docente.

---

## 2. Sistema de Roles

### 2.1 Invitados
- Acceso solo de lectura.
- No ven la identidad de los usuarios que suben contenido.
- No pueden votar ni comentar.

### 2.2 Usuarios Registrados
- Registro restringido a correos institucionales `@rai.usc.es`.
- Pueden subir materiales, votar y comentar.
- Acumulan reputación en función de su actividad.

### 2.3 Masters
- Usuarios con posibilidad de funciones extendidas: cierre de votaciones, aceptación manual, revisión de contenido.
- Su voto tiene mayor peso (configurable).
- Se eligen por votación.

---

## 3. Mecanismo de Votación

### 3.1 Tipos de Propuestas
- Subida de contenido.
- Eliminación de contenido.
- Promoción de usuario a master.
- Degradacion de master a usuario.

### 3.2 Peso del Voto
- Peso del voto de un usuario = su **reputación**.
- Peso del voto de un master = coeficiente (configurable) × peso base del usuario.

### 3.3 Umbrales y Reglas
- Duración de votación: **7 días**.
- Mínimo de votantes: **10 usuarios**.
- Quórum: al menos **20% de los masters** deben votar.
- Aprobación: ≥ **60% de votos ponderados** a favor.
- Expulsión de master: requiere moción de **15% de la comunidad** y ≥ **75% de votos ponderados** a favor.

---

## 4. Sistema de Reputación (versión inicial)

- **+2 puntos**: contenido aprobado.
- **+1 punto**: respuestas votadas positivamente en hilos.
- **+0.5 puntos**: votos que coincidan con el resultado final.

> 📌 Nota: La especificación detallada y actualizada del sistema de reputación se encuentra en [REPUTACION.md](REPUTACION.md).

---

## 5. Incentivos y Participación

### 5.1 Gamificación
- Insignias por actividad.
- Rankings semanales.
- Rankings totales.

### 5.2 Notificaciones Inteligentes
- Alertas a usuarios expertos en asignaturas específicas cuando se suben nuevos documentos.

### 5.3 Delegación de Voto
- Delegación temporal o permanente a usuarios de confianza.

---

## 6. Seguridad

### 6.1 Login Restringido
- Autenticación vía correo institucional `@rai.usc.es`.
- Verificación mediante **código único por email**.

### 6.2 Visibilidad del Uploader
- Los archivos tienen un nivel de visibilidad configurable por el uploader mediante `FileVisibility`:
  - **PUBLIC**: todos los usuarios ven el nickname del uploader.
  - **RESTRICTED**: solo USER y MASTER ven el nickname; GUEST e invitados ven "anonymous".
  - **PRIVATE**: solo el propietario y MASTER ven el nickname; cualquier otro rol ve "anonymous".
- Los MASTER y el propietario del archivo siempre ven el nombre real, independientemente del nivel de visibilidad.
- Internamente: se guarda un **registro completo** de actividad para trazabilidad.

### 6.3 Sistema de baneo a usuarios
- Implementación de un sistema que permite restringir el acceso a un usuario con comportamiento perjudicial.

---

## 7. Gobernanza Dinámica

- Parámetros clave (duración de votaciones, pesos, quórums, umbrales) pueden ajustarse mediante **votación global de la comunidad**.
- Cambios mayores requieren **supermayoría** (ej. 70% de votos ponderados).

---

## 8. Transparencia y Métricas

La plataforma incluirá un **dashboard** con:
- Novedades.
- Tiempos promedio de aceptación.
- Distribución de votos.
- Actividad por asignatura.

Los **logs completos** serán visibles para masters.

---

## 9. Proyección y Futuro

- Apertura a otras titulaciones.
- API para subida de contenido desde apps móviles.
- Integración con Telegram o Discord para notificaciones.
- Aplicación offline para descarga en local.

---

## Conclusión

**Gradox 2.0** es una oportunidad para construir una red colaborativa, transparente, segura y escalable de intercambio de conocimiento universitario.

La propuesta combina:
- Tecnologías modernas.
- Principios de gobernanza distribuida.
- Proyecto de código abierto.

---

**Contacto del equipo de desarrollo:**
[@AsierCL](https://github.com/AsierCL)
