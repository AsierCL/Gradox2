# 🎓 Multi-grado — Especificación de backend

> **Estado:** especificación aprobada en sus decisiones de producto, **no implementada**.
> **Alcance:** backend y documentación. El frontend se adapta en otra iteración (§9).
> **Audiencia:** equipo de frontend (contrato y flujos), y quien implemente el backend.
> **Catálogo:** extraído del plan oficial de la USC (Anexo A). Fecha de extracción: 2025-11-16.

---

## 0. Resumen ejecutivo

Gradox 2.0 pasa de ser una plataforma de **un** grado (Ingeniería Informática) a una plataforma
**multi-grado** para la Escuela de Ingeniería: cada estudiante pertenece a **un** grado de forma
permanente, y ese grado define en qué comunidad puede **participar** (subir, votar, proponer,
gobernar). Leer sigue siendo libre para todo el mundo.

En una frase: **el grado es un ámbito de escritura, no un muro de lectura.**

Cambios de alto nivel:

| Área | Cambio |
|---|---|
| Modelo | Nueva entidad `Degree` por encima de `Course`. Jerarquía `Degree → Course → Subject → File`. |
| Datos | 3 grados, 12 cursos, **146 asignaturas** (49 + 47 + 50), con metadatos del plan oficial. |
| Identidad | `users.degree_id` obligatorio e inmutable para el usuario. |
| Autorización | Nuevo guard de ámbito: toda **escritura** exige que el recurso sea del grado del usuario. |
| Gobernanza | `MASTER`, quórum y votaciones pasan a ser **por grado**. La configuración de votación sigue siendo **global**. |
| Seguridad | Nuevo rol de plataforma para `/admin/**`; `MASTER` deja de ser rol de administración del sistema. |
| API | 3 endpoints nuevos (`/degrees`), campos aditivos en DTOs existentes, 1 cambio incompatible en el registro. |
| Frontend | Sin cambios de código en esta iteración; §9 define el contrato y los flujos esperados. |

---

## 1. Decisiones cerradas

Estas son las decisiones de producto tomadas para esta iteración. **El código debe reflejarlas literalmente**;
si alguna cambia, este documento se actualiza antes que el código.

| # | Decisión | Resolución | Consecuencia técnica |
|---|---|---|---|
| **D1** | ¿Dónde se elige el grado? | **En el registro**, no en el login | `POST /api/auth/register` gana `degreeId` obligatorio; el login solo **devuelve** el grado |
| **D2** | ¿Se puede cambiar de grado? | **No**, es permanente para el usuario | Sin endpoint de cambio; override solo-admin y auditado (§4.3) |
| **D3** | ¿Los masters son por grado? | **Sí**, independientes | `MASTER` gobierna únicamente su grado |
| **D4** | ¿Las votaciones son por grado? | **Sí**, independientes | Quórum y papeleta se calculan solo con miembros del grado del recurso |
| **D5** | ¿La configuración de votación? | **Global** | `vote_config` sigue siendo **una sola fila** para toda la plataforma |
| **D6** | Lectura entre grados | **Pública para todo el mundo** (ya lo era) | El ámbito solo bloquea **escrituras**; la visibilidad de archivos no cambia |
| **D7** | Delegación de voto | **Fuera de alcance** (TODO) | `Delegation` queda sin uso; si se activa algún día, debe ser por grado (§12) |
| **D8** | Usuarios existentes | **Todos a Ingeniería Informática** | Backfill en migración, luego `NOT NULL` |
| **D9** | ¿Las asignaturas se comparten entre grados? | **No**, cada una es independiente | Dos "Álgebra" distintas pueden coexistir; el nombre no identifica nada, el `id` sí |
| **D10** | ¿Comentar cuenta como participar? | **Sí** | Comentar exige estar en el grado del archivo: sin sesión `401`, otro grado `403` |
| **D11** | ¿Quién decide la configuración global? | La proponen masters de **cualquier** grado y la votan **todos** los masters | Quórum sobre el total de masters de la plataforma, no sobre un grado |
| **D12** | ¿El `ADMIN` vota? | **No** | Rol operativo: no vota, no propone, no es candidato a promoción |
| **D13** | ¿Se persisten `credits`, `semester` y `type` de la asignatura? | **Sí** | Columnas informadas desde el catálogo oficial (ver §3.5) |

---

## 2. Modelo de dominio

### 2.1 Jerarquía

```plaintext
Degree (grado)          G4012 Ingeniería Informática
  └── Course (año)      1º, 2º, 3º, 4º
        └── Subject     G4012102 Álgebra          ← independiente por grado
              └── File  apuntes, exámenes...
```

* `Degree` es **nuevo**.
* `Course` ya existe, pero hoy su `code` (`1`..`4`) es único **global**; pasa a ser único **por grado**.
* `Subject` ya existe; gana `degree` y su `code` pasa a ser único **por grado**.
* `File`, `TempFile`, `FileProposal`, `ForumThread`, `ThreadComment` **no cambian de esquema**: heredan el
  grado a través de `File.subject.course.degree`.

### 2.2 Independencia de asignaturas (D9)

Esto no es un detalle cosmético, es la razón por la que el catálogo existe:

* "Álgebra" de Informática y "Álgebra" de IA son **filas distintas**, con `id` distinto, archivos distintos,
  votos distintos y foro distinto.
* El **nombre no identifica** una asignatura. El frontend nunca debe asumir que dos asignaturas con el mismo
  nombre son la misma ni mezclar sus listados.
* Consecuencia directa en la API: los DTOs que hoy devuelven solo el nombre de la asignatura
  (`FileResponse.subject`) **deben** añadir identificadores (`subjectId`, `subjectCode`) y el grado
  (§7.2). Hoy `"Álgebra"` es inequívoco; después del cambio, no.

### 2.3 Resolución del grado de un recurso

Regla única de derivación, para que no haya dos formas de calcularlo:

| Recurso | Camino al grado |
|---|---|
| `File` | `file.subject.course.degree` |
| `TempFile` / `FileProposal` | `tempFile.subject.course.degree` |
| `ForumThread` / `ThreadComment` | `thread.file.subject.course.degree` |
| `PromotionProposal` | `proposal.candidate.degree` **y** `proposal.proposer.degree` |
| `ConfigProposal` | *no aplica*: es global (§6.3) |
| `User` | `user.degree` |

No se **denormaliza** `degree_id` en `Subject` ni en `File`: el grado se deriva de `Course`, que es la única
fuente de verdad. Se acepta el `join` extra a cambio de no poder tener datos inconsistentes.

### 2.4 Cambios de esquema

