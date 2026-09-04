#BUILD
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package
# RUN mvn -B clean package -DskipTests usa esse aqui para dar skip em testes caso eles já estejam sendo executados no github actions, confirmar com jackson

# IMAGEM FINAL
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"] 
#aqui em cima é o "exec form" 