# Reference
## factors
<details><summary><code>client.factors.list() -> ListFactorsResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

List of factors enabled for the Auth0 tenant and available for enrollment by this user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.factors().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## AuthenticationMethods
<details><summary><code>client.authenticationMethods.list() -> ListAuthenticationMethodsResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieve detailed list of authentication methods belonging to the authenticated user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authenticationMethods().list(
    ListAuthenticationMethodsRequestParameters
        .builder()
        .type(
            OptionalNullable.of(FactorTypeEnum.PASSWORD)
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**type:** `Optional<FactorTypeEnum>` — Filter authentication methods by type
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.authenticationMethods.create(request) -> CreateAuthenticationMethodResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Start the enrollment of a supported authentication method.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authenticationMethods().create(
    CreateAuthenticationMethodRequestContent.phone(
        CreatePhoneAuthenticationMethod
            .builder()
            .phoneNumber("phone_number")
            .build()
    )
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request:** `CreateAuthenticationMethodRequestContent` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.authenticationMethods.get(authenticationMethodId) -> GetAuthenticationMethodResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a single authentication method belonging to the authenticated user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authenticationMethods().get("authentication_method_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**authenticationMethodId:** `String` — Authentication Method ID. This value is part of the Location header returned when creating an authentication method. It should be used as it is, without any modifications.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.authenticationMethods.delete(authenticationMethodId)</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a single authentication method belonging to the authenticated user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authenticationMethods().delete("authentication_method_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**authenticationMethodId:** `String` — Authentication Method ID. This value is part of the Location header returned when creating an authentication method. It should be used as it is, without any modifications.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.authenticationMethods.update(authenticationMethodId, request) -> UpdateAuthenticationMethodResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates a single authentication method
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authenticationMethods().update(
    "authentication_method_id",
    UpdateAuthenticationMethodRequestContent
        .builder()
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**authenticationMethodId:** `String` — Authentication Method ID. This value is part of the Location header returned when creating an authentication method. It should be used as it is, without any modifications.
    
</dd>
</dl>

<dl>
<dd>

**name:** `Optional<String>` — The friendly name of the authentication method
    
</dd>
</dl>

<dl>
<dd>

**preferredAuthenticationMethod:** `Optional<PhoneAuthenticationMethodEnum>` — The preferred authentication method (for phone authenticators)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.authenticationMethods.verify(authenticationMethodId, request) -> VerifyAuthenticationMethodResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Confirm the enrollment of a supported authentication method.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authenticationMethods().verify(
    "authentication_method_id",
    VerifyAuthenticationMethodRequestContent.of(
        VerifyEmailAuthenticationMethod
            .builder()
            .authSession("Fe26.2**05c400ed...")
            .otpCode("123456")
            .build()
    )
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**authenticationMethodId:** `String` — Authentication Method ID. This value is part of the Location header returned when creating an authentication method. It should be used as it is, without any modifications.
    
</dd>
</dl>

<dl>
<dd>

**request:** `VerifyAuthenticationMethodRequestContent` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## ConnectedAccounts
<details><summary><code>client.connectedAccounts.create(request) -> CreateConnectedAccountsResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Start an authorization flow to link the authenticated user's account with an external identity provider.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.connectedAccounts().create(
    CreateConnectedAccountsRequestContent
        .builder()
        .connection("connection")
        .redirectUri("redirect_uri")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**connection:** `String` — The name of the connection to link the account with (e.g., 'google-oauth2', 'facebook').
    
</dd>
</dl>

<dl>
<dd>

**redirectUri:** `String` — The URI to redirect to after the connection process completes.
    
</dd>
</dl>

<dl>
<dd>

**state:** `Optional<String>` — An opaque value used to maintain state between the request and callback.
    
</dd>
</dl>

<dl>
<dd>

**codeChallenge:** `Optional<String>` — The PKCE code challenge derived from the code verifier.
    
</dd>
</dl>

<dl>
<dd>

**codeChallengeMethod:** `Optional<CreateConnectedAccountsRequestContentCodeChallengeMethod>` — The method used to derive the code challenge. Required when code_challenge is provided.
    
</dd>
</dl>

<dl>
<dd>

**scopes:** `Optional<List<String>>` — Defines the permissions that the client requests from the Identity Provider. Must include the standard scopes used to identify the user (e.g. 'openid', 'email', 'profile'), the scope required to obtain refresh tokens if needed (e.g. 'offline_access'), and any custom scopes the client needs to access protected resources.
    
</dd>
</dl>

<dl>
<dd>

**authorizationParams:** `Optional<AuthorizationParams>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.connectedAccounts.complete(request) -> CompleteConnectedAccountsResponseContent</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Complete a previously started authorization flow to link the authenticated user's account with an external identity provider.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.connectedAccounts().complete(
    CompleteConnectedAccountsRequestContent
        .builder()
        .authSession("auth_session")
        .connectCode("connect_code")
        .redirectUri("redirect_uri")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**authSession:** `String` — The authentication session identifier
    
</dd>
</dl>

<dl>
<dd>

**connectCode:** `String` — The authorization code returned from the connect flow
    
</dd>
</dl>

<dl>
<dd>

**redirectUri:** `String` — The redirect URI used in the original request
    
</dd>
</dl>

<dl>
<dd>

**codeVerifier:** `Optional<String>` — The PKCE code verifier
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.connectedAccounts.list() -> SyncPagingIterable&amp;lt;ConnectedAccount&amp;gt;</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieve connected accounts belonging to the authenticated user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.connectedAccounts().list(
    ListConnectedAccountsRequestParameters
        .builder()
        .connection(
            OptionalNullable.of(
                ConnectionNamesFilter.of("connection")
            )
        )
        .from(
            OptionalNullable.of("from")
        )
        .take(
            OptionalNullable.of(1)
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**connection:** `Optional<ConnectionNamesFilter>` — Filter connected accounts by connection names
    
</dd>
</dl>

<dl>
<dd>

**from:** `Optional<String>` — Cursor for pagination - start retrieving results from this point
    
</dd>
</dl>

<dl>
<dd>

**take:** `Optional<Integer>` — Number of results to return (1-20)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.connectedAccounts.delete(id)</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Delete a connected account belonging to the authenticated user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.connectedAccounts().delete("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — The unique identifier of the connected account
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## ConnectedAccounts Connections
<details><summary><code>client.connectedAccounts.connections.list() -> SyncPagingIterable&amp;lt;ConnectedAccountConnection&amp;gt;</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieve available connections that can be used for account linking by the authenticated user.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.connectedAccounts().connections().list(
    ListConnectedAccountsConnectionsRequestParameters
        .builder()
        .from(
            OptionalNullable.of("from")
        )
        .take(
            OptionalNullable.of(1)
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**from:** `Optional<String>` — Cursor for pagination - start retrieving results from this point
    
</dd>
</dl>

<dl>
<dd>

**take:** `Optional<Integer>` — Number of results to return (1-20)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