```plaintext
degrees                     (nueva)
  id           PK
  code         UNIQUE          'G4012' | 'G4022' | 'G4121'
  name         NOT NULL        'Ingeniería Informática' | ...

users                       (+ degree_id FK NOT NULL → degrees)
courses                     (+ degree_id FK NOT NULL → degrees; UNIQUE (degree_id, code))
subjects                    (+ degree_id FK NOT NULL → degrees; UNIQUE (degree_id, code)
                             + credits NUMERIC, semester VARCHAR, type VARCHAR)   ← D13
vote_config                 (sin cambios: sigue siendo global, D5)
delegations                 (sin cambios: sin uso, D7)
```

`subjects.credits` admite decimales porque el plan oficial publica asignaturas de `4,5` créditos.
`semester` es nulo en las asignaturas anuales (no tienen semestre).

### 2.5 Un grado, un grado y solo uno

`users.degree_id` es **una** FK, no una tabla de pertenencia. Refleja D2 (una persona, un grado, para siempre)
y evita toda una clase de ambigüedad ("¿en calidad de qué grado estás actuando?").

> **Nota de arquitectura para el futuro:** si algún día se quisiera multi-grado, la forma correcta **no** es
> agregar un `Set<Degree>` a `User`, sino una tabla `user_degrees` con un grado activo explícito y todas las
> reglas de autorización leyendo ese grado activo. Queda escrito acá para que nadie improvise.

---

## 3. Catálogo de grados y asignaturas

### 3.1 Grados

| `code` | `name` | Cursos | Asignaturas |
|---|---|---|---|
| `G4012` | Ingeniería Informática | 4 | 49 |
| `G4022` | Ingeniería Química | 4 | 47 |
| `G4121` | Inteligencia Artificial | 4 | 50 |

Los tres planes son de **240 ECTS**. El catálogo incluye las asignaturas optativas completas (pool), no solo
las obligatorias: el plan publica el pool y el estudiante elige dentro de él. Por eso la suma de ECTS de un
curso con optativas supera 60.

Verificación hecha al extraer los datos: para los tres grados, la sección "plan por curso" y la sección
"plan por módulo/materia" de cada ficha oficial contienen **exactamente el mismo conjunto de códigos**
(49 / 47 / 50). Los cursos 1.º y 2.º de los tres grados suman 60 ECTS exactos.

### 3.2 Ingeniería Informática (G4012)

#### Primer curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4012101` | Fundamentos de Matemáticas | Primer semestre | Formación básica | 6 |
| `G4012102` | Álgebra | Primer semestre | Formación básica | 6 |
| `G4012103` | Matemática Discreta | Primer semestre | Formación básica | 6 |
| `G4012104` | Sistemas Digitales | Primer semestre | Formación básica | 6 |
| `G4012105` | Programación I | Primer semestre | Formación básica | 6 |
| `G4012106` | Fundamentos Tecnológicos y Físicos de la Informática | Segundo semestre | Formación básica | 6 |
| `G4012107` | Estadística | Segundo semestre | Formación básica | 6 |
| `G4012108` | Fundamentos de Computadores | Segundo semestre | Formación básica | 6 |
| `G4012109` | Programación II | Segundo semestre | Formación básica | 6 |
| `G4012121` | Cálculo y Análisis Numérico | Segundo semestre | Obligatorio | 6 |

#### Segundo curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4012201` | Gestión Financiera de Empresas | Segundo semestre | Formación básica | 6 |
| `G4012221` | Bases de Datos I | Primer semestre | Obligatorio | 6 |
| `G4012222` | Algoritmos y Estructuras de Datos | Primer semestre | Obligatorio | 6 |
| `G4012223` | Sistemas Operativos I | Primer semestre | Obligatorio | 6 |
| `G4012224` | Redes | Primer semestre | Obligatorio | 6 |
| `G4012225` | Programación Orientada a Objetos | Primer semestre | Obligatorio | 6 |
| `G4012226` | Bases de Datos II | Segundo semestre | Obligatorio | 4,5 |
| `G4012227` | Sistemas Operativos II | Segundo semestre | Obligatorio | 4,5 |
| `G4012228` | Arquitectura de Computadores | Segundo semestre | Obligatorio | 4,5 |
| `G4012229` | Computación Gráfica | Segundo semestre | Obligatorio | 4,5 |
| `G4012230` | Diseño de Software | Segundo semestre | Obligatorio | 6 |

#### Tercer curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4012321` | Teoría de Autómatas y Lenguajes Formales | Primer semestre | Obligatorio | 6 |
| `G4012322` | Administración de Sistemas y Redes | Primer semestre | Obligatorio | 6 |
| `G4012323` | Desarrollo de Aplicaciones Web | Segundo semestre | Obligatorio | 6 |
| `G4012324` | Gestión de Proyectos Informáticos | Segundo semestre | Obligatorio | 4,5 |
| `G4012325` | Ingeniería del Software | — | Anual | 12 |
| `G4012326` | Computación Distribuida | Primer semestre | Obligatorio | 6 |
| `G4012327` | Compiladores e Intérpretes | Segundo semestre | Obligatorio | 4,5 |
| `G4012328` | Inteligencia Artificial | Primer semestre | Obligatorio | 6 |
| `G4012329` | Seguridad de la Información | Segundo semestre | Obligatorio | 4,5 |
| `G4012330` | Ciberseguridad | Segundo semestre | Obligatorio | 4,5 |

#### Cuarto curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4012421` | Interacción Persona-Ordenador | Primer semestre | Obligatorio | 6 |
| `G4012422` | Ingeniería de Computadores | Primer semestre | Obligatorio | 6 |
| `G4012423` | Trabajo Fin de Grado | — | Trabajos Fin de Grado y Máster | 12 |
| `G4012424` | Prácticas Externas | — | Prácticas en Empresas de Grao e Máster | 9 |
| `G4012441` | Fundamentos de Sistemas Paralelos | Primer semestre | Optativo | 4,5 |
| `G4012442` | Visualización Avanzada | Primer semestre | Optativo | 4,5 |
| `G4012443` | Calidad de los Sistemas de Información | Primer semestre | Optativo | 4,5 |
| `G4012444` | Almacenes y Minería de Datos | Primer semestre | Optativo | 4,5 |
| `G4012445` | Conocimiento y Razonamiento Automático | Primer semestre | Optativo | 4,5 |
| `G4012446` | Sistemas Inteligentes | Primer semestre | Optativo | 4,5 |
| `G4012447` | Diseño y Administración de Redes | Primer semestre | Optativo | 4,5 |
| `G4012448` | Ingeniería de Servicios | Primer semestre | Optativo | 4,5 |
| `G4012449` | Computación en la Nube | Primer semestre | Optativo | 4,5 |
| `G4012451` | Computación Ubicua | Segundo semestre | Optativo | 4,5 |
| `G4012452` | Programación de Arquitecturas Emergentes | Segundo semestre | Optativo | 4,5 |
| `G4012453` | Gestión de Información no estructurada | Segundo semestre | Optativo | 4,5 |
| `G4012454` | Modelos y Técnicas de Optimización | Segundo semestre | Optativo | 4,5 |
| `G4012455` | Aprendizaje Automático | Segundo semestre | Optativo | 4,5 |

