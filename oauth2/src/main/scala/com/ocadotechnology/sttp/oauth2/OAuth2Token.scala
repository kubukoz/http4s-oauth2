package com.kubukoz.ho2

import com.kubukoz.ho2.common.Error.OAuth2Error
import io.circe.Decoder

object OAuth2Token {

  type Response = Either[OAuth2Error, Oauth2TokenResponse]

  private[ho2] implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, Oauth2TokenResponse]] =
    circe.eitherOrFirstError[Oauth2TokenResponse, OAuth2Error](
      Decoder[Oauth2TokenResponse],
      Decoder[OAuth2Error]
    )

}
