-- V11: catálogo multi-grado completo (D9, D13).
--
-- 1. Cursos de Ingeniería Química (G4022) e Inteligencia Artificial (G4121).
-- 2. Renombrado de las 49 asignaturas de Informática a su código oficial de la USC, MANTENIENDO la
--    misma fila: `files.subject_id` y `temp_files.subject_id` no se re-apuntan (§10.1 de GRADOS.md).
-- 3. Metadatos del plan oficial de cada asignatura (créditos, semestre, tipo).
-- 4. Alta de las 97 asignaturas de Química e IA.
--
-- Los nombres se conservan literales del plan oficial, incluida su capitalización irregular: son datos
-- de la USC y no se normalizan. `semester` es null en las asignaturas anuales.
--
-- Todo es idempotente (`on conflict do nothing`) para poder re-aplicarse en entornos de prueba.

-- ── 1. Cursos de los dos grados nuevos ───────────────────────────────────────
insert into courses (code, name, degree_id)
select v.code, v.name, d.id
  from (values ('1', 'Primer curso'), ('2', 'Segundo curso'),
               ('3', 'Tercer curso'), ('4', 'Cuarto curso')) as v(code, name)
 cross join degrees d
 where d.code in ('G4022', 'G4121')
    on conflict (degree_id, code) do nothing;

-- ── 2. Informática: códigos oficiales sobre las mismas filas ────────────────