### 3.3 Ingeniería Química (G4022)

#### Primer curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4022101` | Física | Primer semestre | Formación básica | 6 |
| `G4022102` | Matemáticas | Primer semestre | Formación básica | 6 |
| `G4022103` | Informática | Primer semestre | Formación básica | 6 |
| `G4022104` | Fundamentos químicos en ingeniería | Primer semestre | Formación básica | 6 |
| `G4022105` | Ecuaciones diferenciales | Segundo semestre | Formación básica | 6 |
| `G4022106` | Estadística y ciencia de datos | Segundo semestre | Formación básica | 6 |
| `G4022107` | Química inorgánica | Segundo semestre | Formación básica | 6 |
| `G4022121` | Electrotecnia | Segundo semestre | Obligatorio | 6 |
| `G4022122` | Fundamentos de procesos químicos I | Primer semestre | Obligatorio | 6 |
| `G4022123` | Fundamentos de procesos químicos II | Segundo semestre | Obligatorio | 6 |

#### Segundo curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4022201` | Química orgánica | Primer semestre | Formación básica | 6 |
| `G4022202` | Organización y economía de empresa | Primer semestre | Formación básica | 6 |
| `G4022203` | Expresión gráfica | Segundo semestre | Formación básica | 6 |
| `G4022221` | Mecánica aplicada | Primer semestre | Obligatorio | 6 |
| `G4022222` | Termodinámica aplicada a la ingeniería química | Primer semestre | Obligatorio | 6 |
| `G4022223` | Transporte de fluidos | Primer semestre | Obligatorio | 6 |
| `G4022224` | Transmisión de calor | Segundo semestre | Obligatorio | 6 |
| `G4022225` | Ingeniería de la reacción química | Segundo semestre | Obligatorio | 6 |
| `G4022226` | Fundamentos de energética industrial | Segundo semestre | Obligatorio | 6 |
| `G4022227` | Laboratorio de transporte de fluidos y transmisión de calor | Segundo semestre | Obligatorio | 6 |

#### Tercer curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4022321` | Ciencia de materiales | Primer semestre | Obligatorio | 4,5 |
| `G4022322` | Resistencia de materiales | Primer semestre | Obligatorio | 6 |
| `G4022323` | Ingeniería ambiental | Segundo semestre | Obligatorio | 4,5 |
| `G4022324` | Operaciones de separación | Segundo semestre | Obligatorio | 4,5 |
| `G4022325` | Operaciones de transferencia de materia | Primer semestre | Obligatorio | 6 |
| `G4022326` | Reactores químicos | Primer semestre | Obligatorio | 4,5 |
| `G4022327` | Ingeniería bioquímica | Primer semestre | Obligatorio | 4,5 |
| `G4022328` | Control de procesos | Segundo semestre | Obligatorio | 6 |
| `G4022329` | Laboratorio de procesos químicos | Segundo semestre | Obligatorio | 6 |
| `G4022330` | Ingeniería de procesos | Segundo semestre | Obligatorio | 4,5 |
| `G4022341` | Métodos numéricos | Primer semestre | Optativo | 4,5 |
| `G4022342` | Operaciones con sólidos | Primer semestre | Optativo | 4,5 |
| `G4022343` | Química analítica instrumental | Primer semestre | Optativo | 4,5 |
| `G4022344` | Tecnologías de la valorización | Segundo semestre | Optativo | 4,5 |
| `G4022345` | Procesos de química industrial | Segundo semestre | Optativo | 4,5 |
| `G4022346` | Gestión de la calidad | Segundo semestre | Optativo | 4,5 |

#### Cuarto curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4022421` | Seguridad y prevención de riesgos | — | Obligatorio | 4,5 |
| `G4022422` | Desarrollo de proyectos | — | Obligatorio | 6 |
| `G4022423` | Automática industrial | — | Obligatorio | 4,5 |
| `G4022424` | Ingeniería de los sistemas de producción | — | Obligatorio | 6 |
| `G4022425` | Simulación y optimización | — | Obligatorio | 4,5 |
| `G4022426` | Aula profesional | — | Obligatorio | 6 |
| `G4022427` | Prácticas en empresas | — | Prácticas en Empresas de Grao e Máster | 6 |
| `G4022428` | Trabajo fin de grado | — | Obligatorio | 18 |
| `G4022441` | Inteligencia artificial aplicada a la ingeniería química | — | Optativo | 4,5 |
| `G4022442` | Ingeniería química y sostenibilidad | — | Optativo | 4,5 |
| `G4022443` | Operaciones unitarias de la industria alimentaria | — | Optativo | 4,5 |

### 3.4 Inteligencia Artificial (G4121)

#### Primer curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4121101` | Álgebra | Primer semestre | Formación básica | 6 |
| `G4121102` | Cálculo y análisis numérico | Primer semestre | Formación básica | 6 |
| `G4121103` | Matemática discreta | Primer semestre | Formación básica | 6 |
| `G4121104` | Programación I | Primer semestre | Formación básica | 6 |
| `G4121105` | Introducción a los computadores | Primer semestre | Formación básica | 6 |
| `G4121106` | Estadística | Segundo semestre | Formación básica | 6 |
| `G4121107` | Programación II | Segundo semestre | Formación básica | 6 |
| `G4121108` | Adquisición y procesamiento de señal | Segundo semestre | Formación básica | 6 |
| `G4121109` | Lógica | Segundo semestre | Formación básica | 6 |
| `G4121110` | Gestión de organizaciones | Segundo semestre | Formación básica | 6 |

#### Segundo curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4121221` | Optimización matemática | Primer semestre | Obligatorio | 6 |
| `G4121222` | Algoritmos | Primer semestre | Obligatorio | 6 |
| `G4121223` | Ingeniería de software | Primer semestre | Obligatorio | 6 |
| `G4121224` | Bases de datos | Primer semestre | Obligatorio | 6 |
| `G4121225` | Redes | Primer semestre | Obligatorio | 6 |
| `G4121226` | Computación concurrente, paralela y distribuida | Segundo semestre | Obligatorio | 6 |
| `G4121227` | Autómatas y lenguajes formales | Segundo semestre | Obligatorio | 6 |
| `G4121228` | Fundamentos de aprendizaje automático | Segundo semestre | Obligatorio | 6 |
| `G4121229` | Algoritmos básicos de la inteligencia artificial | Segundo semestre | Obligatorio | 6 |
| `G4121230` | Representación del conocimiento y razonamiento | Segundo semestre | Obligatorio | 6 |

