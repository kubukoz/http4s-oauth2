package com.kubukoz.ho2

import cats.effect.Concurrent
import cats.implicits._
import com.kubukoz.ho2.common.OAuth2Exception
import org.http4s.Uri
import org.http4s.client.Client

/** Tagless Final algebra for ClientCredentials token requests and verification.
  */
trait ClientCredentialsProvider[F[_]] {

  /** Request new token with given scope from OAuth2 provider.
    *
    * The scope is the scope of the application we want to communicate with.
    */
  def requestToken(scope: String): F[ClientCredentialsToken.AccessTokenResponse]

  /** Introspects passed token in OAuth2 provider.
    *
    * Successful introspections returns `F[TokenIntrospectionResponse.IntrospectionResponse]`.
    */
  def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse]

}

object ClientCredentialsProvider {

  /** Create instance of auth provider with sttp backend.
    *
    * `clientId`, `clientSecret`, `applicationScope` are parameters of your application.
    */
  def instance[F[_]: Client: Concurrent](
    tokenUrl: Uri,
    tokenIntrospectionUrl: Uri,
    clientId: String,
    clientSecret: Secret[String]
  ): ClientCredentialsProvider[F] =
    new ClientCredentialsProvider[F] {

      override def requestToken(scope: String): F[ClientCredentialsToken.AccessTokenResponse] =
        ClientCredentials
          .requestToken[F](tokenUrl, clientId, clientSecret, scope)
          .map(_.leftMap(OAuth2Exception(_)))
          .rethrow

      override def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse] =
        ClientCredentials
          .introspectToken[F](tokenIntrospectionUrl, clientId, clientSecret, token)
          .map(_.leftMap(OAuth2Exception(_)))
          .rethrow

    }

}
