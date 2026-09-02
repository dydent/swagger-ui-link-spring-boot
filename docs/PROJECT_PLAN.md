# Swagger UI Link Spring Boot Starter: Project Plan

Last updated: 2026-09-02

## 1. Decision summary

Build one small, open-source Spring Boot starter JAR that prints a clickable Swagger UI URL after a web application is fully started.

| Decision | Choice |
| --- | --- |
| Local folder | `/Users/tobiasboner/Workzone/swagger-ui-link-spring-boot` |
| Public GitHub repository | `dydent/swagger-ui-link-spring-boot` |
| Maven coordinates | `io.github.dydent:swagger-ui-link-spring-boot-starter` |
| Java package | `io.github.dydent.swaggeruilink` |
| License | Apache License 2.0 |
| Packaging | One ordinary JAR with Spring Boot auto-configuration |
| Activation | Explicit, `swagger-ui-link.enabled=true` |
| First release | `0.1.0`, after Central account and namespace setup |
| Initial support | Java 17+, Spring Boot 3.5/4.1, MVC/WebFlux, springdoc 2.9/3.1 |

The name describes the behavior, remains searchable, and follows Spring Boot starter naming without using Spring's reserved `spring-boot-starter-*` prefix.

## 2. Problem and success criteria

Developers should not have to remember or reconstruct the Swagger UI URL after starting a service. The library succeeds when adding one dependency and one local-only property causes exactly one INFO log entry containing an address that opens the application's existing UI.

The initial release must:

1. Wait for `ApplicationReadyEvent`, so the runtime port is known.
2. Support fixed and random ports.
3. Combine Spring Boot context/base paths with springdoc's configured UI path.
4. Support servlet and reactive applications without separate published artifacts.
5. Support springdoc's separate management-port mode.
6. Allow an explicit absolute URL for proxies, containers, remote hosts, and other UI providers.
7. Remain disabled by default and never fail application startup.
8. Avoid a production dependency on springdoc.
9. Publish source, Javadoc, signatures, metadata, and the main JAR to Maven Central.

Non-goals for `0.1.0`: installing Swagger UI, opening a browser, discovering arbitrary routes, probing the endpoint on startup, adding a dashboard, supporting Spring Boot 2/Springfox, or calculating a public proxy URL from forwarded headers before a request exists.

## 3. Why a starter JAR, not a Maven plugin

A Maven plugin runs during the build. It does not normally live inside the running Spring application and cannot reliably know the final port, SSL mode, context path, reactive base path, or management port.

A library dependency is placed on the runtime classpath. Spring Boot can discover auto-configuration from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, bind configuration properties, and react to the actual application lifecycle. Calling the artifact a starter communicates this drop-in behavior. One JAR is enough because the implementation only uses Spring Boot and Spring Framework APIs shared by MVC and WebFlux.

## 4. Proposed behavior

Example local configuration:

```yaml
swagger-ui-link:
  enabled: true
```

Expected log after successful startup:

```text
Swagger UI: http://localhost:8080/swagger-ui.html
```

Resolution order:

1. If `swagger-ui-link.url` is set, validate and print it.
2. Otherwise, if `springdoc.use-management-port=true`, resolve the management port, SSL setting, base paths, and `/swagger-ui` actuator endpoint.
3. Otherwise, resolve the application port and SSL setting.
4. Prefer `spring.webflux.base-path` when present; otherwise combine `server.servlet.context-path` and `spring.mvc.servlet.path`.
5. Add `springdoc.swagger-ui.path`, defaulting to `/swagger-ui.html`, or preserve a trailing slash for springdoc root-path mode.
6. Normalize duplicate and missing slashes and build the final value with `java.net.URI`.

Invalid configuration produces a WARN message. It does not throw out of the ready-event listener. Explicit URLs only allow HTTP(S), require a host, and reject user information to avoid logging credentials.

## 5. Architecture

The implementation intentionally stays small:

```text
AutoConfiguration.imports
        |
SwaggerUiLinkAutoConfiguration
        |
SwaggerUiLinkReporter -- ApplicationReadyEvent --> INFO log
        |
SwaggerUiUrlResolver -- Environment + properties --> URI
```

