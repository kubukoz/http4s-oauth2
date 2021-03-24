package com.kubukoz.ho2

import cats.effect.Concurrent
import org.http4s.AuthScheme
import org.http4s.Credentials
import org.http4s.Method.POST
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Authorization

trait UserInfoProvider[F[_]] {
  def userInfo(accessToken: String): F[UserInfo]
}

object UserInfoProvider {
  def apply[F[_]](implicit ev: UserInfoProvider[F]): UserInfoProvider[F] = ev

  private def requestUserInfo[F[_]: Concurrent](
    baseUrl: Uri,
    accessToken: String
  )(
    implicit client: Client[F]
  ): F[UserInfo] = {
    object dsl extends Http4sClientDsl[F]
    import dsl._

    client
      .expect {
        POST.apply(uri = baseUrl / "openid" / "userinfo", Authorization(Credentials.Token(AuthScheme.Bearer, accessToken)))
      }
  }

  // TODO - add some description on what is expected of baseUrl
  def instance[F[_]: Concurrent: Client](
    baseUrl: Uri
  ): UserInfoProvider[F] =
    (accessToken: String) => requestUserInfo(baseUrl, accessToken)

}
