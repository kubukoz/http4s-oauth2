package com.kubukoz.ho2

import cats.effect.Concurrent
import org.http4s.Method.POST
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

import OAuth2Token.bearerTokenResponseDecoder

object PasswordGrant {

  final case class User(name: String, password: Secret[String])

  def requestToken[F[_]: Concurrent](
    tokenUri: Uri,
    user: User,
    clientId: String,
    clientSecret: Secret[String],
    scope: String
  )(
    implicit client: Client[F]
  ): F[OAuth2Token.Response] = {
    object dsl extends Http4sClientDsl[F]
    import dsl._

    client.expect {
      POST(tokenUri)
        .withEntity(requestTokenParams(clientId, user, clientSecret, scope))
    }
  }

  private def requestTokenParams(clientId: String, user: User, clientSecret: Secret[String], scope: String) =
    Map(
      "grant_type" -> "password",
      "username" -> user.name,
      "password" -> user.password.value,
      "client_id" -> clientId,
      "client_secret" -> clientSecret.value,
      "scope" -> scope
    )

}
