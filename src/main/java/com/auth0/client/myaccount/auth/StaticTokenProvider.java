package com.auth0.client.myaccount.auth;

/**
 * A {@link TokenProvider} that always returns the same static token.
 *
 * <p>The token is never refreshed — when it expires, API calls will fail with 401.
 * This is suitable for short-lived scripts, testing, or when the caller manages
 * token lifecycle externally.
 */
final class StaticTokenProvider implements TokenProvider {

    private final Token token;

    StaticTokenProvider(String token) {
        this.token = Token.of(token);
    }

    @Override
    public Token getToken() {
        return token;
    }
}
