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

    /** Whether to also print the OpenAPI JSON URL after startup. */
    private boolean apiDocs;

    /** Complete HTTP(S) OpenAPI JSON URL to print; setting it also enables this link. */
    private String apiDocsUrl;

    /** Creates properties with reporting disabled and no URL overrides. */
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

    /** Returns whether ready-time OpenAPI JSON URL reporting is enabled. */
    public boolean isApiDocs() {
        return apiDocs;
    }

    /** Sets whether to also print the OpenAPI JSON URL after startup. */
    public void setApiDocs(boolean apiDocs) {
        this.apiDocs = apiDocs;
    }

    /** Returns the complete OpenAPI JSON URL override, or {@code null} when it should be inferred. */
    public String getApiDocsUrl() {
        return apiDocsUrl;
    }

    /** Sets the complete HTTP(S) OpenAPI JSON URL to print and enables this link. */
    public void setApiDocsUrl(String apiDocsUrl) {
        this.apiDocsUrl = apiDocsUrl;
    }
}
