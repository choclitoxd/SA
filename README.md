# PISC — Sistema de Triage y Gestión de Solicitudes Académicas

Sistema full-stack para gestionar el ciclo de vida completo de solicitudes académicas en el Programa de Ingeniería de Sistemas y Computación. Implementa arquitectura DDD, motor de reglas SpEL y clasificación asistida por IA (Google Gemini).

---

## Estructura del Proyecto

```
SA/
├── backend/                  ← Spring Boot 3.2.4 (Java 17)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── env                   ← Variables de entorno (DB + Gemini API key)
├── frontend/                 ← Angular 17+ (TypeScript)
│   ├── src/
│   ├── Dockerfile            ← Build para producción (nginx)
│   ├── Dockerfile.test       ← Build para correr tests
│   ├── karma.conf.js
│   └── proxy.conf.json
├── docker-compose.yml        ← Levanta backend + frontend juntos
└── docker-compose.test.yml   ← Corre tests del frontend en Docker
```

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 3.2.4, Spring Security, Hibernate |
| Frontend | Angular 17+, TypeScript, SCSS |
| Base de datos | MySQL en Aiven Cloud |
| IA | LangChain4j + Google Gemini 2.5 Flash |
| Tests | Jasmine + Karma (189 tests) |
| Infraestructura | Docker, Docker Compose, nginx |

---

## Despliegue completo (recomendado)

Levanta backend y frontend con un solo comando desde la raíz del proyecto:

```powershell
docker-compose up --build
```

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080

Para detener todo:

```powershell
docker-compose down
```

Para reconstruir solo uno de los servicios:

```powershell
docker-compose up --build backend
docker-compose up --build frontend
```

---

## Desarrollo local

### Backend

**Requisitos:** Java 17, variables de entorno del archivo `backend/env`.

Desde la carpeta `backend/`, configurar las variables y ejecutar con Maven dentro de Docker:

```powershell
# Construir imagen del backend
docker build -t pisc-backend ./backend

# Correr con variables de entorno
docker run --rm -p 8080:8080 --env-file ./backend/env pisc-backend
```

O directamente desde IntelliJ IDEA abriendo `backend/pom.xml` como proyecto.

### Frontend

**Requisitos:** Node.js 20+, Angular CLI 17+.

```powershell
cd frontend

# Instalar dependencias
npm install

# Servidor de desarrollo (con proxy al backend en localhost:8080)
ng serve
```

La app queda disponible en http://localhost:4200. El proxy redirige `/solicitudes`, `/usuarios`, `/tipos-solicitud` y `/reglas-prioridad` al backend automáticamente.

---

## Credenciales por defecto

| Campo | Valor |
|---|---|
| Email | `admin@pisc.edu.co` |
| Contraseña | `admin123` |
| Rol | ADMINISTRATIVO |

Otros usuarios de prueba (identificación):
- `88888888` — Ana Martínez (ESTUDIANTE)
- `11111111` — Carlos (COORDINADOR)
- `55555555` — Juan (DOCENTE)

---

## Tests del frontend

### Correr localmente

```powershell
# Configurar Chrome (ruta del ejecutable en tu máquina)
$env:CHROME_BIN = "C:\Users\leoga\.cache\puppeteer\chrome\win64-149.0.7827.22\chrome-win64\chrome.exe"

cd frontend
ng test --watch=false --no-progress
```

### Correr en Docker

```powershell
docker-compose -f docker-compose.test.yml up --build
```

El contenedor termina solo. Exit code `0` = todos los tests pasan, `1` = algún test falla.

### Suite de tests (189 tests)

