# Imagen base con JDK 21
FROM eclipse-temurin:21-jdk AS build

# Instalar Maven y compilar la app
WORKDIR /app
COPY gradox2/pom.xml .
COPY gradox2/mvnw .
COPY gradox2/.mvn .mvn
RUN ./mvnw dependency:go-offline

COPY gradox2/src src
RUN ./mvnw package -DskipTests

# Imagen final
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/gradox2-0.0.1-SNAPSHOT.jar app.jar

# Puerto por defecto de Spring Boot
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
