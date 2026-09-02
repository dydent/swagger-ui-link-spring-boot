// SPDX-License-Identifier: Apache-2.0
package io.github.dydent.swaggeruilink;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for Swagger UI URL reporting. */
@ConfigurationProperties("swagger-ui-link")
public class SwaggerUiLinkProperties {

    /** Whether to print the Swagger UI URL after startup. */
    private boolean enabled;

    /** Complete HTTP(S) URL to print instead of inferring a local URL. */
    private String url;

    /** Creates properties with reporting disabled and no URL override. */
    public SwaggerUiLinkProperties() {
    }

    /** Returns whether ready-time URL reporting is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether ready-time URL reporting is enabled. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Returns the complete URL override, or {@code null} when it should be inferred. */
    public String getUrl() {
        return url;
    }

    /** Sets the complete HTTP(S) URL to print instead of inferring a local URL. */
    public void setUrl(String url) {
        this.url = url;
    }
}