#### Tercer curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4121341` | Psicología cognitiva | Segundo semestre | Optativo | 4,5 |
| `G4121342` | Neurofisiología | Primer semestre | Optativo | 3 |
| `G4121343` | Neurociencia cognitiva y afectiva | Segundo semestre | Optativo | 4,5 |
| `G4121344` | Metaheurísticas | Primer semestre | Optativo | 6 |
| `G4121345` | Razonamiento con incertidumbre | Segundo semestre | Optativo | 6 |
| `G4121346` | Ingeniería de datos a gran escala | Primer semestre | Optativo | 4,5 |
| `G4121347` | Técnicas de procesamiento masivo de datos | Primer semestre | Optativo | 4,5 |
| `G4121348` | Plataformas de internet de las cosas | Primer semestre | Optativo | 4,5 |
| `G4121349` | Aprendizaje automático supervisado | Primer semestre | Optativo | 6 |
| `G4121350` | Aprendizaje automático no supervisado | Segundo semestre | Optativo | 4,5 |
| `G4121351` | Redes neuronales y aprendizaje profundo | Segundo semestre | Optativo | 6 |
| `G4121352` | Proyecto integrador de IA I | Segundo semestre | Optativo | 6 |

#### Cuarto curso

| Código | Asignatura | Semestre | Tipo | Créditos |
|---|---|---|---|---|
| `G4121421` | Trabajo fin de grado | — | Trabajos Fin de Grado y Máster | 12 |
| `G4121441` | Dimensión jurídica de la IA | Primer semestre | Optativo | 3 |
| `G4121442` | Aspectos tecnocientíficos de la IA | Primer semestre | Optativo | 3 |
| `G4121443` | Aprendizaje por refuerzo | Primer semestre | Optativo | 6 |
| `G4121444` | Visión por computador | Primer semestre | Optativo | 6 |
| `G4121445` | Tecnologías del lenguaje | Primer semestre | Optativo | 6 |
| `G4121446` | Proyecto integrador de IA II | Primer semestre | Optativo | 6 |
| `G4121447` | Evaluación de proyectos empresariales | Segundo semestre | Optativo | 6 |
| `G4121448` | Prácticas externas I | — | Prácticas en Empresas de Grao e Máster | 6 |
| `G4121449` | Prácticas externas II | — | Prácticas en Empresas de Grao e Máster | 6 |
| `G4121450` | Algoritmos verdes para inteligencia artificial | — | Optativo | 3 |
| `G4121451` | Computación afectiva | — | Optativo | 3 |
| `G4121452` | Computación de altas prestaciones para IA | — | Optativo | 3 |
| `G4121453` | Inteligencia artificial en salud | Segundo semestre | Optativo | 3 |
| `G4121454` | Minería de procesos | — | Optativo | 3 |
| `G4121455` | Visión por computador II | Segundo semestre | Optativo | 3 |
| `G4121456` | Web semántica y grafos de conocimiento | Segundo semestre | Optativo | 3 |
| `G4121457` | Introducción al machine learning cuántico y a la optimización cuántica | Segundo semestre | Optativo | 3 |

### 3.5 Notas sobre los datos del catálogo

* **Nombres literales.** Los nombres están tal cual figuran en el plan oficial, incluida la capitalización
  irregular (`Fundamentos químicos en ingeniería`, `Química orgánica`). No se normalizan: son datos oficiales.
* **Códigos.** El código oficial de la USC (`G4012101`) es único en toda la plataforma y **ya contiene el
  grado** en sus cuatro primeros caracteres. Se adopta como `subjects.code`, así que el catálogo de los tres
  grados no puede colisionar aunque el nombre se repita.
* **Campos del plan que se persisten desde ahora (D13).** `credits` (decimal), `semester` y `type` se
  guardan en `subjects` y se exponen en `SubjectResponse`: los necesita el frontend para la ficha de la
  asignatura y es barato informarlos en la misma migración que crea el catálogo.
* **`Anual`.** Algunas asignaturas son anuales (12 créditos) y no tienen semestre. El frontend debe tolerar
  `semester` vacío.

---

## 4. Autenticación y perfil

### 4.1 Registro

`POST /api/auth/register` incorpora **un campo obligatorio nuevo**:

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `email` | string | sí | Sin cambios: dominio `@rai.usc.es` / `@rai.usc.gal` |
| `username` | string | sí | Sin cambios |
| `password` | string | sí | Sin cambios |
| **`degreeId`** | **number** | **sí** | `id` de `GET /degrees`. Si falta → `400 DEGREE_REQUIRED`; si no existe → `400 DEGREE_NOT_FOUND` |

El grado se persiste en el momento del alta. **No hay** endpoint de cambio de grado para el usuario (D2).

### 4.2 Login

`POST /api/auth/login` **no cambia su request**. El grado es un dato de salida, no una elección por sesión:

```jsonc
// POST /api/auth/login  →  200
{
  "token": "eyJ...",
  "refreshToken": "eyJ...",
  "username": "aliceuser",
  "role": "USER",              // USER | MASTER | ADMIN
  "isPlatformAdmin": false,    // NUEVO (aditivo)
  "degreeId": 1,               // NUEVO (aditivo)
  "degreeCode": "G4012",       // NUEVO (aditivo)
  "degreeName": "Ingeniería Informática"  // NUEVO (aditivo)
}
```

`GET /users/me` (`MyProfileResponse`) recibe los mismos tres campos de grado. Mismo criterio aditivo.

### 4.3 Inmutabilidad y override administrativo

* Ningún endpoint de usuario permite cambiar `degreeId`. Tampoco el perfil (`PUT /users/me`).
* La única vía de corrección es **administrativa**: `PATCH /admin/users/{id}/degree`, requiere rol de
  plataforma (§6.4), deja un `AuditRecord` con valor anterior, valor nuevo, actor y motivo, e incrementa
  `tokenVersion` del usuario afectado para invalidar tokens vigentes (la columna ya existe).
* El motivo es obligatorio: un cambio de grado altera permisos de forma retroactiva, tiene que quedar
  explicado en la auditoría.

### 4.4 Contenido del JWT

* El token sigue llevando la identidad (`sub`, rol) y **no** se convierte en la fuente de verdad del grado.
* El grado se resuelve **siempre** desde la base de datos en cada request autenticado. Un `degreeId` enviado
  por el cliente en un body o query **nunca** autoriza nada: solo sirve para filtrar lecturas.
* Los campos `degree*` y `isPlatformAdmin` del response existen para que el frontend pinte la UI; no son
  autoridad.

### 4.5 Usuario sin grado (defensivo)

