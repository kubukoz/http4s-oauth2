# httpt4s-oauth2 - OAuth2 client library for http4s

This is a fork of [ocadotechnology/sttp-oauth2](https://github.com/ocadotechnology/sttp-oauth2), adjusted to support http4s directly without going through sttp.

This library aims to provide easy integration with OAuth2 providers based on [OAuth2 RFC](https://tools.ietf.org/html/rfc6749) using [http4s](https://github.com/http4s/http4s) client. It uses [circe](https://github.com/circe/circe) for JSON serialization/deserialization.

Currently it supports methods (grant types) for obtaining authorization:
 - [Authorization code](https://tools.ietf.org/html/rfc6749#section-4.1)
 - [Password grant](https://tools.ietf.org/html/rfc6749#section-4.3)
 - [Client credentials](https://tools.ietf.org/html/rfc6749#section-4.4)


## Usage

### `http4s-oauth2`

Each grant is implemented in an object with explicit return and error types on methods and additionally, Tagless Final friendly `*Provider` interface.
- `AuthorizationCode` and `AuthorizationCodeProvider` - provide functionality for:
  - generating _login_ and _logout_ redirect links,
  - `authCodeToToken` for converting authorization code to token,
  - `refreshAccessToken` for performing a token refresh request
- `PasswordGrant` and `PasswordGrantProvider`, capable of performing `requestToken` to convert user login and password to oauth2 token
- `ClientCredentials` and `ClientCredentialsProvider` expose methods that:
  - Obtain token via `requestToken`
  - `introspect` the token for it's details like `UserInfo`

## Contributing

Feel free to submit feature requests and bug reports under Issues.

## License

http4s-oauth2 is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this software except in compliance with the License.

sttp-oauth2 is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this software except in compliance with the License.

Copyright © 2021 Jakub Kozłowski
Copyright © 2020 Ocado
