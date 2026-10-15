# syntax=docker/dockerfile:1

# ---------- Fase 1: compilazione con Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# La cache di Maven viene riusata tra una build e l'altra:
# le librerie non si riscaricano ogni volta.
# I test sono saltati perché richiedono la connessione al database.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

# ---------- Fase 2: immagine finale (solo Java, senza Maven) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Il programma non gira come amministratore
RUN useradd --system --no-create-home arena

COPY --from=build /app/target/*.jar app.jar

USER arena
EXPOSE 8080

# Le credenziali del database NON stanno nell'immagine:
# arrivano come variabili d'ambiente (vedi docker-compose.yml e .env)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