Después de la migración (D8) no puede existir un usuario sin grado. Aun así, si `degree_id` fuese `null`:

* las **lecturas** funcionan normalmente;
* cualquier **escritura** de ámbito respondida con `409 DEGREE_REQUIRED`, nunca con un `500` ni con un
  permiso concedido por defecto.

---

## 5. Permisos

### 5.1 Matriz de capacidades

"Otro grado" = el recurso pertenece a un grado distinto al del usuario autenticado.

| Capacidad | Mismo grado | Otro grado | Sin sesión |
|---|---|---|---|
| Ver grados, cursos y asignaturas | ✅ | ✅ | ✅ |
| Listar archivos | ✅ | ✅ | ✅ |
| Ver detalle y descargar archivo | ✅ | ✅ | ✅ (con visibilidad aplicada) |
| Puntuar archivo (`POST /files/{id}/vote/{upvote}`) | ✅ | ❌ `403` | ❌ `401` |
| Proponer subida (`POST /file-proposals`) | ✅ | ❌ `403` | ❌ `401` |
| Proponer borrado (`DELETE /files/{id}`) | ✅ | ❌ `403` | ❌ `401` |
| Votar propuesta de archivo | ✅ | ❌ `403` | ❌ `401` |
| Votar propuesta de promoción/expulsión | ✅ | ❌ `403` | ❌ `401` |
| Proponerse a MASTER | ✅ | — (solo aplica a tu grado) | ❌ `401` |
| Proponer expulsión de un MASTER | ✅ (mismo grado) | ❌ `403` | ❌ `401` |
| Proponer cambio de configuración global | ✅ solo `MASTER` | ❌ `403` | ❌ `401` |
| Comentario en hilo de archivo | ✅ | ❌ `403` | ❌ `401` |
| Ver estadísticas | ✅ | ✅ | ✅ |
| `/admin/**` | solo rol de plataforma | solo rol de plataforma | ❌ `401` |

### 5.2 Regla de oro

> **Toda escritura declara su ámbito.** Si una operación de escritura no puede responder "¿de qué grado es
> este recurso?", no está lista para implementarse.

Casos que la regla resuelve sin ambigüedad:

* Un usuario solo vota propuestas cuyo grado coincide con el suyo.
* Un `MASTER` solo gobierna dentro de su grado: no puede expulsar a un master de otro grado, ni votar una
  propuesta de otro grado.
* El autor de una propuesta de borrado no puede "colarla" en otro grado cambiando un id en el body: el grado
  se deriva del **archivo**, no del payload.

### 5.3 Punto único de enforcement

**No** se reparte la comparación de grados por los servicios. Se implementa **un** componente de política,
por ejemplo `DegreeAccessGuard` / `ICommunityScope`, con dos operaciones:

```java
// lecturas: siempre permitidas, no hace falta llamarlo
void requireSameDegree(Degree resourceDegree);            // 403 DEGREE_SCOPE_FORBIDDEN
void requireWriteAccess(User actor, Degree resourceDegree);
```

Reglas de implementación:

1. El servicio que muta **resuelve el grado del recurso desde el recurso ya cargado** y llama al guard
   antes de tocar nada (antes de crear `TempFile`, antes de guardar `Vote`, antes de persistir la propuesta).
2. El guard toma el usuario de `GetAuthUser` —ya existe— y **nunca** de un parámetro del cliente.
3. Falla con `403` y el envelope de error que ya usan `RestAccessDeniedHandler`/`GlobalExceptionHandler`,
   con código `DEGREE_SCOPE_FORBIDDEN` (§8).
4. Los servicios que ya son `@PreAuthorize("hasRole('MASTER')")` mantienen el chequeo de rol **y** suman el
   de ámbito: rol y ámbito son ortogonales.

Motivo: con 7–8 puntos de escritura es cuestión de tiempo que alguien agregue el noveno y se olvide del
control. Un punto único auditable es la diferencia entre "hay un agujero" y "hay un lugar donde mirar".

### 5.4 Lectura y visibilidad (D6)

* La lectura cross-grado **no se bloquea**. Responde `200` igual que en el grado propio.
* `FileVisibility` (`PUBLIC`, `RESTRICTED`, `PRIVATE`) sigue aplicando **exactamente igual que hoy**:
  gobierna qué detalle del archivo y qué identidad del uploader se ven. No tiene relación con el grado.
* Lo que cambia para el visitante de otro grado no es lo que **puede ver**, sino lo que **puede hacer**: el
  frontend oculta las acciones de escritura y, si el usuario insiste, el backend responde `403`.

---

## 6. Gobernanza por grado

### 6.1 `MASTER` deja de ser rol de sistema

Hoy `MASTER` significa dos cosas a la vez: "gobierna la comunidad" y "administra la plataforma"
(todo `/admin/**`, `/admin/logs`, `/admin/config`, `AdminProposalController`, `VoteConfigController`).
Con masters por grado (D3) eso sería un agujero: un master de Química heredaría el panel de toda la
plataforma.

Se separan los conceptos:

| Concepto | Enum | Alcance | Qué puede |
|---|---|---|---|
| Usuario | `USER` | su grado | Leer todo, escribir en su grado |
| Master de grado | `MASTER` | **su grado** | Gobernanza de su grado: promover, expulsar, votar, proponer config |
| Administrador de plataforma | `ADMIN` (nuevo) | **toda la plataforma** | `/admin/**`, gestión de grados, override de grado, bootstrap de masters |

* `/admin/**` pasa de `hasRole('MASTER')` a `hasRole('ADMIN')`.
* Un `MASTER` **no** ve el panel de administración.
* Los tres roles son excluyentes entre sí (`UserRole` sigue siendo un enum de valor único).
* `GET /roles/*` (propuestas de promoción/expulsión) pasa a ser gobernanza **de grado**.

### 6.2 Votaciones (D4)

| Elemento | Antes | Después |
|---|---|---|
| Quórum de la propuesta | Config global `quorumRequired` (nº de votos) | **Mismo valor**, pero cuentan solo votos de miembros del grado del recurso |
| Umbral de aprobación | Config global `approvalThreshold` | Sin cambios (ratio ponderado) |
| Pesos de voto | Config global `masterVoteWeight` / `userVoteWeight` | Sin cambios |
| Elegibilidad del votante | Cualquier usuario activo | Usuario activo **y** del grado del recurso |
| Conteo de masters | **Global** (`countByRoleAndEnabled(MASTER, true)`) | **Por grado** |
| Guard "último master" | Global | **Por grado**: no se puede dejar a un grado sin masters |

Punto crítico: `VoteServiceImpl` cuenta masters globales para el 80 % de las propuestas de configuración y
`RoleServiceImpl` usa el mismo conteo para "no puedes expulsar al último MASTER activo". Ambas consultas deben
volverse por grado; si solo se filtra el voto pero no el conteo, el sistema cierra propuestas con números de
otra comunidad.

