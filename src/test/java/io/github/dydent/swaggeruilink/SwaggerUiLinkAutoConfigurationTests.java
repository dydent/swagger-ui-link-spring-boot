// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class SwaggerUiLinkAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwaggerUiLinkAutoConfiguration.class));

    @Test
    void isDisabledByDefault() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(SwaggerUiLinkReporter.class));
    }

    @Test
    void registersReporterWhenEnabled() {
        contextRunner
                .withPropertyValues("swagger-ui-link.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(SwaggerUiLinkReporter.class));
    }

    @Test
    void reportsResolvedUrlOnReadyEvent(CapturedOutput output) {
        contextRunner
                .withPropertyValues("swagger-ui-link.enabled=true", "local.server.port=8080")
                .run(context -> {
                    context.publishEvent(readyEvent(context.getSourceApplicationContext()));

                    assertThat(output).contains("Swagger UI: http://localhost:8080/swagger-ui.html");
                });
    }

    @Test
    void staysSilentWhenSpringdocUiIsDisabled(CapturedOutput output) {
        contextRunner
                .withPropertyValues(
                        "swagger-ui-link.enabled=true",
                        "local.server.port=8080",
                        "springdoc.swagger-ui.enabled=false")
                .run(context -> {
                    context.publishEvent(readyEvent(context.getSourceApplicationContext()));

                    assertThat(output).doesNotContain("Swagger UI:");
                });
    }

    @Test
    void explicitUrlWorksWhenSpringdocUiIsDisabled(CapturedOutput output) {
        contextRunner
                .withPropertyValues(
                        "swagger-ui-link.enabled=true",
                        "swagger-ui-link.url=https://docs.example.test/ui",
                        "springdoc.swagger-ui.enabled=false")
                .run(context -> {
                    context.publishEvent(readyEvent(context.getSourceApplicationContext()));

                    assertThat(output).contains("Swagger UI: https://docs.example.test/ui");
                });
    }

    @Test
    void invalidUrlWarnsWithoutFailingTheApplication(CapturedOutput output) {
        contextRunner
                .withPropertyValues(
                        "swagger-ui-link.enabled=true",
                        "swagger-ui-link.url=file:///tmp/docs")
                .run(context -> {
                    context.publishEvent(readyEvent(context.getSourceApplicationContext()));

                    assertThat(output)
                            .contains("Could not resolve Swagger UI URL")
                            .doesNotContain("Swagger UI: file:");
                });
    }

    private ApplicationReadyEvent readyEvent(org.springframework.context.ConfigurableApplicationContext context) {
        return new ApplicationReadyEvent(
                new SpringApplication(Object.class), new String[0], context, Duration.ZERO);
    }
}
