package com.auth0.client.myaccount.auth;

/**
 * Represents an access token.
 */
public final class Token {

    private final String value;

    private Token(String value) {
        this.value = value;
    }

    /**
     * Creates a token from the given value.
     */
    public static Token of(String value) {
        return new Token(value);
    }

    public String getValue() {
        return value;
    }
}
