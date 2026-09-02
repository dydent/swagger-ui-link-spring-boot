// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;

@ExtendWith(OutputCaptureExtension.class)
class SwaggerUiLinkManagementIntegrationTests {

    @Test
    void printedManagementUrlOpensSwaggerUi(CapturedOutput output) throws Exception {
        SpringApplication application = new SpringApplication(
                SwaggerUiLinkIntegrationTestSupport.TestApplication.class);
        application.setWebApplicationType(WebApplicationType.valueOf(
                System.getProperty("test.web-application-type").toUpperCase()));

        try (ConfigurableApplicationContext ignored = application.run(
                "--server.port=0",
                "--management.server.port=0",
                "--management.endpoints.web.exposure.include=swagger-ui",
                "--springdoc.use-management-port=true",
                "--swagger-ui-link.enabled=true",
                "--spring.main.banner-mode=off")) {
            SwaggerUiLinkIntegrationTestSupport.assertPrintedUrlOpens(output);
        }
    }
}
