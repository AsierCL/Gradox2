# 📊 Sistema de Reputación — Gradox2

El sistema de reputación de **Gradox2** tiene como objetivo incentivar la **participación activa y de calidad** en la plataforma, asegurando que los usuarios colaboren con buen contenido y votaciones responsables.

La reputación determina la fiabilidad de un usuario dentro del ecosistema y otorga mayor peso a sus acciones si alcanza niveles altos.

> **Alcance en el modelo multi-grado:** la reputación es **global por usuario** (no hay reputación por grado),
> pero **quién puede votar qué** sí está acotado: un voto solo cuenta en las propuestas del grado del votante.
> Los pesos de voto (`masterVoteWeight`, `userVoteWeight`) son configuración global. Ver
> [GRADOS.md](GRADOS.md) §6.

---

## 🎯 Objetivos principales

- Fomentar la **participación mínima** (votar, proponer archivos).

- Premiar la **calidad** (aciertos en votaciones, aportes aceptados).

- Desincentivar el **spam o aportes de baja calidad** (penalizaciones claras).

- Recompensar la **responsabilidad y liderazgo** (Masters con mayor influencia).


---

## 📌 Acciones y puntuación

### 1. Votaciones

| Acción                                          | Puntos |
| ----------------------------------------------- | ------ |
| Emitir voto en una propuesta                    | **+1** |
| Voto correcto (coincide con el resultado final) | **+4** |
| Voto incorrecto (no coincide)                   | **−2** |
### 2. Archivos

| Acción                                 | Puntos  |
| -------------------------------------- | ------- |
| Proponer archivo                       | **+2**  |
| Archivo aceptado                       | **+10** |
| Archivo rechazado                      | **−5**  |
| Archivo eliminado después de publicado | **−15** |
### 3. Moderación (solo Masters)

| Acción                                           | Puntos |
| ------------------------------------------------ | ------ |
| Revisión correcta (coincide con resultado final) | **+5** |
| Revisión incorrecta                              | **−3** |
## ⚖️ Multiplicadores de rango

El rol influye en la ganancia de reputación, **solo para recompensas positivas** (las penalizaciones siempre aplican completas):

| Rol    | Multiplicador | Nota                          |
| ------ | ------------- | ----------------------------- |
| User   | x1            | Sin modificadores             |
| Master | x1.5          | Recompensas aumentadas un 50% |

## 🏅 Reglas generales

- La reputación nunca puede ser negativa → valor mínimo **0**.

- Todas las acciones se calculan según la fórmula:

$$Reputación obtenida=(Acción base+Resultado)×Multiplicador$$

- Los usuarios con más reputación obtienen mayor influencia en la comunidad.

- La reputación de un usuario puede usarse para **desbloquear privilegios adicionales** (ej. votar en propuestas clave, acceder a moderación, etc.).


---

## 🔧 Notas de diseño

- Este sistema está diseñado para ser **ajustable y testeado** en base a datos reales de uso.

- Los valores iniciales (bonificaciones y penalizaciones) se eligieron para:

    - Premiar **participación activa**.

    - Penalizar más fuerte los **errores graves** (archivos falsos o spam).

    - Incentivar la **calidad sobre la cantidad**.

- Futuras versiones pueden incluir un sistema **ELO-like** dinámico para balancear aún más la dificultad de subir de rango.
