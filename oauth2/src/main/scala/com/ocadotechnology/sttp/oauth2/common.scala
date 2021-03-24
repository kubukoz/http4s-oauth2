package com.kubukoz.ho2

import com.kubukoz.ho2.common.Error.OAuth2ErrorResponse.InvalidClient
import com.kubukoz.ho2.common.Error.OAuth2ErrorResponse.InvalidGrant
import com.kubukoz.ho2.common.Error.OAuth2ErrorResponse.InvalidRequest
import com.kubukoz.ho2.common.Error.OAuth2ErrorResponse.InvalidScope
import com.kubukoz.ho2.common.Error.OAuth2ErrorResponse.UnauthorizedClient
import com.kubukoz.ho2.common.Error.OAuth2ErrorResponse.UnsupportedGrantType
import io.circe.Decoder
import org.http4s.Status

object common {

  sealed trait Error extends Product with Serializable

  object Error {

    final case class HttpClientError(statusCode: Status, cause: String) extends Error

    sealed trait OAuth2Error extends Error

    /** Token errors as listed in documentation: https://tools.ietf.org/html/rfc6749#section-5.2
      */
    final case class OAuth2ErrorResponse(errorType: OAuth2ErrorResponse.OAuth2ErrorResponseType, errorDescription: String)
      extends OAuth2Error

    object OAuth2ErrorResponse {

      sealed trait OAuth2ErrorResponseType extends Product with Serializable

      case object InvalidRequest extends OAuth2ErrorResponseType

      case object InvalidClient extends OAuth2ErrorResponseType

      case object InvalidGrant extends OAuth2ErrorResponseType

      case object UnauthorizedClient extends OAuth2ErrorResponseType

      case object UnsupportedGrantType extends OAuth2ErrorResponseType

      case object InvalidScope extends OAuth2ErrorResponseType

    }

    final case class UnknownOAuth2Error(error: String, description: String) extends OAuth2Error

    implicit val errorDecoder: Decoder[OAuth2Error] =
      Decoder.forProduct2[OAuth2Error, String, String]("error", "error_description") { (error, description) =>
        error match {
          case "invalid_request"        => OAuth2ErrorResponse(InvalidRequest, description)
          case "invalid_client"         => OAuth2ErrorResponse(InvalidClient, description)
          case "invalid_grant"          => OAuth2ErrorResponse(InvalidGrant, description)
          case "unauthorized_client"    => OAuth2ErrorResponse(UnauthorizedClient, description)
          case "unsupported_grant_type" => OAuth2ErrorResponse(UnsupportedGrantType, description)
          case "invalid_scope"          => OAuth2ErrorResponse(InvalidScope, description)
          case unknown                  => UnknownOAuth2Error(unknown, description)
        }
      }

  }

  final case class OAuth2Exception(error: Error) extends Throwable

  final case class ParsingException(msg: String) extends Throwable
}
