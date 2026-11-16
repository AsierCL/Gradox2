# Gradox · Frontend

Interfaz web de **Gradox 2.0**, la biblioteca colaborativa de Ingeniería Informática de la USC. Está construida con React, Vite y Tailwind CSS, y consume la API Spring Boot del repositorio.

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
| `VITE_API_URL` | URL base de la API Spring Boot | `http://localhost:8080` |

El backend ya expone CORS para el entorno local. Si usas otro host o un túnel, cambia `VITE_API_URL`.

### Scripts

```bash
npm run dev      # servidor de desarrollo
npm run build    # build de producción en dist/
npm run preview  # previsualiza el build
npm run lint     # ESLint
```

## Qué incluye

- **Inicio** con buscador, últimos archivos con descarga directa y navegación por curso y asignatura.
- **Explorar** con búsqueda sin distinción de acentos, filtros por curso, asignatura y tipo, ordenación y descarga directa desde el listado.
- **Ficha de material** con descarga mediante URL firmada, votación, visibilidad configurable, metadatos e hilo de comentarios anidados.
- **Cuenta USC** con registro, verificación por correo, login, renovación de token, logout y recuperación de contraseña.
- **Perfil** con foto, edición de alias y contraseña, reputación, insignias y solicitud de promoción a master.
- **Propuestas** con listado, detalle, descarga del archivo propuesto y votación ponderada.
- **Subida** de material como propuesta, con validación de PDF de hasta 10 MB y elección de visibilidad.
- **Administración para masters** con resumen, propuestas, usuarios baneados, auditoría y propuestas de cambio de política.
- **Comunidad** con las reglas de votación, reputación, roles, privacidad y preguntas frecuentes.
- **404** y estados de carga, error, sesión caducada y listas vacías en todas las vistas.

## Arquitectura

```text
src/
├── components/
│   ├── auth/          # layout compartido de acceso
│   ├── file/          # filas de archivo y conversaciones
│   ├── home/          # buscador y navegador de cursos
│   ├── layout/        # shell, navbar, footer, fondo, guardas
│   ├── proposals/     # tarjetas y caja de votación
│   └── ui/            # sistema visual reutilizable
├── context/           # sesión, perfil y refresco de tokens
├── lib/               # API, formato, constantes y utilidades
└── pages/             # una entrada por ruta
```

Decisiones técnicas:

- **TanStack Query** gestiona el estado del servidor y las invalidaciones tras votar, comentar o editar.
- **React Router 7** organiza rutas públicas, protegidas y exclusivas de master.
- **Framer Motion** aporta entrada de página, revelados al hacer scroll y microinteracciones. Respeta `prefers-reduced-motion`.
- **Tailwind CSS** contiene el sistema visual. Los tokens de color, tipografía, radios y sombras viven en `tailwind.config.js` y `src/index.css`.
- **Phosphor Icons** se usa como familia única de iconos.
- Los tokens se guardan en `localStorage` y el cliente renueva la sesión automáticamente cuando la API responde `401`.
- Las rutas se cargan con `lazy()` para no enviar toda la aplicación en el primer paquete.

## Notas de integración

- Las descargas usan el endpoint de la API que devuelve una URL firmada temporal; el navegador descarga directamente desde S3.
- La visibilidad `PUBLIC`, `RESTRICTED` y `PRIVATE` se interpreta según lo devuelto por la API.
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
