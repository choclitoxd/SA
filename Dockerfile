# Etapa 1: Build (Compilación)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar solo el pom.xml primero para aprovechar el caché de Docker para las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el jar omitiendo los tests para el artefacto final
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Run (Ejecución)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Variable para la API Key de Gemini (se puede pasar al correr el contenedor)
ENV GOOGLE_AI_GEMINI_API_KEY=""

COPY --from=build /app/target/solicitudes-academicas-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Ejecutar con configuraciones de memoria optimizadas para contenedores
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
