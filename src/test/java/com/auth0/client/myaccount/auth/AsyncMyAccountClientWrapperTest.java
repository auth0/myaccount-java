package com.auth0.client.myaccount.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsyncMyAccountClientWrapperTest {

    private MockWebServer server;
    private OkHttpClient trustingClient;
    private String domain;

    @BeforeEach
    void setup() throws Exception {
        HeldCertificate localhostCert = new HeldCertificate.Builder()
                .addSubjectAlternativeName("localhost")
                .build();
        HandshakeCertificates serverCerts = new HandshakeCertificates.Builder()
                .heldCertificate(localhostCert)
                .build();
        HandshakeCertificates clientCerts = new HandshakeCertificates.Builder()
                .addTrustedCertificate(localhostCert.certificate())
                .build();

        server = new MockWebServer();
        server.useHttps(serverCerts.sslSocketFactory(), false);
        server.start();

        trustingClient = new OkHttpClient.Builder()
                .sslSocketFactory(clientCerts.sslSocketFactory(), clientCerts.trustManager())
                .build();

        // Bare host with no scheme/path; the wrapper derives https://{domain}/me/v1.
        domain = "localhost:" + server.getPort();
    }

    @AfterEach
    void teardown() throws Exception {
        server.shutdown();
    }

    private AsyncMyAccountClient.Builder clientBuilder() {
        return AsyncMyAccountClient.builder().domain(domain).httpClient(trustingClient);
    }

    private void enqueueFactors() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"factors\":[{\"type\":\"password\",\"usage\":[\"primary\"]}]}"));
    }

    @Test
    void sendsBearerHeaderFromStaticToken() throws Exception {
        enqueueFactors();
        AsyncMyAccountClient client = clientBuilder().staticToken("abc123").build();

        client.factors().list().get();

        RecordedRequest request = server.takeRequest();
        assertEquals("Bearer abc123", request.getHeader("Authorization"));
        assertTrue(request.getPath().endsWith("/me/v1/factors"), "path was " + request.getPath());
    }

    @Test
    void sendsAuth0TelemetryHeaders() throws Exception {
        enqueueFactors();
        AsyncMyAccountClient client = clientBuilder().staticToken("abc123").build();

        client.factors().list().get();

        RecordedRequest request = server.takeRequest();
        assertTrue(request.getHeader("Auth0-Client") != null, "Auth0-Client header missing");
        assertTrue(
                request.getHeader("User-Agent").startsWith("myaccount-java/"),
                "User-Agent was " + request.getHeader("User-Agent"));
    }

    @Test
    void refetchesTokenOnEveryRequest() throws Exception {
        enqueueFactors();
        enqueueFactors();
        AtomicInteger calls = new AtomicInteger();
        TokenProvider provider = () -> Token.of("token-" + calls.incrementAndGet());

        AsyncMyAccountClient client = clientBuilder().tokenProvider(provider).build();

        client.factors().list().get();
        client.factors().list().get();

        assertEquals(2, calls.get(), "token provider should be invoked once per request");
        assertEquals("Bearer token-1", server.takeRequest().getHeader("Authorization"));
        assertEquals("Bearer token-2", server.takeRequest().getHeader("Authorization"));
    }

    @Test
    void propagatesTokenProviderFailureAsUncheckedIo() throws Exception {
        enqueueFactors();
        AsyncMyAccountClient client = clientBuilder()
                .tokenProvider(() -> {
                    throw new java.io.IOException("no token available");
                })
                .build();

        // The auth header is resolved synchronously when the request is built, so the
        // failure surfaces on the calling thread rather than via the returned future.
        assertThrows(UncheckedIOException.class, () -> client.factors().list());
    }

    @Test
    void nullTokenValueFailsClosed() throws Exception {
        enqueueFactors();
        // A raw provider returning a null-valued Token must not send "Bearer null".
        AsyncMyAccountClient client =
                clientBuilder().tokenProvider(() -> Token.of(null)).build();

        assertThrows(UncheckedIOException.class, () -> client.factors().list());
    }

    // ---- Validation (no server round-trip) ----

    @Test
    void rejectsWhenNoAuthConfigured() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AsyncMyAccountClient.builder().domain("tenant.auth0.com").build());
    }

    @Test
    void rejectsWhenBothAuthModesConfigured() {
        assertThrows(IllegalArgumentException.class, () -> AsyncMyAccountClient.builder()
                .domain("tenant.auth0.com")
                .staticToken("abc")
                .tokenProvider(() -> Token.of("def"))
                .build());
    }

    @Test
    void rejectsBlankDomain() {
        assertThrows(IllegalArgumentException.class, () -> AsyncMyAccountClient.builder()
                .domain("   ")
                .staticToken("abc")
                .build());
    }

    @Test
    void rejectsDomainWithScheme() {
        assertThrows(IllegalArgumentException.class, () -> AsyncMyAccountClient.builder()
                .domain("https://tenant.auth0.com")
                .staticToken("abc")
                .build());
    }

    @Test
    void rejectsDomainWithTrailingSlash() {
        assertThrows(IllegalArgumentException.class, () -> AsyncMyAccountClient.builder()
                .domain("tenant.auth0.com/")
                .staticToken("abc")
                .build());
    }
}
