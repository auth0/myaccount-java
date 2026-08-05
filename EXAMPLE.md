# Auth0 My Account Java SDK — Examples

SDK-level usage patterns for the **Auth0 My Account Java SDK**: authentication, async usage, three-state optional fields, error handling, and advanced configuration. For installation and setup, see the [README](./README.md); for per-endpoint request/response details, see the [API Reference](./reference.md).

## Authentication

The My Account API is **user-scoped**: it authenticates using the signed-in end user's access token with audience `https://{domain}/me/` and `me:` scopes. Application-only credentials (client credentials, private key JWT) are **not** supported.

> **Domain validation:** the builder expects a bare host such as `example.auth0.com`.
> Passing a value that includes a scheme (`https://example.auth0.com`), a trailing slash
> (`example.auth0.com/`), or a blank string throws `IllegalArgumentException` at build time.

### Static Token

Use this approach when you have a fixed access token (e.g., from a session object):

```java
import com.auth0.client.myaccount.auth.MyAccountClient;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("example.auth0.com")
    .staticToken(userAccessToken)  // Fixed token
    .build();

// Now use the client
client.authenticationMethods().list();
```

`.token(userAccessToken)` is a convenience alias for `.staticToken(userAccessToken)` — either sets a fixed bearer token.

### Token Provider (Dynamic Tokens)

Use a `TokenProvider` when tokens change frequently or need to be refreshed. `TokenProvider`
is a functional interface — its single method `Token getToken() throws IOException` is
invoked before every request, so a lambda works for simple cases:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.auth.Token;

public class SessionContext {
    private UserSession session;  // Assume this holds the current session

    public void setupClient() {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .tokenProvider(() -> Token.of(session.getAccessToken()))
            .build();

        // Token is fetched dynamically before each request
        client.authenticationMethods().list();
    }
}
```

The lambda `() -> Token.of(session.getAccessToken())` is invoked before every request, allowing tokens to refresh seamlessly.

Because `getToken()` may throw `IOException`, a provider that fetches a token over the network
can surface failures directly:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.auth.Token;
import com.auth0.client.myaccount.auth.TokenProvider;
import java.io.IOException;

TokenProvider provider = () -> {
    String accessToken = tokenStore.fetchFresh();  // may perform I/O
    if (accessToken == null) {
        throw new IOException("Unable to obtain an access token");
    }
    return Token.of(accessToken);
};

MyAccountClient client = MyAccountClient
    .builder()
    .domain("example.auth0.com")
    .tokenProvider(provider)
    .build();
```

For an explicit (non-lambda) implementation, wrap your provider in `DelegateTokenProvider`,
which validates that the delegate returns a non-null, non-empty token:

```java
import com.auth0.client.myaccount.auth.DelegateTokenProvider;
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.auth.Token;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("example.auth0.com")
    .tokenProvider(new DelegateTokenProvider(() -> Token.of(session.getAccessToken())))
    .build();
```

---

## Asynchronous Usage

For non-blocking operations, use `AsyncMyAccountClient`. All methods return `CompletableFuture`:

```java
import com.auth0.client.myaccount.auth.AsyncMyAccountClient;
import com.auth0.client.myaccount.auth.Token;
import java.util.concurrent.CompletableFuture;

public class AsyncExample {
    public static void main(String[] args) {
        AsyncMyAccountClient client = AsyncMyAccountClient
            .builder()
            .domain("example.auth0.com")
            .tokenProvider(() -> Token.of(getSessionToken()))
            .build();

        try {
            // List authentication methods asynchronously
            CompletableFuture<?> future = client.authenticationMethods().list()
                .thenAccept(methods -> System.out.println("Methods: " + methods))
                .exceptionally(e -> {
                    System.err.println("Error: " + e.getMessage());
                    return null;
                });

            // Wait for completion (not always necessary in reactive code)
            future.join();

        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
        }
    }

    private static String getSessionToken() {
        // Fetch token from session/context
        return "user_access_token";
    }
}
```

**Composing Async Operations:**

```java
import com.auth0.client.myaccount.auth.AsyncMyAccountClient;
import java.util.concurrent.CompletableFuture;

public class ComposedAsyncExample {
    public static void main(String[] args) {
        AsyncMyAccountClient client = AsyncMyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .build();

        // Chain multiple async operations
        client.factors().list()
            .thenCompose(factors -> {
                System.out.println("Factors: " + factors);
                return client.authenticationMethods().list();
            })
            .thenAccept(methods -> System.out.println("Methods: " + methods))
            .exceptionally(e -> {
                System.err.println("Error: " + e.getMessage());
                return null;
            });
    }
}
```

---

## Handling Optional Fields with OptionalNullable

Nullable request fields (such as query-parameter filters) use `OptionalNullable<T>` to distinguish between three states:

- **`.absent()`**: Field is omitted from the request (not sent)
- **`.ofNull()`**: Field is explicitly set to null (sent as `null`)
- **`.of(value)`**: Field has a value

```java
import com.auth0.client.myaccount.core.OptionalNullable;
import com.auth0.client.myaccount.types.ListAuthenticationMethodsRequestParameters;
import com.auth0.client.myaccount.types.FactorTypeEnum;
import com.auth0.client.myaccount.auth.MyAccountClient;

public class OptionalNullableExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .build();

        try {
            // Send the `type` filter with a value; use .absent() to omit it
            // or .ofNull() to send an explicit null.
            var methods = client.authenticationMethods().list(
                ListAuthenticationMethodsRequestParameters
                    .builder()
                    .type(OptionalNullable.of(FactorTypeEnum.PASSWORD))
                    .build()
            );

            System.out.println("Methods: " + methods);

        } catch (Exception e) {
            System.err.println("List failed: " + e.getMessage());
        }
    }
}
```

