# Sistema de Triage y Gestión de Solicitudes Académicas (PISC)

El sistema **PISC** es una solución de backend robusta diseñada para gestionar el ciclo de vida completo de las solicitudes académicas dentro del Programa de Ingeniería de Sistemas y Computación. Emplea una arquitectura en capas (N-Tier) basada en los principios de **Diseño Guiado por el Dominio (DDD)** para garantizar la escalabilidad, seguridad y mantenibilidad.

## 🚀 Descripción General del Proyecto

El sistema automatiza la recepción, clasificación (triage), asignación y resolución de las solicitudes de los estudiantes. Cuenta con un **Motor de Reglas** inteligente para la asignación automática de prioridades y está diseñado para integrarse con modelos de clasificación impulsados por IA.

### Características Principales
*   **Gestión de Identidad y Acceso:** Gestión segura de usuarios con control de acceso basado en roles (RBAC) y cifrado de contraseñas con BCrypt.
*   **Catálogo de Solicitudes:** Gestión dinámica de tipos de solicitud y reglas de prioridad.
*   **Ciclo de Vida de Solicitudes:** Gestión completa del flujo de trabajo desde el registro (`REGISTRADA`) hasta el cierre (`CERRADA`), incluyendo transiciones para asignación, atención y rechazo.
*   **Triage Automatizado (Motor de Reglas):** Asignación automática de prioridad utilizando **Spring Expression Language (SpEL)** basado en reglas de negocio configurables.
*   **Auditoría y Trazabilidad:** Seguimiento exhaustivo de cada acción realizada sobre una solicitud.
*   **Control de Concurrencia:** Implementación de **Bloqueo Optimista** para manejar actualizaciones simultáneas de forma segura.

## 🏗️ Arquitectura y Stack Tecnológico

*   **Framework:** Spring Boot 3.2.4 (Java 17)
*   **Persistencia:** Spring Data JPA con Hibernate (ORM)
*   **Seguridad:** Spring Security (Stateless/Listo para JWT)
*   **Base de Datos:** MySQL (Hospedada en **Aiven**) / H2 (Opcional para desarrollo local)
*   **Contenedorización:** Docker (Build multi-etapa)
*   **Lógica de Negocio:** Diseño Guiado por el Dominio (DDD) con Raíces de Agregado (`SolicitudAcademica`) y Objetos de Valor (`Prioridad`).
*   **Validación:** Jakarta Bean Validation (Hibernate Validator).
*   **Documentación:** Especificación OpenAPI 3.0 (Swagger) incluida.

## 📂 Estructura del Proyecto

```bash
src/main/java/com/universidad/pisc/
├── identidad/   # Autenticación, Autorización y Gestión de Usuarios
├── catalogo/    # Tipos de Solicitud y Reglas de Prioridad (Configuración del Motor)
├── solicitudes/ # Lógica de Negocio Central: Ciclo de Vida, Triage y Auditoría
├── config/      # Configuración del Sistema (Seguridad, Manejo de Errores, etc.)
└── Application.java # Punto de Entrada Principal de Spring Boot
```

## 🛠️ Primeros Pasos

### Requisitos Previos
*   Java 17 (JDK)
*   Maven 3.6+
*   **Docker Desktop** (Recomendado para ejecución)

### Ejecución con Docker (Recomendado)
1.  Construir la imagen:
    ```bash
    docker build -t solicitudes-academicas .
    ```
2.  Correr el contenedor:
    ```bash
    docker run -p 8080:8080 --name app-pisc solicitudes-academicas
    ```

### Ejecución Local (Maven)
1.  Configurar `src/main/resources/application.properties` con las credenciales de tu base de datos.
2.  Ejecutar la aplicación:
    ```bash
    mvn spring-boot:run
    ```

La API estará disponible en `http://localhost:8080/v2`.

## 📜 Documentación de la API

El contrato completo de la API está definido en `openapi-solicitudes-academicas.yaml`. Puedes importar este archivo en herramientas como **Swagger Editor** o **Postman** para explorar los endpoints disponibles:

*   `/usuarios`: CRUD para usuarios del sistema.
*   `/tipos-solicitud`: Gestión del catálogo de solicitudes.
*   `/solicitudes`: Gestión del ciclo de vida central (Registrar, Clasificar, Asignar, Atender, Cerrar, Rechazar).

## 📈 Estado del Proyecto

El proyecto se encuentra actualmente en la **Etapa 5: Funcionalidades Avanzadas**.
- [x] **Etapas 1 y 2:** Modelos Fundacionales y Acceso a Datos.
- [x] **Etapa 3:** Lógica de Negocio Central (Agregado SolicitudAcademica).
- [x] **Etapa 4:** Implementación de la API de Solicitudes (Ciclo de Vida).
- [x] **Etapa 5 (Parte 1):** Motor de Reglas (Servicio de Triage) implementado.
- [ ] **Etapa 5 (Parte 2):** Sugerencias de IA y refinamientos de Auditoría Avanzada.

---
Desarrollado como parte de la iniciativa académica **PISC**.
