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

    @Test
    void printedUrlOpensSwaggerUi(CapturedOutput output) throws Exception {
        assertPrintedUrlOpens(output);
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

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
