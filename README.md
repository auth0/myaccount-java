![Java SDK for Auth0 My Account](https://cdn.auth0.com/website/sdks/banners/myaccount-java.png)

<div align="center">

[![Maven Central](https://img.shields.io/maven-central/v/com.auth0/myaccount-java.svg?style=flat-square)](https://search.maven.org/artifact/com.auth0/myaccount-java)
[![License](https://img.shields.io/github/license/auth0/myaccount-java.svg?style=flat-square)](https://github.com/auth0/myaccount-java/blob/main/LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/auth0/myaccount-java/release.yml?branch=main&style=flat-square)](https://github.com/auth0/myaccount-java/actions?query=branch%3Amain)
[![fern shield](https://img.shields.io/badge/%F0%9F%8C%BF-Built%20with%20Fern-brightgreen)](https://buildwithfern.com?utm_source=github&utm_medium=github&utm_campaign=readme&utm_source=https%3A%2F%2Fgithub.com%2Fauth0%2Fmyaccount-java)

:books: [Documentation](#documentation) · :rocket: [Getting Started](#getting-started) · :speech_balloon: [Feedback](#feedback)

</div>

---

> [!WARNING]
> This SDK is currently in **beta**. APIs may change in backwards-incompatible ways before the stable release.

## Documentation

- [API Reference](./reference.md) - Complete API reference documentation.
- [Docs site](https://www.auth0.com/docs) — explore our docs site and learn more about Auth0.

## Getting Started

### Requirements

- Java 8+

### Installation

#### Gradle

```groovy
implementation 'com.auth0:myaccount-java:1.0.0-beta.0'
```

#### Maven

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>myaccount-java</artifactId>
    <version>1.0.0-beta.0</version>
</dependency>
```

### Usage

The `MyAccountClient` wraps the API with automatic authentication, Auth0 telemetry headers, and domain-based URL derivation. Instantiate it with your Auth0 tenant domain and the signed-in end user's access token:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .staticToken("<user-access-token>")
    .build();

client.authenticationMethods().list();
client.factors().list();
```

> **Note:** Supply a bare `domain` (e.g., `tenant.auth0.com`) — not a full URL. The client
> derives the base URL `https://{domain}/me/v1` for you. Values that include a scheme
> (`https://`) or a trailing slash are rejected.

The My Account API is a user-scoped API (`/me/v1`) and authenticates with the signed-in
end user's access token (audience `https://{domain}/me/`, with `me:` scopes). Application-only
grants (client credentials, private key JWT) are intentionally not supported.

## Authentication

The client obtains a bearer token before every request. Configure it with either a static
token or a dynamic token provider.

### Static Token

Use a fixed end-user access token:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .staticToken("<user-access-token>")
    .build();
```

### Token Provider

For tokens that change over time (for example, per-request tokens sourced from the current
user session), supply a `TokenProvider`. It is invoked before each request, so the token can
refresh dynamically. `TokenProvider` is a functional interface, so a lambda works for simple
cases:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.auth.Token;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .tokenProvider(() -> Token.of(session.getAccessToken()))
    .build();
```

## Async Client

For non-blocking calls, use `AsyncMyAccountClient`. It exposes the same builder options as
`MyAccountClient` and returns `CompletableFuture` results:

```java
import com.auth0.client.myaccount.auth.AsyncMyAccountClient;
import com.auth0.client.myaccount.auth.Token;

AsyncMyAccountClient client = AsyncMyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .tokenProvider(() -> Token.of(session.getAccessToken()))
    .build();

client.factors().list();
```

## OptionalNullable for PATCH Requests

For PATCH requests, the SDK uses `OptionalNullable<T>` to handle three-state nullable semantics:

- **ABSENT**: Field not provided (omitted from JSON)
- **NULL**: Field explicitly set to null (included as `null` in JSON)
- **PRESENT**: Field has a non-null value

```java
import com.auth0.client.myaccount.core.OptionalNullable;

UpdateRequest request = UpdateRequest.builder()
    .fieldName(OptionalNullable.absent())    // Skip field
    .anotherField(OptionalNullable.ofNull()) // Clear field
    .yetAnotherField(OptionalNullable.of("value")) // Set value
    .build();
```

### Important Notes

- **Required fields**: For required fields, you cannot use `absent()`. Required fields must always be present with either a non-null value or explicitly set to null using `ofNull()`.
- **Type safety**: `OptionalNullable<T>` is not fully type-safe since all three states use the same type, but it provides a cleaner API than nested `Optional<Optional<T>>` for handling three-state nullable semantics.

## Exception Handling

When the API returns a non-success status code (4xx or 5xx response), an API exception will be thrown.

```java
import com.auth0.client.myaccount.core.MyAccountApiException;

try{
    client.authenticationMethods().create(...);
} catch (MyAccountApiException e){
    // Do something with the API exception...
}
```

## Advanced

### Custom Client

This SDK is built to work with any instance of `OkHttpClient`. By default, if no client is provided, the SDK will construct one.
However, you can pass your own client like so:

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import okhttp3.OkHttpClient;

OkHttpClient customClient = ...;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .staticToken("<user-access-token>")
    .httpClient(customClient)
    .build();
```

### Retries

The SDK is instrumented with automatic retries with exponential backoff. A request will be retried as long
as the request is deemed retryable and the number of retry attempts has not grown larger than the configured
retry limit (default: 2). Before defaulting to exponential backoff, the SDK will first attempt to respect
the `Retry-After` header (as either in seconds or as an HTTP date), and then the `X-RateLimit-Reset` header
(as a Unix timestamp in epoch seconds); failing both of those, it will fall back to exponential backoff.

Which status codes are retried depends on the `retry-status-codes` generator configuration:

**`legacy`** (current default): retries on
- [408](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/408) (Timeout)
- [429](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429) (Too Many Requests)
- [5XX](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status#server_error_responses) (All server errors, including 500)

**`recommended`**: retries on
- [408](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/408) (Timeout)
- [429](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429) (Too Many Requests)
- [502](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/502) (Bad Gateway)
- [503](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/503) (Service Unavailable)
- [504](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/504) (Gateway Timeout)

Use the `maxRetries` client option to configure this behavior.

```java
import com.auth0.client.myaccount.auth.MyAccountClient;

MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .staticToken("<user-access-token>")
    .maxRetries(1)
    .build();
```

### Timeouts

The SDK defaults to a 60 second timeout. You can configure this with a timeout option at the client or request level.
```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.RequestOptions;

// Client level
MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .staticToken("<user-access-token>")
    .timeout(60)
    .build();

// Request level
client.authenticationMethods().create(
    ...,
    RequestOptions
        .builder()
        .timeout(60)
        .build()
);
```

### Custom Headers

The SDK allows you to add custom headers to requests. You can configure headers at the client level or at the request level.

```java
import com.auth0.client.myaccount.auth.MyAccountClient;
import com.auth0.client.myaccount.core.RequestOptions;

// Client level
MyAccountClient client = MyAccountClient
    .builder()
    .domain("tenant.auth0.com")
    .staticToken("<user-access-token>")
    .addHeader("X-Custom-Header", "custom-value")
    .addHeader("X-Request-Id", "abc-123")
    .build();
;

// Request level
client.authenticationMethods().create(
    ...,
    RequestOptions
        .builder()
        .addHeader("X-Request-Header", "request-value")
        .build()
);
```

### Access Raw Response Data

The SDK provides access to raw response data, including headers, through the `withRawResponse()` method.
The `withRawResponse()` method returns a raw client that wraps all responses with `body()` and `headers()` methods.
(A normal client's `response` is identical to a raw client's `response.body()`.)

```java
MyAccountApiHttpResponse response = client.authenticationMethods().withRawResponse().create(...);

System.out.println(response.body());
System.out.println(response.headers().get("X-My-Header"));
```

## Contributing

While we value open-source contributions to this SDK, this library is generated programmatically.
Additions made directly to this library would have to be moved over to our generation code,
otherwise they would be overwritten upon the next generated release. Feel free to open a PR as
a proof of concept, but know that we will not be able to merge it as-is. We suggest opening
an issue first to discuss with us!

On the other hand, contributions to the README are always very welcome!

### Raise an Issue

To provide feedback or report a bug, please [raise an issue on our issue tracker](https://github.com/auth0/myaccount-java/issues).

### Vulnerability Reporting

Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/responsible-disclosure-policy) details the procedure for disclosing security issues.

---

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: light)" srcset="https://cdn.auth0.com/website/sdks/logos/auth0_light_mode.png" width="150">
    <source media="(prefers-color-scheme: dark)" srcset="https://cdn.auth0.com/website/sdks/logos/auth0_dark_mode.png" width="150">
    <img alt="Auth0 Logo" src="https://cdn.auth0.com/website/sdks/logos/auth0_light_mode.png" width="150">
  </picture>
</p>

<p align="center">Auth0 is an easy to implement, adaptable authentication and authorization platform.<br />To learn more check out <a href="https://auth0.com/why-auth0">Why Auth0?</a></p>

<p align="center">This project is licensed under the Apache-2.0 license. See the <a href="./LICENSE"> LICENSE</a> file for more info.</p>