### 6.3 Configuración global (D5)

`vote_config` sigue siendo **una fila única** para toda la plataforma: quórum, umbral, máximos de subida
pendiente y pesos de voto son los mismos para los tres grados.

Consecuencia directa de D11: una propuesta de configuración tiene efecto **global**, así que no puede
decidirla una sola comunidad. Comportamiento esperado:

* puede proponerla cualquier `MASTER` de cualquier grado;
* la votan **todos** los masters de **todos** los grados;
* el quórum se cuenta sobre el total de masters de la plataforma y el umbral sigue siendo el 80 % del total;
* el `ADMIN` **no** vota (D12).

### 6.4 Bootstrap de un grado nuevo

Un grado con cero masters está muerto: no puede aprobar promociones ni config. Mecanismo (supuesto **A1**,
§12: es lo único de esta lista que no estaba cerrado):

1. La migración siembra los tres grados con su catálogo, **sin** masters.
2. `BOOTSTRAP_MASTER_*` pasa a crear un **`ADMIN` de plataforma**. Sin esto, tras el despliegue nadie podría
   administrar nada: `/admin/**` deja de aceptar `MASTER`.
3. El primer `MASTER` de cada grado se nombra **por acceso directo a base de datos** (decisión del equipo:
   no hay endpoint para esto). A partir de ahí la gobernanza es endógena: el resto de masters salen de
   votaciones del propio grado.
4. Promoción operativa, documentada acá para no improvisarla en producción:

   ```sql
   -- dar acceso al panel de administración a un usuario existente
   UPDATE users SET role = 'ADMIN' WHERE username = '<usuario>';
   -- nombrar el primer master de un grado (requiere que el usuario ya tenga ese grado)
   UPDATE users SET role = 'MASTER'
    WHERE username = '<usuario>' AND degree_id = (SELECT id FROM degrees WHERE code = 'G4012');
   ```
5. Nunca por auto-promoción: el guard "último master" no debe tener una excepción que permita a un `USER`
   saltárselo.

### 6.5 Reputación e insignias

Sin cambios: son globales por usuario. Lo único que cambia es **quién vota** sobre qué, no cuánto pesa.

---

## 7. API

### 7.1 Endpoints nuevos (todos públicos, `permitAll`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/degrees` | Lista de grados: `id`, `code`, `name`, `coursesCount` |
| `GET` | `/degrees/{id}` | Detalle de un grado |
| `GET` | `/degrees/{id}/courses` | Cursos del grado, cada uno con sus asignaturas (mismo shape que `CourseResponse` + `degree*`) |

`GET /degrees` es el que consume la pantalla de registro: el selector de grado se llena de acá, nunca con una
lista hardcodeada en el frontend.

### 7.2 Endpoints existentes (cambios **aditivos**)

| Endpoint | Qué se agrega | Rompe FE? |
|---|---|---|
| `GET /degrees/*` | nuevos | no |
| `GET /courses` | cada curso gana `degreeId`, `degreeCode`, `degreeName`; filtro opcional `?degreeId=` | no |
| `GET /courses/{id}` | idem | no |
| `GET /subjects` | cada asignatura gana `degreeId`, `degreeCode`, `degreeName`, `credits`, `semester`, `type`; filtro opcional `?degreeId=` | no |
| `GET /subjects/{id}` | idem | no |
| `GET /files/all` | cada archivo gana `subjectId`, `subjectCode`, `degreeId`, `degreeCode`, `degreeName` | no |
| `GET /files/{id}` | idem | no |
| `POST /api/auth/register` | campo `degreeId` **obligatorio** | **SÍ** (§7.4) |
| `POST /api/auth/login` | `degree*` + `isPlatformAdmin` en el response | no |
| `GET /users/me` | `degree*` + `isPlatformAdmin` | no |
| `GET /roles/*` | propuestas expuestas con `candidateDegree*` | no |
| `PUT /files/{id}/visibility` | sin cambios de contrato (sí de permiso: solo dentro del grado) | no |
| `PATCH /admin/users/{id}/degree` | nuevo, solo `ADMIN`, auditado | no |

**Por qué se agregan `subjectId`/`subjectCode` a `FileResponse`:** con D9 el nombre de la asignatura deja de
ser único. Un frontend que agrupe por `subject` va a mezclar el Álgebra de Informática con el de IA. Los
identificadores son obligatorios.

### 7.3 Endpoints sin cambios

`/files/{id}/download`, `/files/{id}/comments`, `GET /badges`, `GET /stats`, `/health`, flujo de reset de
contraseña, `logout`, `token/refresh`. Siguen funcionando igual; los tres primeros heredan el filtro de
ámbito solo en sus operaciones de escritura.

### 7.4 Breaking changes para el frontend

| # | Qué rompe | Impacto | Acción de FE |
|---|---|---|---|
| B1 | `POST /api/auth/register` exige `degreeId` | La pantalla de registro actual no puede crear usuarios | Agregar selector de grado obligatorio (§9.1) |
| B2 | `role == "MASTER"` ya no implica acceso a `/admin/**` | Cualquier UI que muestre el panel por rol queda mal | Usar `isPlatformAdmin` para eso (§9.3) |
| B3 | `FileResponse.subject` deja de ser único global | Agrupaciones por nombre se mezclan entre grados | Usar `subjectId`/`subjectCode` (§9.4) |
| B4 | Escrituras cross-grado devuelven `403 DEGREE_SCOPE_FORBIDDEN` | Antes todas respondían `200` | Manejar el código de error (§8, §9.5) |

`OpenApiContractTest` fija la lista de operaciones públicas: **va a fallar a propósito** al agregar
`/degrees`. Es el recordatorio de que el contrato cambió y hay que regenerar el JSON y avisar a FE.

---

## 8. Catálogo de errores nuevos

| Código | HTTP | Cuándo | Mensaje sugerido |
|---|---|---|---|
| `DEGREE_REQUIRED` | `400` | Registro sin `degreeId`, o escritura de un usuario sin grado | `Debes seleccionar un grado` |
| `DEGREE_NOT_FOUND` | `400` | `degreeId` inexistente en el registro | `El grado seleccionado no existe` |
| `DEGREE_IMMUTABLE` | `403` | Intento de cambiar el grado por vía no administrativa | `El grado de un usuario no se puede modificar` |
| `DEGREE_SCOPE_FORBIDDEN` | `403` | Escritura sobre un recurso de otro grado | `Solo puedes participar en tu grado` |
| `DEGREE_LAST_MASTER` | `409` | Expulsión del último master activo de un grado | `No puedes dejar el grado sin masters` |