update subjects s
   set code = v.new_code,
       credits = v.credits,
       semester = v.semester,
       subject_type = v.subject_type
  from (values
    ('FMAT1', 'G4012101', 6, 'Primer semestre', 'Formación básica'),
    ('ALG1', 'G4012102', 6, 'Primer semestre', 'Formación básica'),
    ('MDIS1', 'G4012103', 6, 'Primer semestre', 'Formación básica'),
    ('SDIG1', 'G4012104', 6, 'Primer semestre', 'Formación básica'),
    ('PROG1', 'G4012105', 6, 'Primer semestre', 'Formación básica'),
    ('FTFI1', 'G4012106', 6, 'Segundo semestre', 'Formación básica'),
    ('EST1', 'G4012107', 6, 'Segundo semestre', 'Formación básica'),
    ('FCOMP1', 'G4012108', 6, 'Segundo semestre', 'Formación básica'),
    ('PROG2', 'G4012109', 6, 'Segundo semestre', 'Formación básica'),
    ('CAN1', 'G4012121', 6, 'Segundo semestre', 'Obligatorio'),
    ('GFE2', 'G4012201', 6, 'Segundo semestre', 'Formación básica'),
    ('BD1', 'G4012221', 6, 'Primer semestre', 'Obligatorio'),
    ('AED2', 'G4012222', 6, 'Primer semestre', 'Obligatorio'),
    ('SO1', 'G4012223', 6, 'Primer semestre', 'Obligatorio'),
    ('RED2', 'G4012224', 6, 'Primer semestre', 'Obligatorio'),
    ('POO2', 'G4012225', 6, 'Primer semestre', 'Obligatorio'),
    ('BD2', 'G4012226', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('SO2', 'G4012227', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('AC2', 'G4012228', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('CG2', 'G4012229', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('DS2', 'G4012230', 6, 'Segundo semestre', 'Obligatorio'),
    ('TALF3', 'G4012321', 6, 'Primer semestre', 'Obligatorio'),
    ('ASR3', 'G4012322', 6, 'Primer semestre', 'Obligatorio'),
    ('DAW3', 'G4012323', 6, 'Segundo semestre', 'Obligatorio'),
    ('GPI3', 'G4012324', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('IS3', 'G4012325', 12, null, 'Anual'),
    ('CD3', 'G4012326', 6, 'Primer semestre', 'Obligatorio'),
    ('CI3', 'G4012327', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('IA3', 'G4012328', 6, 'Primer semestre', 'Obligatorio'),
    ('SI3', 'G4012329', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('CBS3', 'G4012330', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('IPO4', 'G4012421', 6, 'Primer semestre', 'Obligatorio'),
    ('INGCOMP4', 'G4012422', 6, 'Primer semestre', 'Obligatorio'),
    ('TFG4', 'G4012423', 12, null, 'Trabajos Fin de Grado y Máster'),
    ('PEXT4', 'G4012424', 9, null, 'Prácticas en Empresas de Grao e Máster'),
    ('FSP4', 'G4012441', 4.5, 'Primer semestre', 'Optativo'),
    ('VISAV4', 'G4012442', 4.5, 'Primer semestre', 'Optativo'),
    ('CALSI4', 'G4012443', 4.5, 'Primer semestre', 'Optativo'),
    ('AMD4', 'G4012444', 4.5, 'Primer semestre', 'Optativo'),
    ('CRA4', 'G4012445', 4.5, 'Primer semestre', 'Optativo'),
    ('SINT4', 'G4012446', 4.5, 'Primer semestre', 'Optativo'),
    ('DAR4', 'G4012447', 4.5, 'Primer semestre', 'Optativo'),
    ('INGSERV4', 'G4012448', 4.5, 'Primer semestre', 'Optativo'),
    ('CN4', 'G4012449', 4.5, 'Primer semestre', 'Optativo'),
    ('CUB4', 'G4012451', 4.5, 'Segundo semestre', 'Optativo'),
    ('PAE4', 'G4012452', 4.5, 'Segundo semestre', 'Optativo'),
    ('GINE4', 'G4012453', 4.5, 'Segundo semestre', 'Optativo'),
    ('MTO4', 'G4012454', 4.5, 'Segundo semestre', 'Optativo'),
    ('AA4', 'G4012455', 4.5, 'Segundo semestre', 'Optativo')
  ) as v(old_code, new_code, credits, semester, subject_type)
 where s.code = v.old_code;

-- ── 3. Química e IA: alta con sus metadatos ─────────────────────────────────

insert into subjects (course_id, code, name, credits, semester, subject_type)
select c.id, v.code, v.name, v.credits, v.semester, v.subject_type
  from (values
    ('G4022', '1', 'G4022101', 'Física', 6, 'Primer semestre', 'Formación básica'),
    ('G4022', '1', 'G4022102', 'Matemáticas', 6, 'Primer semestre', 'Formación básica'),
    ('G4022', '1', 'G4022103', 'Informática', 6, 'Primer semestre', 'Formación básica'),
    ('G4022', '1', 'G4022104', 'Fundamentos químicos en ingeniería', 6, 'Primer semestre', 'Formación básica'),
    ('G4022', '1', 'G4022105', 'Ecuaciones diferenciales', 6, 'Segundo semestre', 'Formación básica'),
    ('G4022', '1', 'G4022106', 'Estadística y ciencia de datos', 6, 'Segundo semestre', 'Formación básica'),
    ('G4022', '1', 'G4022107', 'Química inorgánica', 6, 'Segundo semestre', 'Formación básica'),
    ('G4022', '1', 'G4022121', 'Electrotecnia', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '1', 'G4022122', 'Fundamentos de procesos químicos I', 6, 'Primer semestre', 'Obligatorio'),
    ('G4022', '1', 'G4022123', 'Fundamentos de procesos químicos II', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022201', 'Química orgánica', 6, 'Primer semestre', 'Formación básica'),
    ('G4022', '2', 'G4022202', 'Organización y economía de empresa', 6, 'Primer semestre', 'Formación básica'),
    ('G4022', '2', 'G4022203', 'Expresión gráfica', 6, 'Segundo semestre', 'Formación básica'),
    ('G4022', '2', 'G4022221', 'Mecánica aplicada', 6, 'Primer semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022222', 'Termodinámica aplicada a la ingeniería química', 6, 'Primer semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022223', 'Transporte de fluidos', 6, 'Primer semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022224', 'Transmisión de calor', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022225', 'Ingeniería de la reacción química', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022226', 'Fundamentos de energética industrial', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '2', 'G4022227', 'Laboratorio de transporte de fluidos y transmisión de calor', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022321', 'Ciencia de materiales', 4.5, 'Primer semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022322', 'Resistencia de materiales', 6, 'Primer semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022323', 'Ingeniería ambiental', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022324', 'Operaciones de separación', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022325', 'Operaciones de transferencia de materia', 6, 'Primer semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022326', 'Reactores químicos', 4.5, 'Primer semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022327', 'Ingeniería bioquímica', 4.5, 'Primer semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022328', 'Control de procesos', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022329', 'Laboratorio de procesos químicos', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022330', 'Ingeniería de procesos', 4.5, 'Segundo semestre', 'Obligatorio'),
    ('G4022', '3', 'G4022341', 'Métodos numéricos', 4.5, 'Primer semestre', 'Optativo'),
    ('G4022', '3', 'G4022342', 'Operaciones con sólidos', 4.5, 'Primer semestre', 'Optativo'),
    ('G4022', '3', 'G4022343', 'Química analítica instrumental', 4.5, 'Primer semestre', 'Optativo'),
    ('G4022', '3', 'G4022344', 'Tecnologías de la valorización', 4.5, 'Segundo semestre', 'Optativo'),
    ('G4022', '3', 'G4022345', 'Procesos de química industrial', 4.5, 'Segundo semestre', 'Optativo'),
    ('G4022', '3', 'G4022346', 'Gestión de la calidad', 4.5, 'Segundo semestre', 'Optativo'),
    ('G4022', '4', 'G4022421', 'Seguridad y prevención de riesgos', 4.5, null, 'Obligatorio'),
    ('G4022', '4', 'G4022422', 'Desarrollo de proyectos', 6, null, 'Obligatorio'),
    ('G4022', '4', 'G4022423', 'Automática industrial', 4.5, null, 'Obligatorio'),
    ('G4022', '4', 'G4022424', 'Ingeniería de los sistemas de producción', 6, null, 'Obligatorio'),
    ('G4022', '4', 'G4022425', 'Simulación y optimización', 4.5, null, 'Obligatorio'),
    ('G4022', '4', 'G4022426', 'Aula profesional', 6, null, 'Obligatorio'),
    ('G4022', '4', 'G4022427', 'Prácticas en empresas', 6, null, 'Prácticas en Empresas de Grao e Máster'),
    ('G4022', '4', 'G4022428', 'Trabajo fin de grado', 18, null, 'Obligatorio'),
    ('G4022', '4', 'G4022441', 'Inteligencia artificial aplicada a la ingeniería química', 4.5, null, 'Optativo'),
    ('G4022', '4', 'G4022442', 'Ingeniería química y sostenibilidad', 4.5, null, 'Optativo'),
    ('G4022', '4', 'G4022443', 'Operaciones unitarias de la industria alimentaria', 4.5, null, 'Optativo'),
    ('G4121', '1', 'G4121101', 'Álgebra', 6, 'Primer semestre', 'Formación básica'),
    ('G4121', '1', 'G4121102', 'Cálculo y análisis numérico', 6, 'Primer semestre', 'Formación básica'),
    ('G4121', '1', 'G4121103', 'Matemática discreta', 6, 'Primer semestre', 'Formación básica'),
    ('G4121', '1', 'G4121104', 'Programación I', 6, 'Primer semestre', 'Formación básica'),
    ('G4121', '1', 'G4121105', 'Introducción a los computadores', 6, 'Primer semestre', 'Formación básica'),
    ('G4121', '1', 'G4121106', 'Estadística', 6, 'Segundo semestre', 'Formación básica'),
    ('G4121', '1', 'G4121107', 'Programación II', 6, 'Segundo semestre', 'Formación básica'),
    ('G4121', '1', 'G4121108', 'Adquisición y procesamiento de señal', 6, 'Segundo semestre', 'Formación básica'),
    ('G4121', '1', 'G4121109', 'Lógica', 6, 'Segundo semestre', 'Formación básica'),
    ('G4121', '1', 'G4121110', 'Gestión de organizaciones', 6, 'Segundo semestre', 'Formación básica'),
    ('G4121', '2', 'G4121221', 'Optimización matemática', 6, 'Primer semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121222', 'Algoritmos', 6, 'Primer semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121223', 'Ingeniería de software', 6, 'Primer semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121224', 'Bases de datos', 6, 'Primer semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121225', 'Redes', 6, 'Primer semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121226', 'Computación concurrente, paralela y distribuida', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121227', 'Autómatas y lenguajes formales', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121228', 'Fundamentos de aprendizaje automático', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121229', 'Algoritmos básicos de la inteligencia artificial', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4121', '2', 'G4121230', 'Representación del conocimiento y razonamiento', 6, 'Segundo semestre', 'Obligatorio'),
    ('G4121', '3', 'G4121341', 'Psicología cognitiva', 4.5, 'Segundo semestre', 'Optativo'),
    ('G4121', '3', 'G4121342', 'Neurofisiología', 3, 'Primer semestre', 'Optativo'),
    ('G4121', '3', 'G4121343', 'Neurociencia cognitiva y afectiva', 4.5, 'Segundo semestre', 'Optativo'),
    ('G4121', '3', 'G4121344', 'Metaheurísticas', 6, 'Primer semestre', 'Optativo'),
    ('G4121', '3', 'G4121345', 'Razonamiento con incertidumbre', 6, 'Segundo semestre', 'Optativo'),
    ('G4121', '3', 'G4121346', 'Ingeniería de datos a gran escala', 4.5, 'Primer semestre', 'Optativo'),
    ('G4121', '3', 'G4121347', 'Técnicas de procesamiento masivo de datos', 4.5, 'Primer semestre', 'Optativo'),
    ('G4121', '3', 'G4121348', 'Plataformas de internet de las cosas', 4.5, 'Primer semestre', 'Optativo'),
    ('G4121', '3', 'G4121349', 'Aprendizaje automático supervisado', 6, 'Primer semestre', 'Optativo'),
    ('G4121', '3', 'G4121350', 'Aprendizaje automático no supervisado', 4.5, 'Segundo semestre', 'Optativo'),
    ('G4121', '3', 'G4121351', 'Redes neuronales y aprendizaje profundo', 6, 'Segundo semestre', 'Optativo'),
    ('G4121', '3', 'G4121352', 'Proyecto integrador de IA I', 6, 'Segundo semestre', 'Optativo'),
    ('G4121', '4', 'G4121421', 'Trabajo fin de grado', 12, null, 'Trabajos Fin de Grado y Máster'),
    ('G4121', '4', 'G4121441', 'Dimensión jurídica de la IA', 3, 'Primer semestre', 'Optativo'),
    ('G4121', '4', 'G4121442', 'Aspectos tecnocientíficos de la IA', 3, 'Primer semestre', 'Optativo'),
    ('G4121', '4', 'G4121443', 'Aprendizaje por refuerzo', 6, 'Primer semestre', 'Optativo'),
    ('G4121', '4', 'G4121444', 'Visión por computador', 6, 'Primer semestre', 'Optativo'),
    ('G4121', '4', 'G4121445', 'Tecnologías del lenguaje', 6, 'Primer semestre', 'Optativo'),
    ('G4121', '4', 'G4121446', 'Proyecto integrador de IA II', 6, 'Primer semestre', 'Optativo'),
    ('G4121', '4', 'G4121447', 'Evaluación de proyectos empresariales', 6, 'Segundo semestre', 'Optativo'),
    ('G4121', '4', 'G4121448', 'Prácticas externas I', 6, null, 'Prácticas en Empresas de Grao e Máster'),
    ('G4121', '4', 'G4121449', 'Prácticas externas II', 6, null, 'Prácticas en Empresas de Grao e Máster'),
    ('G4121', '4', 'G4121450', 'Algoritmos verdes para inteligencia artificial', 3, null, 'Optativo'),
    ('G4121', '4', 'G4121451', 'Computación afectiva', 3, null, 'Optativo'),
    ('G4121', '4', 'G4121452', 'Computación de altas prestaciones para IA', 3, null, 'Optativo'),
    ('G4121', '4', 'G4121453', 'Inteligencia artificial en salud', 3, 'Segundo semestre', 'Optativo'),
    ('G4121', '4', 'G4121454', 'Minería de procesos', 3, null, 'Optativo'),
    ('G4121', '4', 'G4121455', 'Visión por computador II', 3, 'Segundo semestre', 'Optativo'),
    ('G4121', '4', 'G4121456', 'Web semántica y grafos de conocimiento', 3, 'Segundo semestre', 'Optativo'),
    ('G4121', '4', 'G4121457', 'Introducción al machine learning cuántico y a la optimización cuántica', 3, 'Segundo semestre', 'Optativo')
  ) as v(degree_code, course_code, code, name, credits, semester, subject_type)
  join degrees d on d.code = v.degree_code
  join courses c on c.degree_id = d.id and c.code = v.course_code
    on conflict (code) do nothing;
