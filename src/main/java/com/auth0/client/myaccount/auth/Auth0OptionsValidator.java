package com.auth0.client.myaccount.auth;

/**
 * Validates Auth0 My Account client configuration before building the client.
 *
 * <p>The My Account API is a user-scoped API ({@code /me/v1}); it only accepts an
 * end-user access token. Application-only grants (client credentials, private key JWT)
 * are intentionally not supported, so the valid authentication modes are a custom
 * {@link TokenProvider} or a static bearer token.
 */
final class Auth0OptionsValidator {

    private Auth0OptionsValidator() {}

    /**
     * Validates that exactly one authentication mode is configured.
     *
     * <p>Domain validation is owned by {@link Auth0Domain#validate(String)} and is not
     * repeated here.
     *
     * @param staticToken   static bearer token
     * @param tokenProvider custom token provider
     * @throws IllegalArgumentException if the configuration is invalid
     */
    static void validate(String staticToken, TokenProvider tokenProvider) {

        boolean hasStaticToken = staticToken != null && !staticToken.isEmpty();
        boolean hasTokenProvider = tokenProvider != null;

        int count = 0;
        if (hasStaticToken) count++;
        if (hasTokenProvider) count++;

        if (count > 1) {
            throw new IllegalArgumentException(
                    "auth0: only one authentication mode may be used (token provider or static token)");
        }
        if (count == 0) {
            throw new IllegalArgumentException(
                    "auth0: must provide either a token provider (tokenProvider) or a static token (staticToken)");
        }
    }
}
