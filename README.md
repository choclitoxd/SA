# Sistema de Triage y Gestión de Solicitudes Académicas (PISC)

El sistema **PISC** es una solución de backend robusta diseñada para gestionar el ciclo de vida completo de las solicitudes académicas dentro del Programa de Ingeniería de Sistemas y Computación. Emplea una arquitectura en capas (N-Tier) basada en los principios de **Diseño Guiado por el Dominio (DDD)** para garantizar la escalabilidad, seguridad y mantenibilidad.

## 🚀 Descripción General del Proyecto

El sistema automatiza la recepción, clasificación (triage), asignación y resolución de las solicitudes de los estudiantes. Cuenta con un **Motor de Reglas** inteligente para la asignación automática de prioridades y clasificación asistida por **Inteligencia Artificial (Google Gemini)**.

### Características Principales
*   **Gestión de Identidad y Acceso:** Control de acceso basado en roles (RBAC) y cifrado BCrypt.
*   **Catálogo Dinámico:** Gestión de tipos de solicitud y reglas de prioridad en tiempo de ejecución.
*   **Ciclo de Vida:** Flujo completo desde `REGISTRADA` hasta `CERRADA`.
*   **Triage Inteligente:** Clasificación con **Google Gemini 1.5 Flash** y Motor de Reglas **SpEL**.
*   **Auditoría:** Trazabilidad total de acciones.

---

## 🏗️ Stack Tecnológico
*   **Backend:** Spring Boot 3.2.4 (**Java 17 LTS**)
*   **IA:** **LangChain4j** + **Google Gemini 1.5 Flash**
*   **Base de Datos:** MySQL (Hospedada en Aiven)
*   **Infraestructura:** Docker & Maven

---

## 📖 Guía de Ejecución (Paso a Paso)

### 1. Requisitos Previos
Asegúrate de tener instalados estos componentes:
*   **Java 17 JDK** (Obligatorio)
*   **Maven 3.9+** (Para ejecución normal)
*   **Docker Desktop** (Para ejecución en contenedor)
*   **Google AI API Key:** Obtén una gratis [aquí](https://aistudio.google.com/app/apikey).

### 2. Configuración de la IA (API Key)
El sistema requiere una llave para que la IA funcione. Configúrala así:
*   **Windows (PowerShell):** `$env:GOOGLE_AI_GEMINI_API_KEY="tu_llave"`
*   **Linux/Mac:** `export GOOGLE_AI_GEMINI_API_KEY="tu_llave"`

---

### 💻 Opción A: Ejecución Normal (Maven)
Usa esta opción si quieres desarrollar o modificar el código.

1.  **Instalar dependencias y compilar:**
    ```bash
    mvn clean install
    ```
2.  **Ejecutar la aplicación:**
    ```bash
    mvn spring-boot:run
    ```
3.  **Verificar:** La API estará lista en `http://localhost:8080/v2`.

---

### 🐳 Opción B: Ejecución con Docker
Usa esta opción para correr el proyecto de forma aislada sin instalar Maven o configurar Java manualmente.

1.  **Construir la Imagen:**
    ```bash
    docker build -t app-pisc .
    ```
2.  **Ejecutar el Contenedor:**
    ```bash
    docker run -p 8080:8080 -e GOOGLE_AI_GEMINI_API_KEY="tu_llave" --name app-pisc-container app-pisc
    ```
3.  **Verificar:** El contenedor estará corriendo y exponiendo el puerto 8080.

---

### 🧪 ¿Cómo probar el sistema?
Una vez ejecutado (por cualquier método), puedes enviar una solicitud de prueba:

*   **Herramienta:** Postman, Insomnia o `curl`.
*   **Endpoint:** `POST http://localhost:8080/v2/solicitudes`
*   **Cuerpo (JSON):**
    ```json
    {
      "solicitanteId": 1,
      "descripcion": "Solicito la cancelación de la materia Programación III por motivos de salud.",
      "canal": "WEB"
    }
    ```
*   **Respuesta:** Verás la solicitud creada junto con una `sugerenciaIA` que contiene la clasificación automática hecha por Gemini.

## 📜 Documentación Técnica
El contrato OpenAPI (Swagger) se encuentra en `openapi-solicitudes-academicas.yaml`.

## 📈 Estado del Desarrollo
- [x] **Identidad y Catálogo:** Completado.
- [x] **Ciclo de Vida de Solicitudes:** Completado.
- [x] **Motor de Triage (Reglas):** Completado.
- [x] **Integración IA (Gemini):** Completado.
- [ ] **Auditoría Avanzada:** En desarrollo.

---
Desarrollado para el Programa de Ingeniería de Sistemas y Computación (PISC).