Formato: el mismo envelope de error que ya usa la API (`ErrorDTO`). El frontend debe discriminar por
**código**, no por texto.

---

## 9. Flujos esperados de frontend (sin implementar)

Esta sección es el contrato de comportamiento. **No** se escribe código de frontend en esta iteración.

### 9.1 Registro

1. Pantalla de registro pide email, usuario, contraseña y **grado** (obligatorio).
2. El selector de grado se llena con `GET /degrees`; muestra `name` y usa `id`.
3. Aviso explícito en la UI: *"El grado no se puede cambiar después. Elegilo con cuidado."* — es una decisión
   permanente (D2).
4. Error `400 DEGREE_REQUIRED` → resaltar el selector.

### 9.2 Pantalla por defecto después del login

Pedido del equipo: al loguearse, la vista predeterminada es **la pantalla del grado del usuario**.

1. Tras login exitoso, el response ya trae `degreeId`, `degreeCode`, `degreeName`.
2. Redirigir a la pantalla del grado: `/grados/{degreeCode}` o equivalente, mostrando los cursos y asignaturas
   de ese grado (`GET /degrees/{degreeId}/courses`).
3. No hay selector de grado post-login: el grado activo es siempre el del perfil.
4. Si el usuario entra a mano a otro grado, se muestra en modo lectura (§9.5).

### 9.3 Rol y administración

* El panel de administración se muestra con `isPlatformAdmin === true`, **nunca** con `role === "MASTER"`.
* `role === "MASTER"` habilita **solo** las acciones de gobernanza del grado propio: proponer expulsión,
  votar propuestas, proponer configuración.
* Un `MASTER` no debe ver enlaces a `/admin/**`.

### 9.4 Listados y agrupaciones

* Nunca agrupar archivos por nombre de asignatura: usar `subjectId`.
* Mostrar el grado en el detalle del archivo (`degreeName`) para que el visitante sepa de dónde viene el
  material; los nombres de asignatura se repiten entre grados.

### 9.5 Visitante de otro grado (modo lectura)

* Navegación normal: puede ver catálogo, archivos, detalle y descargar.
* Ocultar o deshabilitar toda acción de escritura (votar, subir, proponer, comentar).
* Si una acción llega igual al backend y vuelve `403 DEGREE_SCOPE_FORBIDDEN`, mostrar un aviso neutro:
  *"Solo los estudiantes de este grado pueden participar"*, sin tratarlo como error de sesión.
* Aplicar la visibilidad existente: `RESTRICTED`/`PRIVATE` ocultan detalle y uploader según la regla actual.

### 9.6 Cuenta sin grado (defensivo)

Si en algún momento el perfil viene sin grado, mostrar onboarding de selección una única vez; cualquier
escritura devuelve `409 DEGREE_REQUIRED` hasta resolverlo.

---

## 10. Migración de datos

### 10.1 Estrategia

| Paso | Acción | Riesgo |
|---|---|---|
| 1 | Crear `degrees` e insertar los 3 grados | bajo |
| 2 | `users.degree_id` nullable → backfill **todos** a Ingeniería Informática → `NOT NULL` | **alto** (dato irreversible) |
| 3 | `courses.degree_id` + `UNIQUE (degree_id, code)`; asignar los 4 cursos actuales a Informática | medio |
| 4 | `subjects.degree_id` + `UNIQUE (degree_id, code)`; asignar las 49 asignaturas actuales a Informática | medio |
| 5 | **Renombrar** los códigos de las asignaturas de Informática a sus códigos oficiales USC (`FMAT1` → `G4012101`, …) con `UPDATE` **sobre las mismas filas** | medio |
| 6 | Insertar cursos y asignaturas de Química e IA (filas nuevas) | bajo |

El paso 5 es el delicado y **no puede ser un `delete` + `insert`**: los `files` y `temp_files` apuntan a
`subject_id`. Se actualiza `code` en la fila existente para que los archivos queden donde están.

### 10.2 Mapa de renombrado (Informática)

Las 49 asignaturas sembradas por `V8__seed_courses_subjects.sql` coinciden **una a una**, por nombre y por
curso, con el plan oficial de Ingeniería Informática. Ese mapeo es el de la migración:

| Código actual | Asignatura | Curso | Código oficial USC |
|---|---|---|---|
| `FMAT1` | Fundamentos de Matemáticas | 1 | `G4012101` |
| `ALG1` | Álgebra | 1 | `G4012102` |
| `MDIS1` | Matemática Discreta | 1 | `G4012103` |
| `SDIG1` | Sistemas Digitales | 1 | `G4012104` |
| `PROG1` | Programación I | 1 | `G4012105` |
| `FTFI1` | Fundamentos Tecnológicos y Físicos de la Informática | 1 | `G4012106` |
| `EST1` | Estadística | 1 | `G4012107` |
| `FCOMP1` | Fundamentos de Computadores | 1 | `G4012108` |
| `PROG2` | Programación II | 1 | `G4012109` |
| `CAN1` | Cálculo y Análisis Numérico | 1 | `G4012121` |
| `GFE2` | Gestión Financiera de Empresas | 2 | `G4012201` |
| `BD1` | Bases de Datos I | 2 | `G4012221` |
| `AED2` | Algoritmos y Estructuras de Datos | 2 | `G4012222` |
| `SO1` | Sistemas Operativos I | 2 | `G4012223` |
| `RED2` | Redes | 2 | `G4012224` |
| `POO2` | Programación Orientada a Objetos | 2 | `G4012225` |
| `BD2` | Bases de Datos II | 2 | `G4012226` |
| `SO2` | Sistemas Operativos II | 2 | `G4012227` |
| `AC2` | Arquitectura de Computadores | 2 | `G4012228` |
| `CG2` | Computación Gráfica | 2 | `G4012229` |
| `DS2` | Diseño de Software | 2 | `G4012230` |
| `TALF3` | Teoría de Autómatas y Lenguajes Formales | 3 | `G4012321` |
| `ASR3` | Administración de Sistemas y Redes | 3 | `G4012322` |
| `DAW3` | Desarrollo de Aplicaciones Web | 3 | `G4012323` |
| `GPI3` | Gestión de Proyectos Informáticos | 3 | `G4012324` |
| `IS3` | Ingeniería del Software | 3 | `G4012325` |
| `CD3` | Computación Distribuida | 3 | `G4012326` |
| `CI3` | Compiladores e Intérpretes | 3 | `G4012327` |
| `IA3` | Inteligencia Artificial | 3 | `G4012328` |
| `SI3` | Seguridad de la Información | 3 | `G4012329` |
| `CBS3` | Ciberseguridad | 3 | `G4012330` |
| `IPO4` | Interacción Persona-Ordenador | 4 | `G4012421` |
| `INGCOMP4` | Ingeniería de Computadores | 4 | `G4012422` |
| `TFG4` | Trabajo Fin de Grado | 4 | `G4012423` |
| `PEXT4` | Prácticas Externas | 4 | `G4012424` |
| `FSP4` | Fundamentos de Sistemas Paralelos | 4 | `G4012441` |
| `VISAV4` | Visualización Avanzada | 4 | `G4012442` |
| `CALSI4` | Calidad de los Sistemas de Información | 4 | `G4012443` |
| `AMD4` | Almacenes y Minería de Datos | 4 | `G4012444` |
| `CRA4` | Conocimiento y Razonamiento Automático | 4 | `G4012445` |
| `SINT4` | Sistemas Inteligentes | 4 | `G4012446` |
| `DAR4` | Diseño y Administración de Redes | 4 | `G4012447` |
| `INGSERV4` | Ingeniería de Servicios | 4 | `G4012448` |
| `CN4` | Computación en la Nube | 4 | `G4012449` |
| `CUB4` | Computación Ubicua | 4 | `G4012451` |
| `PAE4` | Programación de Arquitecturas Emergentes | 4 | `G4012452` |
| `GINE4` | Gestión de Información no estructurada | 4 | `G4012453` |
| `MTO4` | Modelos y Técnicas de Optimización | 4 | `G4012454` |
| `AA4` | Aprendizaje Automático | 4 | `G4012455` |

