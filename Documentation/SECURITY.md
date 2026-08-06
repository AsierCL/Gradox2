# Política de Seguridad de Gradox 2

La seguridad es una prioridad absoluta para Gradox 2.0. Nos tomamos muy en serio la protección de los datos, los documentos académicos y la integridad del sistema de votaciones. Agradecemos enormemente a la comunidad que nos ayude a mantener la plataforma segura.

## Versiones Soportadas

Actualmente, solo proporcionamos actualizaciones de seguridad para la última versión principal (rama `main` y las releases más recientes).

## Cómo reportar una vulnerabilidad

**Por favor, 🚨NO reportes vulnerabilidades de seguridad abriendo un Issue público en GitHub🚨.**

Si crees que has encontrado una vulnerabilidad de seguridad en Gradox 2, te rogamos que nos la comuniques de forma privada para que podamos solucionarla antes de que sea de conocimiento público.

Envía un correo electrónico directamente a nuestro equipo de administración:
**[gradox2App@gmail.com]**

### ¿Qué debe incluir tu reporte?
Para ayudarnos a clasificar y solucionar el problema rápidamente, incluye la siguiente información en tu correo:
* **Tipo de vulnerabilidad:** (Ej. Escalada de privilegios, inyección SQL, bypass de JWT, acceso no autorizado a S3).
* **Pasos para reproducir:** Instrucciones claras y detalladas para que podamos replicar el fallo.
* **Impacto:** ¿Qué podría hacer un atacante aprovechando esta vulnerabilidad?
* **Posible solución:** (Opcional) Si tienes idea de cómo solucionarlo o en qué línea de código está el problema, ¡dínoslo!

## Nuestro compromiso

1. **Recepción:** Te confirmaremos la recepción de tu reporte lo antes posible.
2. **Evaluación:** Evaluaremos la vulnerabilidad y te daremos una estimación de tiempo para solucionarla.
3. **Resolución:** Trabajaremos para publicar un parche lo antes posible en la rama `main`.
4. **Reconocimiento:** Tras aplicar el parche y publicar la nueva versión, te reconoceremos públicamente (si lo deseas) en nuestras notas de la versión.

## Alcance del programa

Cualquier vulnerabilidad que afecte la integridad, confidencialidad o disponibilidad del backend de Gradox 2.0 se considera dentro del alcance. Especialmente:
* Manipulación ilícita de los JWT y la autenticación.
* Escalada de privilegios (ej. conseguir acceso de rol `MASTER` sin la votación correspondiente).
* Bypass del sistema de descargas de AWS SDK (S3) para acceder a archivos `PRIVATE` o `RESTRICTED` sin permiso.
* Alteración del peso de los votos o manipulación de las propuestas de gobernanza.
