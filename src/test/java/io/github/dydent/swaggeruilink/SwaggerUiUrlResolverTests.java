// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SwaggerUiUrlResolverTests {

    private final SwaggerUiUrlResolver resolver = new SwaggerUiUrlResolver();

    @Test
    void resolvesDefaultUrlFromRuntimePort() {
        URI result = resolveWith("local.server.port", "8080");

        assertThat(result).hasToString("http://localhost:8080/swagger-ui.html");
    }

    @Test
    void combinesHttpsAndServletPaths() {
        URI result = resolveWith(
                "local.server.port", "8443",
                "server.ssl.enabled", "true",
                "server.servlet.context-path", "/api/",
                "spring.mvc.servlet.path", "/services/",
                "springdoc.swagger-ui.path", "/docs");

        assertThat(result).hasToString("https://localhost:8443/api/services/docs");
    }

    @Test
    void usesWebFluxBasePath() {
        URI result = resolveWith(
                "local.server.port", "8081",
                "server.servlet.context-path", "/ignored",
                "spring.webflux.base-path", "/reactive",
                "springdoc.swagger-ui.path", "docs");

        assertThat(result).hasToString("http://localhost:8081/reactive/docs");
    }

    @Test
    void keepsTrailingSlashForRootPathMode() {
        URI result = resolveWith(
                "local.server.port", "8080",
                "server.servlet.context-path", "/api",
                "springdoc.swagger-ui.use-root-path", "true");

        assertThat(result).hasToString("http://localhost:8080/api/");
    }

    @Test
    void resolvesManagementPortUrl() {
        URI result = resolveWith(
                "springdoc.use-management-port", "true",
                "local.management.port", "9090",
                "management.server.base-path", "/management",
                "management.endpoints.web.base-path", "/manage");

        assertThat(result).hasToString("http://localhost:9090/management/manage/swagger-ui");
    }

    @Test
    void explicitUrlWins() {
        SwaggerUiLinkProperties properties = new SwaggerUiLinkProperties();
        properties.setUrl("https://api.example.test/docs?group=public#top");
        MockEnvironment environment = new MockEnvironment().withProperty("local.server.port", "8080");

        assertThat(resolver.resolve(environment, properties))
                .hasToString("https://api.example.test/docs?group=public#top");
    }

    @Test
    void rejectsUnsafeExplicitUrl() {
        SwaggerUiLinkProperties properties = new SwaggerUiLinkProperties();
        properties.setUrl("https://user:secret@example.test/docs");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> resolver.resolve(new MockEnvironment(), properties))
                .withMessageContaining("credentials");
    }

    @Test
    void rejectsRelativeExplicitUrl() {
        SwaggerUiLinkProperties properties = new SwaggerUiLinkProperties();
        properties.setUrl("/docs");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> resolver.resolve(new MockEnvironment(), properties))
                .withMessageContaining("absolute HTTP(S)");
    }

    @Test
    void requiresRuntimePort() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> resolver.resolve(new MockEnvironment(), new SwaggerUiLinkProperties()))
                .withMessageContaining("local.server.port");
    }

    @Test
    void rejectsAbsoluteSpringdocPath() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> resolveWith(
                        "local.server.port", "8080",
                        "springdoc.swagger-ui.path", "https://example.test/docs"))
                .withMessageContaining("springdoc.swagger-ui.path")
                .withMessageContaining("path");
    }

    private URI resolveWith(String... properties) {
        MockEnvironment environment = new MockEnvironment();
        for (int index = 0; index < properties.length; index += 2) {
            environment.setProperty(properties[index], properties[index + 1]);
        }
        return resolver.resolve(environment, new SwaggerUiLinkProperties());
    }
}
