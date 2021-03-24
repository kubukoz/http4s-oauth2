package com.kubukoz.ho2

import java.time.Instant

import com.kubukoz.ho2.common.Error.OAuth2Error
import io.circe.Decoder

object Introspection {

  type Response = Either[OAuth2Error, Introspection.TokenIntrospectionResponse]

  private[ho2] implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, TokenIntrospectionResponse]] =
    circe.eitherOrFirstError[TokenIntrospectionResponse, OAuth2Error](
      Decoder[TokenIntrospectionResponse],
      Decoder[OAuth2Error]
    )

  final case class TokenIntrospectionResponse(
    clientId: String,
    domain: String,
    exp: Instant,
    active: Boolean,
    authorities: List[String],
    scope: String,
    tokenType: String
  )

  object TokenIntrospectionResponse {

    private implicit val instantDecoder: Decoder[Instant] = Decoder.decodeLong.map(Instant.ofEpochSecond)

    implicit val decoder: Decoder[TokenIntrospectionResponse] =
      Decoder.forProduct7(
        "client_id",
        "domain",
        "exp",
        "active",
        "authorities",
        "scope",
        "token_type"
      )(TokenIntrospectionResponse.apply)

  }

}
