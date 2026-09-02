// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
        classes = SwaggerUiLinkIntegrationTestSupport.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "swagger-ui-link.enabled=true",
                "spring.main.banner-mode=off",
                "spring.main.web-application-type=servlet"
        })
class SwaggerUiLinkMvcIntegrationTests extends SwaggerUiLinkIntegrationTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void usesSpringMvc() {
        assertThat(applicationContext.containsBean("dispatcherServlet")).isTrue();
    }
}
