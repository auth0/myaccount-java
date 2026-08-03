package com.auth0.client.myaccount.auth;

/**
 * Utilities for validating an Auth0 tenant domain and deriving the My Account API URL.
 */
final class Auth0Domain {

    private Auth0Domain() {}

    /**
     * Validates that the domain is a bare host with no scheme or path.
     *
     * <p>Callers must supply a bare domain such as {@code "tenant.auth0.com"}. Values that
     * include a scheme ({@code https://}, {@code http://}) or a trailing slash are rejected
     * so that misconfiguration surfaces early rather than producing a malformed base URL.
     *
     * @param domain the tenant domain
     * @return the validated domain, unchanged
     * @throws IllegalArgumentException if the domain is null, blank, or contains a scheme or slash
     */
    static String validate(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("auth0: domain must not be empty");
        }
        String d = domain.trim();
        if (d.contains("://")) {
            throw new IllegalArgumentException(
                    "auth0: domain must not include a scheme (e.g., \"https://\"); provide a bare host like \"tenant.auth0.com\"");
        }
        if (d.contains("/")) {
            throw new IllegalArgumentException(
                    "auth0: domain must not include a path or trailing slash; provide a bare host like \"tenant.auth0.com\"");
        }
        return d;
    }

    /**
     * Derives the base URL from a validated domain.
     *
     * @return {@code https://{domain}/me/v1}
     */
    static String deriveBaseURL(String domain) {
        return "https://" + domain + "/me/v1";
    }
}
