# Changelog

## [1.0.0-beta.0](https://github.com/auth0/myaccount-java/tree/1.0.0-beta.0) (2026-08-04)

### Features

- **User-Scoped API Access** - SDK for accessing the Auth0 My Account API (`/me/v1`), providing end-users programmatic access to manage their own account data.
- **End-User Authentication** - Authenticate using signed-in user access tokens (audience `https://{domain}/me/`) with automatic bearer token injection on every request.
- **Flexible Authentication Options** - Multiple authentication methods:
  - **Static Token** - Direct access token for testing or pre-obtained tokens.
  - **Custom Token Provider** - Bring your own `TokenProvider` implementation for dynamic token refresh.
- **Account Management Resources** - Access user-scoped endpoints:
  - **Factors** - Manage user authentication factors.
  - **Authentication Methods** - Manage user authentication methods.
  - **Connected Accounts** - Manage user connected accounts.
- **OptionalNullable for PATCH Requests** - Three-state nullable semantics (`absent`, `null`, `present`) for partial updates.
- **Auth0 Telemetry** - Automatic `Auth0-Client` and `User-Agent` headers on every request.
- **Domain-Based URL Derivation** - Automatically derives the base URL from the Auth0 tenant domain.
- **Raw Response Access** - Access HTTP headers and status codes via `withRawResponse()`.
- **Configurable Retries** - Automatic retries with exponential backoff on 408, 429, and 5XX responses, respecting `Retry-After` and `X-RateLimit-Reset` headers.
- **Configurable Timeouts** - Client-level and per-request timeout configuration (default 60 seconds).
- **Custom Headers** - Add custom HTTP headers at the client or request level.

### Installation

**Gradle**

```groovy
implementation 'com.auth0:myaccount-java:1.0.0-beta.0'
```

**Maven**

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>myaccount-java</artifactId>
    <version>1.0.0-beta.0</version>
</dependency>
```

### Basic Usage

```java
import com.auth0.client.myaccount.auth.MyAccountClient;

MyAccountClient client = MyAccountClient.builder()
    .domain("tenant.auth0.com")
    .staticToken("USER_ACCESS_TOKEN")
    .build();

System.out.println(client.factors().list());
```

### Dependencies

| Dependency | Version |
|---|---|
| OkHttp | 5.4.0 |
| Jackson Databind | 2.22.1 |
| Jackson Datatype JDK8 | 2.22.1 |
| Jackson Datatype JSR310 | 2.22.1 |

**Runtime Requirements:**
- Java 8+
