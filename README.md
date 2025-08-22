# Propuesta de Rediseño y Modernización de la Plataforma "Gradox 2.0"

## Introducción

La presente propuesta tiene como objetivo definir los principios funcionales, organizativos, técnicos y legales de una nueva versión de la antigua plataforma "Gradox", creada originalmente por un estudiante de la USC hace más de 10 años. Esta plataforma ha servido como repositorio colaborativo de apuntes, exámenes, ejercicios resueltos y materiales de estudio entre estudiantes del Grado en Ingeniería Informática.

"Gradox 2.0" busca modernizar la infraestructura, resolver los cuellos de botella existentes, proteger a los usuarios, fomentar la participación activa y estructurar un sistema de gobernanza distribuido y justo.

---

## 1. Objetivos del Proyecto

- Mejorar la disponibilidad, escalabilidad y facilidad de uso de la plataforma.
- Permitir la participación directa de los estudiantes en la subida, revisión y organización de los materiales.
- Implementar mecanismos democráticos y automáticos para la gestión de contenido.
- Limitar el acceso exclusivamente a miembros verificados de la Universidad de Santiago de Compostela.
- Minimizar los riesgos legales asociados a la publicación de material docente.

---

## 2. Sistema de Roles

### 2.1 Invitados

- Acceso solo de lectura.
- No ven la identidad de los usuarios que suben contenido.
- No pueden votar ni comentar.

### 2.2 Usuarios Registrados

- Registro restringido a correos institucionales `@rai.usc.es`.
- Pueden subir materiales, participar en votaciones y comentar.
- Acumulan reputación en función de su actividad.

### 2.3 Masters

- Usuarios con funciones extendidas: cierre de votaciones, aceptación manual, revisión de contenido.
- Su voto tiene mayor peso (configurable).
- Se eligen por votación.

---

## 3. Mecanismo de Votación

### 3.1 Tipos de Propuestas

- Subida de contenido.
- Eliminación de contenido.
- Promoción de usuario a master.
- Expulsión de master.

### 3.2 Peso del Voto

- Peso del voto de un usuario = Reputacion.
- Peso del voto de un master = coeficiente (configurable) * peso del usuario base.

### 3.3 Umbrales y Reglas

- Votación abierta durante 7 días.
- Mínimo de votantes: 10 usuarios.
- Quórum: al menos 20% de los masters deben votar.
- Aprobación: ≥ 60% de votos ponderados a favor.
- Expulsión de master: solo si el 15% de la comunidad inicia una moción y el 75% de los votos ponderados están a favor.

---

## 4. Sistema de Reputación

- +2 puntos: contenido aprobado.
- +1 punto: respuestas votadas positivamente en hilos.
- +0.5 puntos: votos que coincidan con el resultado final.

El sistema limita la concentración de poder usando la raíz cuadrada del total como peso de voto, evitando oligarquías.

---

## 5. Incentivos y Participación

### 5.1 Gamificación

- Insignias por actividad.
- Rankings semanales.
- Rankings totales.

### 5.2 Notificaciones Inteligentes

- Alertas a usuarios expertos en asignaturas específicas cuando se suben documentos nuevos.

### 5.3 Delegación de Voto

- Delegación temporal o permanente a usuarios de confianza.

---

## 6. Seguridad de Acceso

### 6.1 Login Restringido

- Autenticación vía correo `@rai.usc.es`.
- Sistema de verificación por email con código único.

---

## 7. Protección Legal

### 7.1 Términos de Uso (TOS)

- Los usuarios aceptan que:
    - Son responsables del contenido que suben.
    - No deben subir documentos con derechos de autor sin permiso.
    - El equipo del sitio puede eliminar contenido bajo denuncia.

### 7.2 Política de Denuncias

- Cada documento tiene opción "denunciar contenido".
- Si recibe muchas denuncias, se oculta temporalmente hasta revisión.
- Se registra el historial de denuncias y moderaciones.

### 7.3 Anonimato y Visibilidad

- Para invitados:
    - No se muestra la identidad de quien sube ni comenta.
- Internamente:
    - Registro completo de actividad para trazabilidad.


---

## 8. Gobernanza Dinámica

- Parámetros clave como duración de votaciones, peso de votos, quórums y umbrales pueden ajustarse por votación global de la comunidad.
- Cambios mayores requieren supermayoría (e.g. 70% ponderado de votos).

---

## 9. Transparencia y Métricas

- Dashboard con:
    - Novedades.
    - Tiempos promedio de aceptación.
    - Distribución de votos.
    - Actividad por asignatura.
- Logs visibles para masters.

---

## 10. Proyección y Futuro

- Apertura a otras titulaciones.
- API para subir contenido desde apps móviles.
- Integración con Telegram o Discord para notificaciones.
- Aplicación offline para descarga en local.

---

## Conclusión

Gradox 2.0 es una oportunidad para establecer una red colaborativa, transparente, segura y escalable de intercambio de conocimiento universitario. Esta propuesta combina tecnologías modernas, principios de gobernanza distribuida y prevención legal para asegurar su sostenibilidad y crecimiento.

El siguiente paso es desarrollar un MVP (Producto Mínimo Viable) con el sistema de autenticación, subida de archivos, votación comunitaria y moderación básica, para probar su funcionamiento con un grupo reducido de estudiantes.

---

**Contacto del equipo de desarrollo:** @AsierCL
