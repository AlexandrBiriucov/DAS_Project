# syntax=docker/dockerfile:1

# Build context is the repository root (see compose.yaml), because the module
# needs the parent pom to build.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Every module listed in the parent's <modules> must exist for the reactor to load,
# even though only jiggly-pocket is built.
COPY pom.xml .
COPY jiggly-pocket/pom.xml jiggly-pocket/
COPY jiggly-web-bff/pom.xml jiggly-web-bff/
COPY jiggly-pocket/src jiggly-pocket/src

# The cache mount keeps ~/.m2 between builds, so dependencies download only once.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl jiggly-pocket -am package -DskipTests

# Split the fat jar into layers: dependencies change rarely, application code often.
RUN java -Djarmode=tools -jar jiggly-pocket/target/jiggly-pocket-*.jar \
        extract --layers --launcher --destination /workspace/extracted


FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER spring
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
