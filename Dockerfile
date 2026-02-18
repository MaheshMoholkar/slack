FROM node:20-alpine AS ui-build
WORKDIR /app/client

COPY client/package.json client/package-lock.json ./
RUN npm ci

COPY client/ ./

ARG VITE_API_URL=/api
ARG VITE_WS_URL=/ws
ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_WS_URL=${VITE_WS_URL}

RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS api-build
WORKDIR /app/server

COPY server/pom.xml ./
COPY server/.mvn .mvn
COPY server/mvnw ./
RUN chmod +x mvnw

RUN ./mvnw -q -DskipTests dependency:go-offline

COPY server/src src
COPY --from=ui-build /app/client/dist src/main/resources/static

RUN ./mvnw -q -DskipTests clean package && \
    JAR_FILE=$(ls target/*.jar | grep -v '\.original$' | head -n 1) && \
    cp "${JAR_FILE}" /app/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=api-build /app/app.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