- `SwaggerUiLinkProperties` exposes only `enabled` and `url`.
- `SwaggerUiLinkAutoConfiguration` activates only for web applications with the enable property.
- `SwaggerUiLinkReporter` owns lifecycle timing and non-fatal logging.
- `SwaggerUiUrlResolver` owns deterministic path and URL logic.
- There is no provider interface, factory, SPI, or reflection layer. An explicit URL covers providers that do not use springdoc conventions; abstractions can be added only if real provider integrations require them.

The project uses Maven because Maven Central publishing and Spring's Java ecosystem are straightforward with it. The library targets Java 17 bytecode, the baseline for Spring Boot 3.

## 6. Existing-solution evaluation

Research found adjacent tools but no small, maintained dependency with this exact cross-project contract:

| Option | What it solves | Fit for this goal |
| --- | --- | --- |
| [springdoc-openapi](https://springdoc.org/) | Generates OpenAPI and serves Swagger UI for MVC/WebFlux, including configurable and management-port routes. | Required provider in many target apps, but does not own this narrowly scoped console workflow. Integrate via documented properties, not springdoc internal classes. |
| Per-application ready listener | Can print any URL with no new shared artifact. | Best for one application; becomes duplicated policy across a fleet. |
| [BootUI](https://github.com/jdubois/boot-ui) | Rich local console with health, metrics, mappings, diagnostics, and more. | Strong broader alternative; too large in scope if the only requirement is one link. |
| [Bootify](https://bootify.io/) | Generates new Spring Boot projects with Swagger UI configured. | Useful at project creation, not a reusable solution for existing services. |
| IDE/OpenAPI plugins | Editor navigation and request tooling. | Helpful but editor-specific and invisible in ordinary terminal output. |
| Automatic browser launch | Opens the UI immediately. | Intrusive for CI, containers, remote sessions, and developers who do not want it; deliberately excluded. |

The starter should remain complementary to springdoc rather than reimplementing it. It must not import springdoc internals because those differ between major versions and would turn a one-line developer aid into a compatibility burden.

## 7. Compatibility and testing strategy

The test matrix is:

| Spring Boot | springdoc | MVC | WebFlux |
| --- | --- | --- | --- |
| 3.5.16 | 2.9.0 | Required | Required |
| 4.1.1 | 3.1.0 | Required | Required |

Tests are split by cost:

- Resolver unit tests cover ports, HTTPS, servlet paths, reactive paths, root-path mode, management paths, explicit URLs, invalid URLs, and missing ports.
- Auto-configuration tests cover disabled-by-default behavior, opt-in registration, ready-event output, and disabled springdoc UI behavior.
- Integration tests start real MVC or WebFlux servers on random ports, extract the logged link, follow its redirect, and require a successful Swagger UI HTTP response.
- A management integration test starts separate application and management ports and requests the printed management URL.

GitHub Actions runs all four combinations on Java 17. Local development may use a newer JDK, but the compiler emits Java 17 bytecode.

## 8. Open-source repository plan

The repository is public from its first push. The initial contents include source, tests, README, project plan, Apache-2.0 license, contribution guidance, security policy, CI, and a guarded release workflow.

Branch convention:

- `main` is the stable default branch.
- Work happens on `codex/initial-implementation` for the initial build and on focused branches later.
- Enable GitHub branch protection after the initial push: require CI, require pull requests, and block force-pushes to `main`.

Recommended repository topics: `spring-boot`, `swagger-ui`, `springdoc`, `openapi`, `developer-tools`, `java`, `maven`.

The README explicitly records the TypeScript/Node/Express developer-experience inspiration while avoiding the inaccurate claim that Express universally provides this message.

## 9. License decision

Use Apache License 2.0.

Both MIT and Apache-2.0 permit use, modification, redistribution, and commercial use. MIT is shorter, but Apache-2.0 provides an explicit patent license and clearer contribution/patent termination terms. That is preferable for a dependency intended for broad adoption in company Spring Boot projects. The tradeoff is a longer license and preservation of notices. No contributor license agreement is needed initially.

## 10. Maven Central publishing plan

Maven Central is a repository, not the build tool. Maven builds the files; the Central Publisher Portal validates and distributes them. Consumers then resolve the dependency without adding a custom repository.

### One-time maintainer setup

1. Sign in at [central.sonatype.com](https://central.sonatype.com/).
2. Claim and verify the namespace `io.github.dydent`. Sonatype's Portal supplies the exact GitHub verification step.
3. Generate a Central user token; store its username and password as GitHub Actions secrets `CENTRAL_USERNAME` and `CENTRAL_PASSWORD`.
4. Create a GPG signing key whose identity is controlled by the maintainer. Export the ASCII-armored private key to `GPG_PRIVATE_KEY` and its passphrase to `GPG_PASSPHRASE` in the protected GitHub `maven-central` environment.
5. Enable private vulnerability reporting and branch protection in GitHub repository settings.

Do not commit Central tokens, signing keys, passphrases, or a populated Maven `settings.xml`.

### Project-side requirements

The POM already contains the required coordinates, project name, description, project URL, Apache license, developer, and SCM metadata. The `release` Maven profile attaches source and Javadoc JARs, signs release files, and uploads with Sonatype's Central Publishing Maven Plugin. `autoPublish=false` deliberately leaves the final release click to a maintainer after Portal validation.

### First release checklist

1. Finish the public README and validate every link.
2. Change the POM version from `0.1.0-SNAPSHOT` to `0.1.0` and commit it.
3. Run all four matrix commands from the README.
4. Run `mvn -Prelease,mvc -Dgpg.skip=true clean verify` and inspect the main, source, and Javadoc JARs.
5. Create and push the signed tag `v0.1.0`.
6. The release workflow verifies the matrix, checks that the tag matches the POM, signs the artifacts, and runs `mvn deploy`.
7. In Central Publisher Portal, inspect the validated deployment and press Publish.
8. Wait for synchronization, then confirm the coordinates on [Maven Central](https://central.sonatype.com/).
9. Create GitHub Release `v0.1.0` with short installation and compatibility notes.
10. Return the POM on `main` to the next `-SNAPSHOT` version, for example `0.1.1-SNAPSHOT`.

Central releases are immutable. If `0.1.0` contains a bug after publication, release `0.1.1`; never attempt to replace `0.1.0`.

## 11. Delivery phases

### Phase A — implemented

- Repository structure and Maven metadata
- Opt-in Spring Boot auto-configuration
- URL resolver and safe explicit override
- MVC, WebFlux, management-port, SSL, base-path, and random-port behavior
- Unit, auto-configuration, and real HTTP integration tests
- Boot 3/4 compatibility matrix

### Phase B — repository launch

- Finish documentation and release configuration
- Run clean verification and artifact inspection
- Commit, create the public GitHub repository, push `main`, and add repository topics
- Configure branch protection after GitHub Actions has registered the CI check

### Phase C — first Maven Central release (maintainer credentials required)

- Complete Central namespace, token, GitHub environment, and GPG setup
- Release `0.1.0` using the checklist above
- Validate consumption in a separate minimal application from Maven Central

### Phase D — only after real feedback

- Add support for another provider only when a concrete provider and configuration contract are known.
- Add Spring Boot minors to CI before claiming support.
- Consider an optional ANSI/OSC-8 hyperlink only if terminals fail to auto-link the plain URL in practice.
- Consider Gradle build examples or a sample app only if users cannot adopt the two-property starter from the README.

No dashboard, browser launcher, plugin SPI, or multi-module split is planned without evidence that the one-JAR design has reached a real limit.

## 12. Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Printed local URL differs from a proxy/container URL | Use the explicit `swagger-ui-link.url` override. |
| UI provider is absent | Document that this starter reports but does not install Swagger UI; integration tests cover springdoc. |
| Provider changes its default path | Users can set the provider path or explicit URL; test supported springdoc majors in CI. |
| Management endpoint is not exposed or secured | Document exposure/security as application responsibilities and test a working configuration. |
| Logging leaks credentials | Reject URL user info; never inspect or print unrelated configuration. |
| Convenience code breaks startup | Catch resolution failures and log a warning. |
| Support matrix becomes expensive | Keep only current Boot 3 and Boot 4 representative versions until demand justifies more. |
| Central release is wrong | Keep manual Portal approval and remember that published versions are immutable. |

## 13. Release acceptance criteria

`0.1.0` is ready when:

- all four CI matrix jobs pass;
- the JAR contains auto-configuration imports and generated property metadata;
- the runtime dependency tree contains neither springdoc nor a web server;
- the class files target Java 17;
- source and Javadoc JARs build;
- a sample consumer prints one link and that link opens;
- public documentation describes defaults, limitations, license, inspiration, and security reporting;
- Central validates the signed deployment before manual publication.
