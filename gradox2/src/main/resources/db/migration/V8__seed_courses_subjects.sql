-- V8: catálogo definitivo de cursos y asignaturas.
--
-- Migración puramente ADITIVA: solo INSERTs. No altera ni borra filas ni
-- esquema existentes, por lo que es seguro aplicarla sobre la base de datos
-- actual (evolución, no reemplazo). Se usa ON CONFLICT DO NOTHING para que
-- sea idempotente y respete cualquier curso/asignatura ya existente.

insert into courses (code, name)
values ('1', 'Primer curso'),
       ('2', 'Segundo curso'),
       ('3', 'Tercer curso'),
       ('4', 'Cuarto curso')
on conflict (code) do nothing;

insert into subjects (code, name, course_id)
values
    -- Primer curso
    ('FMAT1', 'Fundamentos de Matemáticas', (select id from courses where code = '1')),
    ('ALG1',  'Álgebra',                                    (select id from courses where code = '1')),
    ('MDIS1', 'Matemática Discreta',                        (select id from courses where code = '1')),
    ('SDIG1', 'Sistemas Digitales',                         (select id from courses where code = '1')),
    ('PROG1', 'Programación I',                             (select id from courses where code = '1')),
    ('FTFI1', 'Fundamentos Tecnológicos y Físicos de la Informática', (select id from courses where code = '1')),
    ('EST1',  'Estadística',                                (select id from courses where code = '1')),
    ('FCOMP1','Fundamentos de Computadores',                (select id from courses where code = '1')),
    ('PROG2', 'Programación II',                            (select id from courses where code = '1')),
    ('CAN1',  'Cálculo y Análisis Numérico',                (select id from courses where code = '1')),
    -- Segundo curso
    ('GFE2',  'Gestión Financiera de Empresas',             (select id from courses where code = '2')),
    ('BD1',   'Bases de Datos I',                           (select id from courses where code = '2')),
    ('AED2',  'Algoritmos y Estructuras de Datos',          (select id from courses where code = '2')),
    ('SO1',   'Sistemas Operativos I',                      (select id from courses where code = '2')),
    ('RED2',  'Redes',                                      (select id from courses where code = '2')),
    ('POO2',  'Programación Orientada a Objetos',           (select id from courses where code = '2')),
    ('BD2',   'Bases de Datos II',                          (select id from courses where code = '2')),
    ('SO2',   'Sistemas Operativos II',                     (select id from courses where code = '2')),
    ('AC2',   'Arquitectura de Computadores',               (select id from courses where code = '2')),
    ('CG2',   'Computación Gráfica',                        (select id from courses where code = '2')),
    ('DS2',   'Diseño de Software',                         (select id from courses where code = '2')),
    -- Tercer curso
    ('TALF3', 'Teoría de Autómatas y Lenguajes Formales',   (select id from courses where code = '3')),
    ('ASR3',  'Administración de Sistemas y Redes',         (select id from courses where code = '3')),
    ('DAW3',  'Desarrollo de Aplicaciones Web',             (select id from courses where code = '3')),
    ('GPI3',  'Gestión de Proyectos Informáticos',          (select id from courses where code = '3')),
    ('IS3',   'Ingeniería del Software',                    (select id from courses where code = '3')),
    ('CD3',   'Computación Distribuida',                    (select id from courses where code = '3')),
    ('CI3',   'Compiladores e Intérpretes',                 (select id from courses where code = '3')),
    ('IA3',   'Inteligencia Artificial',                    (select id from courses where code = '3')),
    ('SI3',   'Seguridad de la Información',                (select id from courses where code = '3')),
    ('CBS3',  'Ciberseguridad',                             (select id from courses where code = '3')),
    -- Cuarto curso
    ('IPO4',   'Interacción Persona-Ordenador',             (select id from courses where code = '4')),
    ('INGCOMP4','Ingeniería de Computadores',               (select id from courses where code = '4')),
    ('TFG4',   'Trabajo Fin de Grado',                      (select id from courses where code = '4')),
    ('PEXT4',  'Prácticas Externas',                        (select id from courses where code = '4')),
    ('FSP4',   'Fundamentos de Sistemas Paralelos',         (select id from courses where code = '4')),
    ('VISAV4', 'Visualización Avanzada',                    (select id from courses where code = '4')),
    ('CALSI4', 'Calidad de los Sistemas de Información',    (select id from courses where code = '4')),
    ('AMD4',   'Almacenes y Minería de Datos',              (select id from courses where code = '4')),
    ('CRA4',   'Conocimiento y Razonamiento Automático',    (select id from courses where code = '4')),
    ('SINT4',  'Sistemas Inteligentes',                     (select id from courses where code = '4')),
    ('DAR4',   'Diseño y Administración de Redes',          (select id from courses where code = '4')),
    ('INGSERV4','Ingeniería de Servicios',                  (select id from courses where code = '4')),
    ('CN4',    'Computación en la Nube',                    (select id from courses where code = '4')),
    ('CUB4',   'Computación Ubicua',                        (select id from courses where code = '4')),
    ('PAE4',   'Programación de Arquitecturas Emergentes',  (select id from courses where code = '4')),
    ('GINE4',  'Gestión de Información no estructurada',    (select id from courses where code = '4')),
    ('MTO4',   'Modelos y Técnicas de Optimización',        (select id from courses where code = '4')),
    ('AA4',    'Aprendizaje Automático',                    (select id from courses where code = '4'))
on conflict (code) do nothing;