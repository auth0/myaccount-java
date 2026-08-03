package com.auth0.client.myaccount.auth;

import java.io.IOException;

/**
 * Provides access tokens for authenticating My Account API requests.
 *
 * <p>The My Account API operates in the context of the signed-in end user, so the
 * token returned here must be that user's <em>access token</em> (audience {@code https://{domain}/me/},
 * with {@code me:} scopes) — not an ID token.
 *
 * <p>Implementations handle token acquisition, caching, and refresh. The SDK calls
 * {@link #getToken()} before each HTTP request to obtain a valid token.
 *
 * <p>This is a functional interface, so lambdas can be used for simple cases:
 * <pre>{@code
 * TokenProvider provider = () -> Token.of(myUserAccessToken);
 * }</pre>
 */
@FunctionalInterface
public interface TokenProvider {

    /**
     * Returns a valid access token.
     *
     * @return a non-null {@link Token}
     * @throws IOException if the token cannot be obtained (e.g., network error)
     */
    Token getToken() throws IOException;
}