| Archivo | Descripción |
|---|---|
| `auth.service.spec.ts` | Login, roles, localStorage, tokens |
| `solicitudes.service.spec.ts` | Todos los endpoints HTTP de solicitudes |
| `usuarios.service.spec.ts` | CRUD de usuarios |
| `tipos-solicitud.service.spec.ts` | CRUD del catálogo |
| `reglas-prioridad.service.spec.ts` | CRUD de reglas SpEL |
| `auth.guard.spec.ts` | Protección de rutas autenticadas |
| `role.guard.spec.ts` | Control de acceso por rol |
| `auth.interceptor.spec.ts` | Inyección del header Authorization |
| `login.component.spec.ts` | Validación de formulario y flujos HTTP |
| `dashboard.component.spec.ts` | Renderizado de stats y tabla |
| `solicitudes-lista.component.spec.ts` | Paginación, filtros, badges de estado |
| `solicitud-detalle.component.spec.ts` | Botones por estado/rol, modal, sugerencia IA |
| `solicitud-nueva.component.spec.ts` | Validación y creación |
| `usuarios.component.spec.ts` | CRUD con modales |
| `tipos-solicitud.component.spec.ts` | CRUD del catálogo |
| `reglas-prioridad.component.spec.ts` | CRUD de reglas con condición SpEL |

---

## Ciclo de vida de una solicitud

```
REGISTRADA → CLASIFICADA → EN_ATENCION → ATENDIDA → CERRADA
                 ↓
             RECHAZADA
```

### Roles y permisos

| Acción | Roles permitidos |
|---|---|
| Crear solicitud | ESTUDIANTE, DOCENTE, ADMINISTRATIVO |
| Clasificar / Rechazar | COORDINADOR, ADMINISTRATIVO, DIRECTOR |
| Asignar responsable | COORDINADOR, ADMINISTRATIVO, DIRECTOR |
| Marcar atendida | DOCENTE, ADMINISTRATIVO, DIRECTOR, COORDINADOR |
| Cerrar | ESTUDIANTE, DOCENTE, ADMINISTRATIVO, DIRECTOR, COORDINADOR |
| Gestionar usuarios | ADMINISTRATIVO, DIRECTOR |
| Gestionar catálogo | ADMINISTRATIVO, COORDINADOR, DIRECTOR |

---

## Variables de entorno (`backend/env`)

```env
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:<port>/defaultdb?useSSL=true&...
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=<password>
GOOGLE_AI_GEMINI_API_KEY=<api-key>
```

Obtén una API Key de Gemini gratis en: https://aistudio.google.com/app/apikey

---

## Base de datos — tablas principales

| Tabla | Descripción |
|---|---|
| `usuario` | Usuarios del sistema |
| `rol` / `usuario_roles` | Roles y asignaciones |
| `tipo_solicitud` | Catálogo de tipos |
| `regla_prioridad` | Reglas SpEL del motor de triage |
| `solicitud_academica` | Solicitudes (entidad principal) |
| `asignacion` | Historial de asignaciones |
| `historial_solicitud` | Auditoría de cambios de estado |
| `sugerencia_ia` | Sugerencias generadas por Gemini |

---

## Solución de problemas frecuentes

**Puerto 8080 ocupado al levantar Docker:**
```powershell
# Ver qué proceso usa el puerto
netstat -ano | Select-String ":8080"
# Detener el proceso o el contenedor anterior
docker stop <nombre-contenedor>
```

**Backend no conecta a la DB (UnknownHostException):**
- El servicio MySQL en Aiven puede pausarse por inactividad. Ir a https://console.aiven.io y reactivarlo.
- Si el error persiste en Docker, asegurarse de que el `docker-compose.yml` tiene configurado `dns: [8.8.8.8, 8.8.4.4]`.

**Error de concurrencia al hacer acciones en serie:**
- Ya corregido en `SolicitudService` con `entityManager.refresh()` tras cada `save()`.
- El backend siempre devuelve la versión exacta de la DB.

**Frontend no conecta al backend (403 en login):**
- Asegurarse de usar `ng serve` (no abrir el HTML directamente).
- El `proxy.conf.json` redirige las llamadas al backend para evitar CORS.

---

Desarrollado como Proyecto Final — Programación Avanzada  
Ingeniería de Sistemas y Computación