---

## Pagination

List endpoints that return many results (such as `connectedAccounts().list()`) return a
`SyncPagingIterable<T>`. Iterating over it transparently fetches subsequent pages, so you
can loop over every result without managing cursors yourself. Use `take` to set the page
size (1–20) and `from` to resume from a cursor.

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.OptionalNullable;
import com.auth0.client.myaccount.core.SyncPagingIterable;
import com.auth0.client.myaccount.types.ConnectedAccount;
import com.auth0.client.myaccount.types.ListConnectedAccountsRequestParameters;

public class PaginationExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .build();

        SyncPagingIterable<ConnectedAccount> accounts = client.connectedAccounts().list(
            ListConnectedAccountsRequestParameters
                .builder()
                .take(OptionalNullable.of(5)) // page size (1-20)
                .build()
        );

        // Iterate across all pages automatically.
        for (ConnectedAccount account : accounts) {
            System.out.println("Connected account: " + account);
        }
    }
}
```

The same pattern applies to `client.connectedAccounts().connections().list()`, which returns
a `SyncPagingIterable<ConnectedAccountConnection>`. The async client returns a
`CompletableFuture<SyncPagingIterable<T>>`.

---

## Error Handling

Catch `MyAccountApiException` to handle API errors:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.MyAccountApiException;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .build();

        try {
            client.authenticationMethods().delete("invalid_id");

        } catch (MyAccountApiException e) {
            // Access error details
            System.err.println("Status Code: " + e.statusCode());
            System.err.println("Error Body: " + e.body());
            System.err.println("Headers: " + e.headers());
            System.err.println("Full Exception: " + e);

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
```

---

## Advanced Configuration

### Request-Level Timeouts

Override the client's default timeout for specific requests:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.RequestOptions;
import java.util.concurrent.TimeUnit;

public class RequestTimeoutExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .timeout(30)  // Client-level: 30 seconds
            .build();

        try {
            // Override with a longer timeout for this request
            var methods = client.authenticationMethods().list(
                RequestOptions.builder()
                    .timeout(90, TimeUnit.SECONDS)
                    .build()
            );

            System.out.println("Methods: " + methods);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Custom Headers

Add custom headers at the client or request level:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.RequestOptions;

public class CustomHeadersExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .addHeader("X-Request-ID", "req-12345")
            .addHeader("X-Custom-Header", "custom-value")
            .build();

        try {
            // Add request-specific headers
            var methods = client.authenticationMethods().list(
                RequestOptions.builder()
                    .addHeader("X-Request-Trace", "trace-uuid")
                    .build()
            );

            System.out.println("Methods: " + methods);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Raw Response Access

Access response headers and body directly:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.MyAccountApiHttpResponse;
import java.util.List;

public class RawResponseExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .build();

        try {
            // Get raw response with headers
            MyAccountApiHttpResponse response = 
                client.authenticationMethods()
                    .withRawResponse()
                    .list();

            // Access body
            var methods = response.body();
            System.out.println("Methods: " + methods);

            // Access headers. Header keys are case-sensitive and are stored exactly
            // as returned by the server (Auth0 returns lowercase names).
            List<String> contentType = response.headers().get("content-type");
            System.out.println("content-type: " + contentType);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Retries and Backoff

Configure automatic retry behavior:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;

public class RetriesExample {
    public static void main(String[] args) {
        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .maxRetries(3)  // Retry up to 3 times on retryable errors
            .build();

        try {
            var methods = client.authenticationMethods().list();
            System.out.println("Methods: " + methods);

        } catch (Exception e) {
            System.err.println("Error after retries: " + e.getMessage());
        }
    }
}
```

**Default Behavior:**
- Retries on: 408 (Timeout), 429 (Too Many Requests), 5XX (Server Errors)
- Respects `Retry-After` and `X-RateLimit-Reset` headers
- Falls back to exponential backoff
- Default max retries: 2

### Custom OkHttpClient

Provide your own `OkHttpClient` for full control:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class CustomClientExample {
    public static void main(String[] args) {
        OkHttpClient customClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        MyAccountClient client = MyAccountClient
            .builder()
            .domain("example.auth0.com")
            .staticToken("user_access_token")
            .httpClient(customClient)
            .build();

        try {
            var methods = client.authenticationMethods().list();
            System.out.println("Methods: " + methods);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Logging

Enable request/response logging with `LogConfig`. Set the level, provide a custom `ILogger`,
or silence output entirely:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.LogConfig;
import com.auth0.client.myaccount.core.LogLevel;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("example.auth0.com")
    .staticToken("user_access_token")
    .logging(
        LogConfig.builder()
            .level(LogLevel.DEBUG)  // DEBUG, INFO, WARN, or ERROR
            .build()
    )
    .build();
```

To disable logging, set `.silent(true)` on the `LogConfig` builder.

---

## Next Steps

- Consult the [API Reference](./reference.md) for complete method signatures and parameters
- Review the [README](./README.md) for general setup and configuration
- Report issues and contributions on [GitHub](https://github.com/auth0/myaccount-java)
- For Auth0 API documentation, visit [Auth0 Developer Docs](https://auth0.com/docs)
