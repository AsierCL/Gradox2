# Gradox · Frontend

Interfaz web de **Gradox 2.0**, la biblioteca colaborativa multi-grado. Está construida con React, Vite y Tailwind CSS, y consume la API Spring Boot del repositorio.

El catálogo cubre Ingeniería Informática, Ingeniería Química e Inteligencia Artificial. El grado es permanente para cada usuario y define dónde puede escribir; la lectura y la descarga son libres entre grados.

La interfaz usa una escala de grises, capas de cristal, luz ambiental muy sutil y movimiento lento. El objetivo es que el contenido y las acciones tengan prioridad sobre la decoración.

## Puesta en marcha

Requisitos:

- Node.js 20.19+ (recomendado 22)
- La API de Gradox disponible en `http://localhost:8080`

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

La app queda en `http://localhost:5173`.

### Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `VITE_API_URL` | URL base de la API. Vacío = mismo host que la web. | vacío |
| `VITE_API_PORT` | Puerto de la API cuando `VITE_API_URL` está vacío. | `8080` |

El backend ya expone CORS para el entorno local. Con `VITE_API_URL` vacío, la web calcula la API a partir del host con el que se abre, así que también funciona desde otra máquina de la LAN (`http://192.168.x.x:5173`). Para una API remota o un túnel, define `VITE_API_URL`.

### Scripts

```bash
npm run dev      # servidor de desarrollo
npm run build    # build de producción en dist/
npm run preview  # previsualiza el build
npm run lint     # ESLint
```

## Qué incluye

- **Inicio** con buscador, últimos archivos de todos los grados y tarjetas para entrar en cada grado.
- **Grados** con índice público y pantalla por grado: cursos, asignaturas, créditos, semestre y tipo de materia.
- **Explorar** con búsqueda sin distinción de acentos, filtros por grado, curso, asignatura y tipo, ordenación y descarga directa.
- **Ficha de material** con grado visible, descarga mediante URL firmada, votación, visibilidad configurable, metadatos e hilo de comentarios anidados.
- **Cuenta institucional** con registro (incluye elección obligatoria de grado), verificación por correo, login, renovación de token, logout y recuperación de contraseña.
- **Perfil** con grado permanente, foto, edición de alias y contraseña, reputación, insignias y solicitud de promoción a master del grado propio.
- **Propuestas** de material con listado global en lectura, detalle, descarga del archivo propuesto y votación limitada al grado.
- **Subida** de material como propuesta, solo con asignaturas del grado propio, PDF de hasta 10 MB y elección de visibilidad.
- **Gobernanza para masters** con configuración global, promociones y expulsiones del grado.
- **Administración de plataforma** con resumen, propuestas globales, usuarios baneados, auditoría y corrección auditada de grados.
- **Comunidad** con las reglas de votación por grado, reputación, roles, privacidad y preguntas frecuentes.
- **Modo lectura entre grados**: las acciones de escritura se ocultan o deshabilitan y el `403 DEGREE_SCOPE_FORBIDDEN` se presenta como aviso neutro.
- **404** y estados de carga, error, sesión caducada y listas vacías en todas las vistas.

## Arquitectura

```text
src/
├── components/
│   ├── auth/          # layout compartido de acceso
│   ├── common/        # avisos de solo lectura y piezas compartidas
│   ├── degrees/       # tarjetas, badges y selector de grados
│   ├── file/          # filas de archivo y conversaciones
│   ├── governance/    # propuesta de configuración global
│   ├── home/          # buscador y navegador de cursos
│   ├── layout/        # shell, navbar, footer, fondo, guardas
│   ├── proposals/     # tarjetas y caja de votación
│   └── ui/            # sistema visual reutilizable
├── context/           # sesión, perfil y refresco de tokens
├── hooks/             # ámbito de grado reutilizable
├── lib/               # API, formato, constantes y utilidades
└── pages/             # una entrada por ruta
```

Decisiones técnicas:

- **TanStack Query** gestiona el estado del servidor y las invalidaciones tras votar, comentar o editar.
- **React Router 7** organiza rutas públicas, protegidas, exclusivas de master y exclusivas de administración de plataforma.
- **`useDegreeScope`** centraliza la comparación entre el grado del perfil y el del recurso; el backend sigue siendo la autoridad final.
- **Framer Motion** aporta entrada de página, revelados al hacer scroll y microinteracciones. Respeta `prefers-reduced-motion`.
- **Tailwind CSS** contiene el sistema visual. Los tokens de color, tipografía, radios y sombras viven en `tailwind.config.js` y `src/index.css`.
- **Phosphor Icons** se usa como familia única de iconos.
- Los tokens se guardan en `localStorage` y el cliente renueva la sesión automáticamente cuando la API responde `401`; el refresco conserva grado e `isPlatformAdmin`.
- Las rutas se cargan con `lazy()` para no enviar toda la aplicación en el primer paquete.

## Notas de integración

- El catálogo se navega desde `GET /degrees`; no existen listados globales de cursos ni asignaturas.
- Los archivos se agrupan por `subjectId`, nunca por nombre: dos grados pueden tener una asignatura con el mismo nombre.
- Las descargas usan el endpoint de la API que devuelve una URL firmada temporal; el navegador descarga directamente desde S3.
- La visibilidad `PUBLIC`, `RESTRICTED` y `PRIVATE` se interpreta según lo devuelto por la API.
- Los códigos `DEGREE_SCOPE_FORBIDDEN`, `DEGREE_REQUIRED`, `DEGREE_NOT_FOUND`, `DEGREE_IMMUTABLE` y `DEGREE_LAST_MASTER` se discriminan por `errorCode`.
- Las notificaciones internas y la moderación avanzada siguen en el roadmap del backend y no se presentan como funciones activas.
- Los enlaces de verificación y recuperación llegan desde el correo del backend. En local se pueden probar abriendo la URL con su `token`.

### Despliegue

El servidor estático debe aplicar *fallback* a `index.html` para cualquier ruta, porque React Router gestiona la navegación en cliente. Configura `VITE_API_URL` con la URL pública de la API antes de ejecutar `npm run build`.

## Diseño

El sistema se apoya en:

- Tipografía sans con fallback al sistema (`SF Pro`, Manrope).
- Escala de grises: `#edeff3`, `#ffffff`, `#1d1d1f`, `#6e6e73` y `#86868b`.
- Capas de cristal con desenfoque, borde interior claro y sombra amplia.
- Luz ambiental animada solo en escritorio; estática en móvil y bajo `prefers-reduced-motion`.
- Fallback sólido para `prefers-reduced-transparency` y mayor contraste para `prefers-contrast`.
- Contraste revisado para textos pequeños, foco visible y navegación por teclado.

La interfaz se ha comprobado a 320, 390, 768 y 1440 px, incluyendo todas las rutas principales sin desbordamiento horizontal.
