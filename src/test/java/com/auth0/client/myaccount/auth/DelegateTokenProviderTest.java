package com.auth0.client.myaccount.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class DelegateTokenProviderTest {

    @Test
    void returnsTokenFromDelegate() throws Exception {
        Token token = Token.of("abc123");
        DelegateTokenProvider provider = new DelegateTokenProvider(() -> token);

        assertSame(token, provider.getToken());
    }

    @Test
    void rejectsNullDelegate() {
        assertThrows(NullPointerException.class, () -> new DelegateTokenProvider(null));
    }

    @Test
    void throwsWhenDelegateReturnsNullToken() {
        DelegateTokenProvider provider = new DelegateTokenProvider(() -> null);

        assertThrows(IOException.class, provider::getToken);
    }

    @Test
    void throwsWhenDelegateReturnsNullValue() {
        DelegateTokenProvider provider = new DelegateTokenProvider(() -> Token.of(null));

        assertThrows(IOException.class, provider::getToken);
    }

    @Test
    void throwsWhenDelegateReturnsEmptyValue() {
        DelegateTokenProvider provider = new DelegateTokenProvider(() -> Token.of(""));

        assertThrows(IOException.class, provider::getToken);
    }

    @Test
    void propagatesDelegateException() {
        DelegateTokenProvider provider = new DelegateTokenProvider(() -> {
            throw new IOException("boom");
        });

        IOException ex = assertThrows(IOException.class, provider::getToken);
        assertEquals("boom", ex.getMessage());
    }
}
