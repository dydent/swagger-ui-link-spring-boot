// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Auto-configures ready-time Swagger UI URL reporting for web applications. */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "swagger-ui-link", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SwaggerUiLinkProperties.class)
public class SwaggerUiLinkAutoConfiguration {

    /** Creates the auto-configuration. */
    public SwaggerUiLinkAutoConfiguration() {
    }

    @Bean
    SwaggerUiLinkReporter swaggerUiLinkReporter(ApplicationContext applicationContext,
            Environment environment, SwaggerUiLinkProperties properties) {
        return new SwaggerUiLinkReporter(
                applicationContext, environment, properties, new SwaggerUiUrlResolver());
    }
}
