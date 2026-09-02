// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

final class SwaggerUiLinkReporter {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerUiLinkReporter.class);

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final SwaggerUiLinkProperties properties;
    private final SwaggerUiUrlResolver resolver;

    SwaggerUiLinkReporter(ApplicationContext applicationContext, Environment environment,
            SwaggerUiLinkProperties properties, SwaggerUiUrlResolver resolver) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.properties = properties;
        this.resolver = resolver;
    }

    @EventListener
    void report(ApplicationReadyEvent event) {
        if (event.getApplicationContext() != applicationContext) {
            return;
        }
        if (!StringUtils.hasText(properties.getUrl())
                && !environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class, true)) {
            return;
        }

        try {
            logger.info("Swagger UI: {}", resolver.resolve(environment, properties));
        }
        catch (IllegalArgumentException exception) {
            logger.warn("Could not resolve Swagger UI URL: {}", exception.getMessage());
        }
    }
}
