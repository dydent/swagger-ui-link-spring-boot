// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

final class SwaggerUiUrlResolver {

    URI resolve(Environment environment, SwaggerUiLinkProperties properties) {
        if (StringUtils.hasText(properties.getUrl())) {
            return explicitUrl(properties.getUrl(), "swagger-ui-link.url");
        }
        if (environment.getProperty("springdoc.use-management-port", Boolean.class, false)) {
            return managementUrl(environment);
        }
        return applicationUrl(environment);
    }

    URI resolveApiDocs(Environment environment, SwaggerUiLinkProperties properties) {
        if (StringUtils.hasText(properties.getApiDocsUrl())) {
            return explicitUrl(properties.getApiDocsUrl(), "swagger-ui-link.api-docs-url");
        }
        if (environment.getProperty("springdoc.use-management-port", Boolean.class, false)) {
            return managementApiDocsUrl(environment);
        }
        return applicationApiDocsUrl(environment);
    }

    private URI explicitUrl(String configuredUrl, String property) {
        URI uri;
        try {
            uri = new URI(configuredUrl.trim());
        }
        catch (URISyntaxException exception) {
            throw new IllegalArgumentException(property + " must be an absolute HTTP(S) URL", exception);
        }

        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || uri.getHost() == null || scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException(property + " must be an absolute HTTP(S) URL");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException(property + " must not contain credentials");
        }
        return uri;
    }

    private URI applicationUrl(Environment environment) {
        int port = requiredPort(environment, "local.server.port");
        String basePath = applicationBasePath(environment);

        boolean rootPath = environment.getProperty("springdoc.swagger-ui.use-root-path", Boolean.class, false);
        String swaggerPath = rootPath ? "/" : swaggerPath(environment);
        return localUri(scheme(environment, "server.ssl.enabled", false), port,
                rootPath ? rootPath(basePath) : path(basePath, swaggerPath));
    }

    private URI applicationApiDocsUrl(Environment environment) {
        return localUri(
                scheme(environment, "server.ssl.enabled", false),
                requiredPort(environment, "local.server.port"),
                path(applicationBasePath(environment), configuredPath(
                        environment, "springdoc.api-docs.path", "/v3/api-docs")));
    }

    private String applicationBasePath(Environment environment) {
        String basePath = environment.getProperty("spring.webflux.base-path");
        return StringUtils.hasText(basePath) ? basePath : path(
                environment.getProperty("server.servlet.context-path"),
                environment.getProperty("spring.mvc.servlet.path"));
    }

    private String swaggerPath(Environment environment) {
        return configuredPath(environment, "springdoc.swagger-ui.path", "/swagger-ui.html");
    }

    private String configuredPath(Environment environment, String property, String defaultValue) {
        String configuredPath = environment.getProperty(property, defaultValue);
        try {
            if (new URI(configuredPath).isAbsolute()) {
                throw new IllegalArgumentException(property + " must be a path; use a swagger-ui-link URL override"
                        + " for a complete URL");
            }
        }
        catch (URISyntaxException exception) {
            throw new IllegalArgumentException(property + " is not a valid path", exception);
        }
        return configuredPath;
    }

    private URI managementUrl(Environment environment) {
        int port = requiredPort(environment, "local.management.port");
        boolean applicationSsl = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        return localUri(
                scheme(environment, "management.server.ssl.enabled", applicationSsl),
                port,
                path(
                        environment.getProperty("management.server.base-path"),
                        environment.getProperty("management.endpoints.web.base-path", "/actuator"),
                        "/swagger-ui"));
    }

    private URI managementApiDocsUrl(Environment environment) {
        int port = requiredPort(environment, "local.management.port");
        boolean applicationSsl = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        return localUri(
                scheme(environment, "management.server.ssl.enabled", applicationSsl),
                port,
                path(
                        environment.getProperty("management.server.base-path"),
                        environment.getProperty("management.endpoints.web.base-path", "/actuator"),
                        "/openapi"));
    }

    private int requiredPort(Environment environment, String property) {
        Integer port = environment.getProperty(property, Integer.class);
        if (port == null || port < 1 || port > 65535) {
            throw new IllegalArgumentException(property + " is not available");
        }
        return port;
    }

    private String scheme(Environment environment, String property, boolean defaultValue) {
        return environment.getProperty(property, Boolean.class, defaultValue) ? "https" : "http";
    }

    private URI localUri(String scheme, int port, String path) {
        try {
            return new URI(scheme.toLowerCase(Locale.ROOT), null, "localhost", port, path, null, null);
        }
        catch (URISyntaxException exception) {
            throw new IllegalArgumentException("configured paths do not form a valid URL", exception);
        }
    }

    private String rootPath(String basePath) {
        String base = path(basePath);
        return base.equals("/") ? base : base + "/";
    }

    private String path(String... parts) {
        String joined = Arrays.stream(parts)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(part -> part.replaceAll("^/+|/+$", ""))
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("/"));
        return joined.isEmpty() ? "/" : "/" + joined;
    }
}
