# Swagger UI Link Spring Boot Starter

Print the Swagger UI URL when a Spring Boot application is ready:

```text
Swagger UI: http://localhost:8080/swagger-ui.html
```

Most terminals and IDE consoles make the URL clickable. The starter does not install or configure Swagger UI; it reports the URL of the UI that your application already serves.

## Why this exists

I came to Spring Boot after working with TypeScript, Node, and Express applications. I missed the small, useful startup message that puts an application's important development URL directly in the console. This project brings that workflow to Spring Boot without adding a dashboard or requiring application-specific listener code.

This is inspiration from that developer experience, not a claim that Express itself universally prints a Swagger UI link.

## Status

Version `0.1.0` targets:

- Java 17 or newer
- Spring Boot 3.5.x with springdoc-openapi 2.9.x
- Spring Boot 4.1.x with springdoc-openapi 3.1.x
- Spring MVC and Spring WebFlux
- A separate springdoc management port

All four framework combinations are exercised by integration tests that start a real server, capture the printed URL, and request it over HTTP.

## Install

Add one dependency alongside the Swagger UI provider already used by the application.

Maven:

```xml
<dependency>
    <groupId>io.github.dydent</groupId>
    <artifactId>swagger-ui-link-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.dydent:swagger-ui-link-spring-boot-starter:0.1.0'
```

## Enable

The starter is deliberately disabled by default. Enable it in a local Spring profile:

```yaml
# application-local.yml
swagger-ui-link:
  enabled: true
```

Start the application. After Spring Boot publishes `ApplicationReadyEvent`, the console prints one INFO-level message:

```text
Swagger UI: http://localhost:8080/swagger-ui.html
```

For a profile-scoped dependency, Maven users can also put the dependency in a Maven `local` profile. The Spring property remains the final switch and avoids surprises if that Maven profile is accidentally used elsewhere.

## Configuration

| Property | Default | Purpose |
| --- | --- | --- |
| `swagger-ui-link.enabled` | `false` | Enables the startup message. |
| `swagger-ui-link.url` | empty | Complete HTTP(S) URL to print instead of inferring a local URL. |

An explicit URL is useful for a reverse proxy, Docker port mapping, a non-springdoc provider, or any setup where the process-local address is not the address a developer should open:

```yaml
swagger-ui-link:
  enabled: true
  url: https://api.local.example/docs
```

Only absolute `http` or `https` URLs are accepted, and credentials in the URL are rejected so they cannot be leaked into logs.

### Inferred springdoc URL

Without an explicit override, the starter reads Spring Boot's actual bound port and springdoc's public path properties. It understands:

- `local.server.port`
- `server.ssl.enabled`
- `server.servlet.context-path`
- `spring.mvc.servlet.path`
- `spring.webflux.base-path`
- `springdoc.swagger-ui.path`
- `springdoc.swagger-ui.use-root-path`
- `springdoc.swagger-ui.enabled`

This works with a fixed port and with `server.port=0`. If `springdoc.swagger-ui.enabled=false`, inferred reporting stays silent. An explicit `swagger-ui-link.url` still prints because another provider may own that URL.

### Separate management port

When springdoc is configured with `springdoc.use-management-port=true`, the starter uses `local.management.port` and combines:

- `management.server.base-path`
- `management.endpoints.web.base-path` (default `/actuator`)
- `/swagger-ui`

For example:

```yaml
management:
  server:
    port: 9090
  endpoints:
    web:
      exposure:
        include: swagger-ui
springdoc:
  use-management-port: true
swagger-ui-link:
  enabled: true
```

prints `http://localhost:9090/actuator/swagger-ui`. The endpoint still has to be exposed and allowed by the application's security rules.

## How it works

This is a Spring Boot auto-configuration library, not a Maven build plugin. A Maven or Gradle dependency places the JAR on the application's runtime classpath. Spring Boot discovers its auto-configuration, binds the two `swagger-ui-link` properties, waits until the web application is ready, then logs the resolved URI.

The artifact does not compile against springdoc classes. That keeps it usable with MVC, WebFlux, and other providers through `swagger-ui-link.url`, while the default inference follows springdoc's documented property conventions.

Resolution failures only produce a warning; this developer convenience must never prevent an application from starting.

## Boundaries

- It does not add springdoc, Swagger UI, routes, controllers, or browser assets.
- It does not open a browser automatically.
- It does not probe the endpoint during application startup.
- The inferred host is `localhost`. Containers, remote development machines, proxies, and forwarded public URLs should use `swagger-ui-link.url`.
- A local HTTPS certificate may still require browser trust.
- Spring Boot 2 and Springfox are outside the initial support scope.

## Existing options considered

- [springdoc-openapi](https://springdoc.org/) is still the recommended way to serve OpenAPI and Swagger UI. Its documentation tells developers the conventional URL and supports MVC, WebFlux, custom paths, and management ports, but its responsibility is serving the UI rather than emitting this focused ready-time console message.
- An application-local `ApplicationReadyEvent` listener is only a few lines and is reasonable for one application. This starter becomes useful when the same behavior would otherwise be copied across many repositories.
- [BootUI](https://github.com/jdubois/boot-ui) provides a much broader local developer console for Spring Boot and Quarkus. It is a good fit when health, metrics, mappings, and diagnostics are also wanted; it is much more than a single URL log line.
- [Bootify](https://bootify.io/) can generate a Spring Boot application with Swagger UI and documents its URL. It is a project generator, not a reusable dependency for existing applications.
- IDE-specific endpoint or OpenAPI tools can make navigation convenient, but the experience then depends on a particular editor and does not follow the application into CI logs or another developer's terminal.

The narrow starter is justified only if several applications need the same behavior. For a single service, the local listener remains the smallest solution.

## Development

Run the default Spring Boot 3/MVC suite:

```shell
mvn clean verify
```

Run the complete compatibility matrix:

```shell
mvn clean verify
mvn -Pwebflux clean verify
mvn -Dspring-boot.version=4.1.1 -Dspringdoc.version=3.1.0 clean verify
mvn -Pwebflux -Dspring-boot.version=4.1.1 -Dspringdoc.version=3.1.0 clean verify
```

See [the project plan](docs/PROJECT_PLAN.md) for the architecture decisions, release roadmap, and Maven Central checklist.

## Contributing and security

Contributions are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. Please report vulnerabilities using [SECURITY.md](SECURITY.md), not a public issue.

## License

Licensed under the [Apache License 2.0](LICENSE). It permits commercial and private use, modification, and redistribution while preserving notices, and includes an explicit patent grant appropriate for a reusable library.
