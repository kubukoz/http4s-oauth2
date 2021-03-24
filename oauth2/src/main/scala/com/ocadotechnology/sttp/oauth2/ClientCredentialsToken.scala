package com.kubukoz.ho2

import scala.concurrent.duration.FiniteDuration

import com.kubukoz.ho2.common.Error.OAuth2Error
import io.circe.Decoder

object ClientCredentialsToken {

  //this should be ADTed really
  type Response = Either[OAuth2Error, ClientCredentialsToken.AccessTokenResponse]

  private[ho2] implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, AccessTokenResponse]] =
    circe.eitherOrFirstError[AccessTokenResponse, OAuth2Error](
      Decoder[AccessTokenResponse],
      Decoder[OAuth2Error]
    )

  final case class AccessTokenResponse(
    accessToken: Secret[String],
    domain: String,
    expiresIn: FiniteDuration,
    scope: String
  )

  object AccessTokenResponse {

    import com.kubukoz.ho2.circe._

    implicit val tokenDecoder: Decoder[AccessTokenResponse] =
      Decoder
        .forProduct4(
          "access_token",
          "domain",
          "expires_in",
          "scope"
        )(AccessTokenResponse.apply)
        .validate {
          _.downField("token_type").as[String] match {
            case Right(value) if value.equalsIgnoreCase("Bearer") => List.empty
            case Right(string)                                    => List(s"Error while decoding '.token_type': value '$string' is not equal to 'Bearer'")
            case Left(s)                                          => List(s"Error while decoding '.token_type': ${s.getMessage}")
          }
        }

  }

}
