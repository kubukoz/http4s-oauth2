package com.kubukoz.ho2

import cats.effect.Concurrent
import org.http4s.Method.POST
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

object ClientCredentials {

  /** Requests token from OAuth2 provider `tokenUri` using `clientId`, `clientSecret`, requested `scope` and `client_credentials` grant type.
    * Request is performed with provided `backend`.
    *
    * All errors are mapped to [[common.Error]] ADT.
    */
  def requestToken[F[_]: Concurrent](
    tokenUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    scope: String
  )(
    implicit client: Client[F]
  ): F[ClientCredentialsToken.Response] = {
    object dsl extends Http4sClientDsl[F]
    import dsl._

    client.expect(
      POST(
        tokenUri
      ).withEntity(requestTokenParams(clientId, clientSecret, scope))
    )
  }

  private def requestTokenParams(clientId: String, clientSecret: Secret[String], scope: String) =
    Map(
      "grant_type" -> "client_credentials",
      "client_id" -> clientId,
      "client_secret" -> clientSecret.value,
      "scope" -> scope
    )

  /** Introspects provided `token` in OAuth2 provider `tokenIntrospectionUri`, using `clientId` and `clientSecret`.
    * Request is performed with provided `backend`.
    *
    * Errors are mapped to [[common.Error]] ADT.
    */
  def introspectToken[F[_]: Concurrent](
    tokenIntrospectionUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    token: Secret[String]
  )(
    implicit client: Client[F]
  ): F[Introspection.Response] = {
    object dsl extends Http4sClientDsl[F]
    import dsl._

    client.expect(
      POST(
        tokenIntrospectionUri
      ).withEntity(requestTokenIntrospectionParams(clientId, clientSecret, token))
    )
  }

  private def requestTokenIntrospectionParams(clientId: String, clientSecret: Secret[String], token: Secret[String]) =
    Map(
      "client_id" -> clientId,
      "client_secret" -> clientSecret.value,
      "token" -> token.value
    )

}
