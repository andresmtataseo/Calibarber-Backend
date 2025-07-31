# Etapa 1: Construcción con Maven
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copia primero los archivos de construcción para cachear las dependencias
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# --- AÑADE ESTA LÍNEA PARA DAR PERMISOS ---
RUN chmod +x ./mvnw

# Ahora el script se puede ejecutar sin problemas
RUN ./mvnw dependency:go-offline

# Ahora copia el código fuente
COPY src ./src

# Construye la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa 2: Creación de la imagen final
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto que usará
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]