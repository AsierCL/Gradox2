# Guía de Contribución para Gradox2

¡Gracias por tu interés en contribuir a Gradox2! Para mantener el código limpio y los despliegues estables, sigue estas directrices:

## Flujo de Ramas (Branching)
* La rama `main` está protegida y es reflejo directo de Producción. **Nunca** hagas commits directamente a `main`.
* Crea una rama nueva a partir de `main` para cada tarea usando este formato:
  * `feat/<nombre>` (Nuevas características)
  * `fix/<nombre>` (Correcciones de bugs)
  * `refactor/<nombre>` (Mejoras estructurales sin cambios de lógica)
  * `docs/<nombre>` (Solo cambios de documentación)
  * `test/<nombre>` (Añadir test, refactorizar tests; sin cambios de código en producción)
  * `chore/<nombre>` (Tareas de mantenimiento del proyecto; sin cambios de código en producción ni test)

## Convención de Commits
Usamos [Conventional Commits](https://www.conventionalcommits.org/). El formato debe ser:
`<tipo>: <descripción corta y en minúsculas>`
*Ejemplo:* `feat: añade validación asíncrona para correos`

## 🚀 Proceso de Pull Request (PR)
1. Antes de abrir una PR, asegúrate de haber ejecutado los tests en local (`./run.sh test`) y que todos pasen en verde.
2. Abre la PR contra la rama `main`.
3. Proporciona una descripción clara en la PR indicando qué problema resuelve y cómo lo has testeado.
4. Si la PR soluciona un Issue abierto, incluye la palabra clave (ej. `Closes #5`) en la descripción.
5. Espera a la revisión (Code Review). Todo código debe ser revisado antes del merge.

## 🛠️ Entorno de Desarrollo
Consulta el [Manual de Instalación](INSTALL.md) para levantar el stack con Docker (Supabase + MinIO) antes de codificar.
