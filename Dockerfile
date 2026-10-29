# Stage 1: build the WAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

# Stage 2: run on Tomcat (deployment target for Task 8)
FROM tomcat:10.1-jdk17-temurin
LABEL org.opencontainers.image.title="Institutional Repository Portal" \
      org.opencontainers.image.authors="Nilesh Patni"
RUN rm -rf /usr/local/tomcat/webapps/* && mkdir -p /data
COPY --from=build /src/target/irp-portal.war /usr/local/tomcat/webapps/ROOT.war
ENV APP_ENVIRONMENT=docker APP_VERSION=dev
EXPOSE 8080
VOLUME /data
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD curl -fs http://localhost:8080/actuator/health || exit 1
