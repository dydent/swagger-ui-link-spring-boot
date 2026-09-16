// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.system.CapturedOutput;

import static org.assertj.core.api.Assertions.assertThat;

abstract class SwaggerUiLinkIntegrationTestSupport {

    private static final Pattern SWAGGER_UI_LOG = Pattern.compile("Swagger UI: (https?://\\S+)");
    private static final Pattern OPENAPI_JSON_LOG = Pattern.compile("OpenAPI JSON: (https?://\\S+)");

    @Test
    void printedUrlsOpenDocumentation(CapturedOutput output) throws Exception {
        assertPrintedUrlOpens(output);
        assertPrintedApiDocsUrlOpens(output);
    }

    static void assertPrintedUrlOpens(CapturedOutput output) throws Exception {
        Matcher matcher = SWAGGER_UI_LOG.matcher(output.getOut());
        assertThat(matcher.find()).as("startup log contains the Swagger UI URL").isTrue();
        assertThat(output.getOut()).containsOnlyOnce("Swagger UI: http");

        HttpResponse<Void> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(
                        HttpRequest.newBuilder(URI.create(matcher.group(1))).GET().build(),
                        HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode()).isBetween(200, 299);
    }

    static void assertPrintedApiDocsUrlOpens(CapturedOutput output) throws Exception {
        Matcher matcher = OPENAPI_JSON_LOG.matcher(output.getOut());
        assertThat(matcher.find()).as("startup log contains the OpenAPI JSON URL").isTrue();
        assertThat(output.getOut()).containsOnlyOnce("OpenAPI JSON: http");

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(matcher.group(1))).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isBetween(200, 299);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
                .contains("application/json");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
