package com.kubukoz.ho2

import cats.effect.Concurrent
import cats.syntax.all._
import com.kubukoz.ho2.PasswordGrant.User
import org.http4s.Uri
import org.http4s.client.Client

import common._

trait PasswordGrantProvider[F[_]] {
  def requestToken(user: User, scope: String): F[Oauth2TokenResponse]
}

object PasswordGrantProvider {

  def apply[F[_]](implicit ev: PasswordGrantProvider[F]): PasswordGrantProvider[F] = ev

  def instance[F[_]: Concurrent: Client](
    tokenUrl: Uri,
    clientId: String,
    clientSecret: Secret[String]
  ): PasswordGrantProvider[F] = { (user: User, scope: String) =>
    PasswordGrant.requestToken[F](tokenUrl, user, clientId, clientSecret, scope).map(_.leftMap(OAuth2Exception)).rethrow
  }

}