### 10.3 Qué NO se toca

* `vote_config`: sigue con su fila única (D5).
* `delegations`: queda sin uso (D7). No se borra en esta iteración para no tocar esquema sin necesidad.
* Archivos y binarios en S3: intactos.

### 10.4 Efectos colaterales en código existente

* `DataLoader` (perfil `local`/`demo`) busca `subjectRepository.findByCode("FMAT1")`: se rompe al renombrar.
  Debe pasar a usar los códigos nuevos y asignar grado a los usuarios demo.
* `TestDatabaseCleaner` y los tests que asumen 4 cursos / 1 grado / códigos `FMAT1` necesitan actualización.
* `MasterBootstrap` (perfil `prod`) crea un master sin grado: debe asignar el grado de Informática.
* `OpenApiContractTest`: actualizar la lista de operaciones públicas.

---

## 11. Plan de verificación

Tests de integración (perfil `test`), uno por regla. Cada uno debe verificar también el **efecto negativo**
(que la fila no se creó, que la cuota no se consumió):

| # | Regla | Test |
|---|---|---|
| V1 | Registro sin grado | `400 DEGREE_REQUIRED`, sin usuario persistido |
| V2 | Registro con grado inexistente | `400 DEGREE_NOT_FOUND` |
| V3 | Registro con grado | usuario persistido; login devuelve `degree*` e `isPlatformAdmin` |
| V4 | Migración | usuarios previos quedan en Informática; 49 asignaturas remapeadas; `files.subject_id` sin cambios |
| V5 | Catálogo | `GET /degrees` → 3; Informática 4 cursos / 49 asignaturas; Química 47; IA 50 |
| V6 | Independencia | dos asignaturas con el mismo nombre en grados distintos tienen `id` distinto y no comparten archivos |
| V7 | Subida en grado propio | propuesta creada, cuota consumida |
| V8 | Subida en grado ajeno | `403 DEGREE_SCOPE_FORBIDDEN`, **sin** `TempFile`, **sin** cuota consumida |
| V9 | Voto en propuesta ajena | `403`, sin `Vote` |
| V10 | Puntuación de archivo ajeno | `403`, sin `Vote` |
| V11 | Expulsión cross-grado | `403` aunque el actor sea `MASTER` de otro grado |
| V12 | Último master de un grado | `409 DEGREE_LAST_MASTER`; el mismo caso en otro grado no bloquea |
| V13 | Quórum por grado | con votos suficientes repartidos entre dos grados, la propuesta **no** se cierra |
| V14 | Lectura cross-grado | `GET /files/{id}` de otro grado → `200`, con visibilidad aplicada |
| V15 | `/admin/**` | `MASTER` → `403`; `ADMIN` → `200` |
| V16 | Contrato OpenAPI | `/degrees` presente; `OpenApiContractTest` actualizado |

---

## 12. Pendientes

Las decisiones de producto que bloqueaban la implementación están **cerradas** (D1–D13). Quedan dos asuntos
fuera de alcance y un supuesto que hay que confirmar antes de la tarea T14:

| # | Tema | Estado | Resolución |
|---|---|---|---|
| **P5** | Delegación de voto | **TODO** (D7) | No implementar. Si se retoma, debe ser por grado: hoy `Delegation` no tiene `subject_id` (está comentado) y sería un bypass |
| **P6** | Estadísticas por grado | fuera de alcance | `/stats` sigue global |
| **A1** | ¿Cómo se nombra al `ADMIN` y al primer master de un grado? | **resuelto**: por acceso directo a base de datos | No se implementa endpoint; el SQL está documentado en §6.4 |


P5 y P6 no bloquean nada. A1 sí bloquea la tarea de bootstrap (T14), no el resto del plan.

---

## 13. Documentos a actualizar al implementar

| Documento | Qué cambia |
|---|---|
| `Documentation/OVERVIEW.md` | Hoy describe una sola comunidad; agregar §grados y ámbito |
| `Documentation/ARCHITECTURE.md` | Árbol de entidades: `Degree`, campos nuevos, guard de ámbito |
| `Documentation/ENDPOINTS.md` | `/degrees` y campos nuevos |
| `Documentation/REPUTACION.md` | Aclarar que reputación es global y que la papeleta es por grado |
| `README.md` | Bullet de funcionalidades: multi-grado |
| Contrato OpenAPI | Se regenera con `OpenApiContractTest` |

---

## Anexo A — Fuentes del catálogo

Extraído el 2025-11-16 de las fichas oficiales de la USC (plan "por curso" y "por módulo/materia"):

| Grado | Código | URL |
|---|---|---|
| Ingeniería Informática | `G4012` | https://www.usc.gal/es/estudios/grados/ingenieria-arquitectura/grado-ingenieria-informatica-2aedicion |
| Ingeniería Química | `G4022` | https://www.usc.gal/es/estudios/grados/ingenieria-arquitectura/grado-ingenieria-quimica-2a-edicion |
| Inteligencia Artificial | `G4121` | https://www.usc.gal/es/estudios/grados/ingenieria-arquitectura/grado-inteligencia-artificial |

Los nombres de grado se registran **sin** la mención a "2ª edición", por indicación del equipo. El `code`
oficial de la USC sí conserva el identificador del plan vigente.

Si la USC publica un plan nuevo, este anexo y el catálogo se regeneran: el catálogo es **dato**, no código.
