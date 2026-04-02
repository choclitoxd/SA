# Academic Request Triage and Management System (PISC)

The **PISC** system is a robust backend solution designed to manage the complete lifecycle of academic requests within the Systems and Computation Engineering Program. It employs a layered architecture (N-Tier) based on **Domain-Driven Design (DDD)** principles to ensure scalability, security, and maintainability.

## 🚀 Project Overview

The system automates the intake, classification (triage), assignment, and resolution of student requests. It features an intelligent **Rule Engine** for automated priority assignment and is designed to integrate with AI-driven classification models.

### Key Features
*   **Identity & Access Management:** Secure user management with role-based access control (RBAC) and BCrypt password encryption.
*   **Request Catalog:** Dynamic management of request types and priority rules.
*   **Request Lifecycle:** Full workflow management from registration (`REGISTRADA`) to closure (`CERRADA`), including transitions for assignment, attendance, and rejection.
*   **Automated Triage (Rule Engine):** Automatic priority assignment using **Spring Expression Language (SpEL)** based on configurable business rules.
*   **Audit & Traceability:** Comprehensive history tracking for every action taken on a request.
*   **Concurrency Control:** Implementation of **Optimistic Locking** to handle simultaneous updates safely.

## 🏗️ Architecture & Technology Stack

*   **Framework:** Spring Boot 3.2.4 (Java 17)
*   **Persistence:** Spring Data JPA with Hibernate (ORM)
*   **Security:** Spring Security (Stateless/JWT-ready)
*   **Database:** MySQL (Hosted on **Aiven**) / H2 (Optional for local dev)
*   **Containerization:** Docker (Multi-stage build)
*   **Business Logic:** Domain-Driven Design (DDD) with Aggregate Roots (`SolicitudAcademica`) and Value Objects (`Prioridad`).
*   **Validation:** Jakarta Bean Validation (Hibernate Validator).
*   **Documentation:** OpenAPI 3.0 (Swagger) specification included.

## 📂 Project Structure

```bash
src/main/java/com/universidad/pisc/
├── identidad/   # Authentication, Authorization, and User Management
├── catalogo/    # Request Types and Priority Rules (Rule Engine Configuration)
├── solicitudes/ # Core Business Logic: Lifecycle, Triage, and Audit
├── config/      # System Configuration (Security, etc.)
└── Application.java # Main Spring Boot Entry Point
```

## 🛠️ Getting Started

### Prerequisites
*   Java 17 (JDK)
*   Maven 3.6+
*   **Docker Desktop** (Recommended for deployment)

### Execution with Docker (Recommended)
1.  Build the image:
    ```bash
    docker build -t solicitudes-academicas .
    ```
2.  Run the container:
    ```bash
    docker run -p 8080:8080 --name app-pisc solicitudes-academicas
    ```

### Local Execution (Maven)
1.  Configure `src/main/resources/application.properties` with your database credentials.
2.  Run the application:
    ```bash
    mvn spring-boot:run
    ```

The API will be available at `http://localhost:8080/v2`.

## 📜 API Documentation

The complete API contract is defined in `openapi-solicitudes-academicas.yaml`. You can import this file into tools like **Swagger Editor** or **Postman** to explore the available endpoints:

*   `/usuarios`: CRUD for system users.
*   `/tipos-solicitud`: Management of the request catalog.
*   `/solicitudes`: Core lifecycle management (Register, Classify, Assign, Atender, Cerrar, Rechazar).

## 📈 Project Status

The project is currently in **Stage 5: Advanced Functionalities**.
- [x] **Stage 1 & 2:** Foundational Models and Data Access.
- [x] **Stage 3:** Core Business Logic (SolicitudAcademica Aggregate).
- [x] **Stage 4:** Request API Implementation (Lifecycle).
- [x] **Stage 5 (Part 1):** Rule Engine (Triage Service) implemented.
- [ ] **Stage 5 (Part 2):** AI Suggestions and Advanced Audit refinements.

---
Developed as part of the **PISC** academic initiative.
