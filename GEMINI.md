# Proyecto: Sistema de Triage y Gestión de Solicitudes Académicas (PISC)

Este archivo sirve como contexto y guía para continuar el desarrollo del proyecto. Contiene el estado actual, la arquitectura y los pasos pendientes.

## 🚀 Estado del Proyecto
Actualmente se han completado las **Etapas 1, 2 y 3** del plan de desarrollo.

- [x] **Etapa 1: Fundación y Modelado del Dominio (Identidad y Catálogo).**
- [x] **Etapa 2: Acceso a Datos y API de Gestión (Usuarios y Tipos de Solicitud).**
- [x] **Etapa 3: Lógica de Negocio Principal - El Agregado `SolicitudAcademica`.**
- [ ] **Etapa 4: Implementación de la API de Solicitudes (Ciclo de Vida).**
- [ ] **Etapa 5: Funcionalidades Avanzadas (Motor de Reglas, IA, Auditoría).**

---

## 🏗️ Arquitectura y Tecnologías
- **Framework:** Spring Boot 3.2.4 (Java 17).
- **Persistencia:** Spring Data JPA con Hibernate (ORM).
- **Seguridad:** Spring Security (configurado con `BCrypt` para contraseñas).
- **Base de Datos:** H2 (en memoria para desarrollo).
- **Patrón Arquitectónico:** Arquitectura en Capas (N-Tier) con principios de Diseño Guiado por el Dominio (DDD).
- **Control de Concurrencia:** Optimistic Locking mediante `@Version`.

---

## 📂 Mapa de Implementación Actual

### 1. Identidad y Acceso (`com.universidad.pisc.identidad`)
- **Modelos:** `Usuario`, `Rol`, `NombreRol` (Enum).
- **Funcionalidad:** Gestión completa de usuarios con roles y cifrado de contraseñas.
- **Endpoints:** `/usuarios` (CRUD paginado).

### 2. Catálogo (`com.universidad.pisc.catalogo`)
- **Modelos:** `TipoSolicitud`, `ReglaPrioridad`, `NivelPrioridad` (Enum).
- **Funcionalidad:** Gestión de tipos de solicitud y definición de reglas de prioridad.
- **Endpoints:** `/tipos-solicitud` (CRUD con filtros).

### 3. Solicitudes (`com.universidad.pisc.solicitudes`)
- **Modelos (Agregado Raíz):** `SolicitudAcademica`.
- **Componentes:** `Prioridad` (Value Object), `Asignacion`, `HistorialSolicitud`, `SugerenciaIA`.
- **Enums:** `EstadoSolicitud`, `CanalOrigen`, `MotivoRechazo`.
- **Repositorios:** Todos los repositorios para estas entidades están creados.

---

## 📝 Próximos Pasos (Etapa 4)

El objetivo inmediato es implementar la **API de Solicitudes (`/solicitudes`)**.

### Tareas Pendientes:
1.  **DTOs de Solicitud:** Crear `RegistrarSolicitudRequest`, `SolicitudDetalleResponse`, etc.
2.  **Mapper:** Crear `SolicitudMapper` para transformar la entidad y sus relaciones.
3.  **Servicio de Solicitudes:** Implementar la lógica para:
    - Registrar una solicitud (estado inicial `REGISTRADA`).
    - Clasificar (cambio a `CLASIFICADA`).
    - Asignar responsable (cambio a `EN_ATENCION`).
    - Atender, Cerrar, Rechazar, etc.
4.  **Controlador de Solicitudes:** Exponer los endpoints definidos en el archivo `openapi-solicitudes-academicas.yaml`.

---

## 💡 Instrucciones para la IA
Cuando retomes este proyecto:
1. Lee este archivo `GEMINI.md`.
2. Revisa el archivo `openapi-solicitudes-academicas.yaml`  para los contratos de los endpoints.
3. Revisa el diagrama `domain-model-v2.puml`  para entender las relaciones entre entidades.
4. Continúa con la **Etapa 4** (DTOs y Servicio de Solicitudes).
