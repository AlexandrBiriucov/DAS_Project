# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/4.1.1/reference/testing/testcontainers.html#testing.testcontainers)
* [Testcontainers Postgres Module Reference Guide](https://java.testcontainers.org/modules/databases/postgres/)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [HTTP Client](https://docs.spring.io/spring-boot/4.1.1/reference/io/rest-client.html#io.rest-client.restclient)
* [Spring Security](https://docs.spring.io/spring-boot/4.1.1/reference/web/spring-security.html)
* [OAuth2 Client](https://docs.spring.io/spring-boot/4.1.1/reference/web/spring-security.html#web.security.oauth2.client)
* [OAuth2 Resource Server](https://docs.spring.io/spring-boot/4.1.1/reference/web/spring-security.html#web.security.oauth2.server)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Flyway Migration](https://docs.spring.io/spring-boot/4.1.1/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.1.1/reference/actuator/index.html)
* [Validation](https://docs.spring.io/spring-boot/4.1.1/reference/io/validation.html)
* [Testcontainers](https://java.testcontainers.org/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)

### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/4.1.1/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`postgres:latest`](https://hub.docker.com/_/postgres)

Please review the tags of the used images and set them to the same as you're running in production.

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

# jiggly application

Maven multi-module Spring Boot project.

| Module            | What it is                              | Port (host) |
|-------------------|-----------------------------------------|-------------|
| `jiggly`          | Parent aggregator (`packaging: pom`)    | —           |
| `jiggly-pocket`   | Document service                        | 8081        |
| `jiggly-web-bff`  | Backend-for-frontend (skeleton)         | —           |
| `keycloak`        | Auth server (compose only)              | 8088        |

## Prerequisites

- **JDK 25.**

- **Docker** with Compose v2, for the container workflow.

Use the wrapper (`./mvnw`) rather than a system `mvn` so everyone builds with the same Maven version.

## Maven

### Build

```bash
./mvnw clean install                  # whole reactor, with tests, into ~/.m2
./mvnw clean verify                   # same but does not publish to ~/.m2
./mvnw clean install -DskipTests      # compiles tests, does not run them
./mvnw clean install -Dmaven.test.skip=true   # does not even compile tests (faster)
```

`install` matters once a module depends on another module in this repo: it is what puts
`jiggly-pocket`/`jiggly-web-bff` jars into your local repository so other builds can resolve them.

### Build one module

```bash
./mvnw -pl jiggly-pocket -am clean install   # the module plus what it depends on
./mvnw -pl jiggly-pocket clean install       # the module alone
./mvnw clean install -rf :jiggly-pocket      # resume the reactor from this module
```

`-pl` = project list, `-am` = also make (build required modules), `-rf` = resume from.

### Tests

```bash
./mvnw test                                                   # all modules
./mvnw -pl jiggly-pocket test                                 # one module
./mvnw -pl jiggly-pocket test -Dtest=JigglyPocketApplicationTests
./mvnw -pl jiggly-pocket test -Dtest='JigglyPocketApplicationTests#contextLoads'
./mvnw -pl jiggly-pocket test -Dtest='Document*Test'          # pattern
```

Running `-Dtest=...` from the root without `-pl` **fails** in every module that has no matching
test, which right now means `jiggly-web-bff`:

```
No tests matching pattern "JigglyPocketApplicationTests" were executed!
```


### Run an app locally (no Docker)

```bash
./mvnw -pl jiggly-pocket spring-boot:run
./mvnw -pl jiggly-pocket spring-boot:run -Dspring-boot.run.arguments=--server.port=18081
./mvnw -pl jiggly-pocket spring-boot:run -Dspring-boot.run.profiles=local
```


## Docker

Compose builds `jiggly-pocket` from [`jiggly-pocket/Dockerfile`](jiggly-pocket/Dockerfile), which
compiles the module with Maven inside the image. You do **not** need to run a Maven build first, and
you do not need JDK 25 on your machine for this path.

### Normal loop

```bash
docker compose up -d --build        # rebuild changed images, then start
docker compose up -d                # start, reusing existing images
docker compose ps
docker compose logs -f jiggly-pocket
docker compose down                 # stop and remove containers
```

Without `--build`, compose reuses the image it already has and your code changes will not appear.

### Build an image from scratch

```bash
docker compose build --no-cache jiggly-pocket          # ignore all cached layers
docker compose build --no-cache --pull jiggly-pocket   # also re-pull the base images
```

`--no-cache` also discards the Maven dependency cache for that build, so expect a few minutes.

### Run compose with images built from scratch

```bash
docker compose build --no-cache
docker compose up -d --force-recreate
```

`--force-recreate` replaces containers even when compose thinks nothing changed, which is what you
want after rebuilding an image under the same tag.


## Smoke test

```bash
# create a document -> 201 with the new id in the body
curl -i -X POST http://localhost:8081/documents \
  -H 'Content-Type: application/json' \
  -d '{"title":"My first doc","description":"optional text"}'

# description is optional
curl -i -X POST http://localhost:8081/documents \
  -H 'Content-Type: application/json' \
  -d '{"title":"No description"}'

# fetch -> 200, body not implemented yet
curl -i http://localhost:8081/documents/42
```

Keycloak admin console: <http://localhost:8088> (`admin` / `admin`).



