package com.auth0.client.myaccount.auth;

import com.auth0.client.myaccount.AsyncMyAccountApi;
import com.auth0.client.myaccount.AsyncMyAccountApiBuilder;
import com.auth0.client.myaccount.core.ClientOptions;
import com.auth0.client.myaccount.core.Environment;
import com.auth0.client.myaccount.core.LogConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import okhttp3.OkHttpClient;

/**
 * Asynchronous Auth0 My Account API client with automatic token management.
 *
 * <p>Wraps the Fern-generated {@link AsyncMyAccountApi} with dynamic authentication,
 * Auth0 telemetry headers, and domain-based URL derivation.
 *
 * <p>The My Account API is a user-scoped API ({@code /me/v1}); it authenticates with the
 * signed-in end user's access token. Application-only grants (client credentials, private
 * key JWT) are intentionally not supported.
 *
 * <p>Example — custom token provider:
 * <pre>{@code
 * AsyncMyAccountClient client = AsyncMyAccountClient.builder()
 *     .domain("tenant.auth0.com")
 *     .tokenProvider(() -> Token.of(session.getAccessToken()))
 *     .build();
 *
 * client.factors().list();
 * }</pre>
 */
public final class AsyncMyAccountClient extends AsyncMyAccountApi {

    private AsyncMyAccountClient(ClientOptions clientOptions) {
        super(clientOptions);
    }

    /**
     * Creates a new builder for {@link AsyncMyAccountClient}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AsyncMyAccountApiBuilder {

        private String domain;
        private TokenProvider customTokenProvider;
        private String staticTokenValue;
        private final Map<String, String> auth0Headers = new HashMap<>();

        private TokenProvider resolvedTokenProvider;

        /**
         * The Auth0 tenant domain (e.g., "tenant.auth0.com").
         */
        public Builder domain(String domain) {
            this.domain = Objects.requireNonNull(domain, "domain must not be null");
            return this;
        }

        /**
         * Use a static bearer token (the end-user access token) for authentication.
         */
        public Builder staticToken(String token) {
            this.staticTokenValue = Objects.requireNonNull(token, "token must not be null");
            return this;
        }

        /**
         * Use a pre-built {@link TokenProvider} for authentication.
         */
        public Builder tokenProvider(TokenProvider tokenProvider) {
            this.customTokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider must not be null");
            return this;
        }

        // ---- Covariant overrides for method chaining ----

        @Override
        public Builder timeout(int timeout) {
            super.timeout(timeout);
            return this;
        }

        @Override
        public Builder maxRetries(int maxRetries) {
            super.maxRetries(maxRetries);
            return this;
        }

        @Override
        public Builder httpClient(OkHttpClient httpClient) {
            super.httpClient(httpClient);
            return this;
        }

        @Override
        public Builder logging(LogConfig logging) {
            super.logging(logging);
            return this;
        }

        @Override
        public Builder addHeader(String name, String value) {
            this.auth0Headers.put(name, value);
            return this;
        }

        /**
         * Sets a static bearer token. Delegates to {@link #staticToken(String)}.
         */
        @Override
        public Builder token(String token) {
            return staticToken(token);
        }

        // ---- Hook overrides ----

        @Override
        protected void setEnvironment(ClientOptions.Builder builder) {
            String validated = Auth0Domain.validate(domain);
            builder.environment(Environment.custom(Auth0Domain.deriveBaseURL(validated)));
        }

        @Override
        protected void setAuthentication(ClientOptions.Builder builder) {
            builder.addHeader("Authorization", () -> {
                try {
                    Token token = resolvedTokenProvider.getToken();
                    if (token == null
                            || token.getValue() == null
                            || token.getValue().isEmpty()) {
                        throw new IOException("Token provider returned a null or empty token");
                    }
                    return "Bearer " + token.getValue();
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to obtain access token", e);
                }
            });
        }

        @Override
        protected void setAdditional(ClientOptions.Builder builder) {
            builder.addHeader("Auth0-Client", Auth0ClientTelemetry.auth0ClientHeader());
            builder.addHeader("User-Agent", Auth0ClientTelemetry.userAgent());

            for (Map.Entry<String, String> entry : auth0Headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        @Override
        protected void validateConfiguration() {
            Auth0Domain.validate(domain);
            Auth0OptionsValidator.validate(staticTokenValue, customTokenProvider);
        }

        @Override
        public AsyncMyAccountClient build() {
            validateConfiguration();

            this.resolvedTokenProvider = resolveTokenProvider();

            ClientOptions clientOptions = buildClientOptions();
            return new AsyncMyAccountClient(clientOptions);
        }

        private TokenProvider resolveTokenProvider() {
            if (customTokenProvider != null) {
                return new DelegateTokenProvider(customTokenProvider);
            }
            if (staticTokenValue != null) {
                return new StaticTokenProvider(staticTokenValue);
            }
            throw new IllegalStateException("No authentication method configured");
        }
    }
}
