# Proyecto: Sistema de Triage y Gestión de Solicitudes Académicas (PISC)

Este archivo sirve como contexto y guía para continuar el desarrollo del proyecto. Contiene el estado actual, la arquitectura y los pasos pendientes.

## 🚀 Estado del Proyecto
Actualmente se han completado las **Etapas 1, 2, 3 y 4** del plan de desarrollo.

- [x] **Etapa 1: Fundación y Modelado del Dominio (Identidad y Catálogo).**
- [x] **Etapa 2: Acceso a Datos y API de Gestión (Usuarios y Tipos de Solicitud).**
- [x] **Etapa 3: Lógica de Negocio Principal - El Agregado `SolicitudAcademica`.**
- [x] **Etapa 4: Implementación de la API de Solicitudes (Ciclo de Vida).**
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

## 📝 Próximos Pasos (Etapa 5)

El objetivo ahora es implementar las **Funcionalidades Avanzadas**.

### Tareas Pendientes:
1.  **Motor de Reglas:** Implementar la lógica para evaluar `ReglaPrioridad` y asignar automáticamente el nivel de prioridad inicial.
2.  **Sugerencias de IA:** Implementar la integración (simulada o real) con un servicio de IA para clasificar solicitudes.
3.  **Auditoría y Historial:** Refinar la trazabilidad y los eventos de dominio.
4.  **Seguridad y Roles:** Ajustar las autorizaciones de los endpoints según el rol del usuario autenticado.

---

## 💡 Instrucciones para la IA
Cuando retomes este proyecto:
1. Lee este archivo `GEMINI.md`.
2. Revisa el archivo `openapi-solicitudes-academicas.yaml`  para los contratos de los endpoints.
3. Revisa el diagrama `domain-model-v2.puml`  para entender las relaciones entre entidades.
4. Continúa con la **Etapa 5** (Funcionalidades Avanzadas).
